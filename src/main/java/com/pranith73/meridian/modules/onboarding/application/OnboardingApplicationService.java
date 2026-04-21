package com.pranith73.meridian.modules.onboarding.application;

import com.pranith73.meridian.modules.onboarding.application.request.CreateApplicationRequest;
import com.pranith73.meridian.modules.onboarding.application.request.DecisionRequest;
import com.pranith73.meridian.modules.onboarding.application.request.RequestChangesRequest;
import com.pranith73.meridian.modules.onboarding.application.result.ActivationReadinessResult;
import com.pranith73.meridian.modules.onboarding.application.result.ApplicationDecisionResult;
import com.pranith73.meridian.modules.onboarding.domain.ActivationDecision;
import com.pranith73.meridian.modules.onboarding.domain.ApplicationStatus;
import com.pranith73.meridian.modules.onboarding.domain.DocumentType;
import com.pranith73.meridian.modules.onboarding.domain.MerchantApplication;
import com.pranith73.meridian.modules.onboarding.domain.RequiredDocument;
import com.pranith73.meridian.modules.onboarding.domain.RequiredDocumentStatus;
import com.pranith73.meridian.shared.audit.AuditRecord;
import com.pranith73.meridian.shared.audit.AuditSink;
import com.pranith73.meridian.shared.error.ResourceNotFoundException;
import com.pranith73.meridian.shared.outbox.OutboxMessage;
import com.pranith73.meridian.shared.outbox.OutboxSink;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles write operations for the Onboarding module.
 *
 * This service enforces onboarding workflow rules and coordinates
 * changes to MerchantApplication records. It delegates storage to
 * OnboardingApplicationRepository so it is not tied to any specific
 * storage technology.
 *
 * Allowed operations:
 *   - createApplication
 *   - submitApplication
 *   - startReview
 *   - requestChanges
 *   - approveApplication
 *   - rejectApplication
 */
public class OnboardingApplicationService {

    private final OnboardingApplicationRepository repository;
    private final AuditSink auditSink;
    private final OutboxSink outboxSink;

    public OnboardingApplicationService(OnboardingApplicationRepository repository,
                                        AuditSink auditSink,
                                        OutboxSink outboxSink) {
        this.repository = repository;
        this.auditSink = auditSink;
        this.outboxSink = outboxSink;
    }

    // ---------------------------------------------------------------------------
    // Create
    // ---------------------------------------------------------------------------

    /**
     * Opens a new onboarding application in DRAFT status.
     * Either merchantId or prospectReference must be provided on the request.
     */
    public MerchantApplication createApplication(CreateApplicationRequest request) {
        if (request == null) throw new IllegalArgumentException("request must not be null");

        MerchantApplication application = MerchantApplication.openDraft(
                request.getMerchantId(),
                request.getProspectReference(),
                request.getRequestedProducts()
        );

        MerchantApplication saved = repository.save(application);
        Instant now = saved.getCreatedAt();

        // Audit facts record that a governed action happened, for traceability.
        emitAudit("ONBOARDING_APPLICATION_CREATED", saved.getApplicationId(), now,
                "status=" + saved.getApplicationStatus());
        // Outbox facts are durable handoff signals — not direct cross-module writes.
        emitOutbox("onboarding.application.created", saved.getApplicationId(), now,
                "{\"applicationId\":\"" + saved.getApplicationId() + "\"}");

        return saved;
    }

    // ---------------------------------------------------------------------------
    // Submit
    // ---------------------------------------------------------------------------

    /**
     * Advances an existing DRAFT application to SUBMITTED.
     * Throws if the application is not found or is not in DRAFT status.
     */
    public MerchantApplication submitApplication(UUID applicationId) {
        if (applicationId == null) throw new IllegalArgumentException("applicationId must not be null");

        MerchantApplication application = repository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Onboarding application not found: " + applicationId));

        // submit() enforces the DRAFT -> SUBMITTED transition rule.
        application.submit();

        MerchantApplication saved = repository.save(application);
        Instant now = saved.getUpdatedAt();

        emitAudit("ONBOARDING_APPLICATION_SUBMITTED", saved.getApplicationId(), now,
                "status=" + saved.getApplicationStatus());
        emitOutbox("onboarding.application.submitted", saved.getApplicationId(), now,
                "{\"applicationId\":\"" + saved.getApplicationId() + "\"}");

        return saved;
    }

    // ---------------------------------------------------------------------------
    // Start review
    // ---------------------------------------------------------------------------

    /**
     * Assigns an analyst and moves the application into UNDER_REVIEW.
     * Allowed from SUBMITTED or NEEDS_INFO.
     * Throws if the application is not found or the transition is not allowed.
     */
    public MerchantApplication startReview(UUID applicationId, UUID assignedAnalystId) {
        if (applicationId == null)     throw new IllegalArgumentException("applicationId must not be null");
        if (assignedAnalystId == null) throw new IllegalArgumentException("assignedAnalystId must not be null");

        MerchantApplication application = requireApplication(applicationId);

        // startReview() enforces SUBMITTED/NEEDS_INFO -> UNDER_REVIEW.
        application.startReview(assignedAnalystId);

        MerchantApplication saved = repository.save(application);
        Instant now = saved.getUpdatedAt();

        emitAudit("ONBOARDING_REVIEW_STARTED", saved.getApplicationId(), now,
                "analystId=" + assignedAnalystId);
        emitOutbox("onboarding.review.started", saved.getApplicationId(), now,
                "{\"applicationId\":\"" + saved.getApplicationId() + "\",\"analystId\":\"" + assignedAnalystId + "\"}");

        return saved;
    }

    // ---------------------------------------------------------------------------
    // Request changes
    // ---------------------------------------------------------------------------

    /**
     * Flags the application as NEEDS_INFO and records the reason the analyst
     * requires additional information from the applicant.
     * Only allowed while the application is UNDER_REVIEW.
     * Throws if the application is not found or the transition is not allowed.
     */
    public MerchantApplication requestChanges(RequestChangesRequest request) {
        if (request == null)                                          throw new IllegalArgumentException("request must not be null");
        if (request.getApplicationId() == null)                       throw new IllegalArgumentException("applicationId must not be null");
        if (request.getReasonSummary() == null || request.getReasonSummary().isBlank()) throw new IllegalArgumentException("reasonSummary must not be blank");

        MerchantApplication application = requireApplication(request.getApplicationId());

        // requestChanges() enforces UNDER_REVIEW -> NEEDS_INFO.
        application.requestChanges(request.getReasonSummary());

        MerchantApplication saved = repository.save(application);
        Instant now = saved.getUpdatedAt();

        emitAudit("ONBOARDING_CHANGES_REQUESTED", saved.getApplicationId(), now,
                "reason=" + request.getReasonSummary());
        emitOutbox("onboarding.changes.requested", saved.getApplicationId(), now,
                "{\"applicationId\":\"" + saved.getApplicationId() + "\"}");

        return saved;
    }

    // ---------------------------------------------------------------------------
    // Approve
    // ---------------------------------------------------------------------------

    /**
     * Records an approval decision for an UNDER_REVIEW application.
     * Creates ActivationDecision evidence and returns both the updated
     * application and the decision together in ApplicationDecisionResult.
     * ActivationDecision is not persisted separately yet; that comes later.
     */
    public ApplicationDecisionResult approveApplication(DecisionRequest request) {
        validateDecisionRequest(request);

        MerchantApplication application = requireApplication(request.getApplicationId());

        // approve() enforces the UNDER_REVIEW -> APPROVED transition.
        application.approve();

        ActivationDecision decision = ActivationDecision.approve(
                application.getApplicationId(),
                request.getDecisionReasonSummary(),
                request.getDecidedBy()
        );

        MerchantApplication saved = repository.save(application);
        Instant now = saved.getUpdatedAt();

        emitAudit("ONBOARDING_APPLICATION_APPROVED", saved.getApplicationId(), now,
                "decidedBy=" + request.getDecidedBy());
        emitOutbox("onboarding.application.approved", saved.getApplicationId(), now,
                "{\"applicationId\":\"" + saved.getApplicationId() + "\",\"decidedBy\":\"" + request.getDecidedBy() + "\"}");

        return new ApplicationDecisionResult(saved, decision);
    }

    // ---------------------------------------------------------------------------
    // Reject
    // ---------------------------------------------------------------------------

    /**
     * Records a rejection decision for an UNDER_REVIEW application.
     * Creates ActivationDecision evidence and returns both the updated
     * application and the decision together in ApplicationDecisionResult.
     * ActivationDecision is not persisted separately yet; that comes later.
     */
    public ApplicationDecisionResult rejectApplication(DecisionRequest request) {
        validateDecisionRequest(request);

        MerchantApplication application = requireApplication(request.getApplicationId());

        // reject() enforces the UNDER_REVIEW -> REJECTED transition.
        application.reject();

        ActivationDecision decision = ActivationDecision.reject(
                application.getApplicationId(),
                request.getDecisionReasonSummary(),
                request.getDecidedBy()
        );

        MerchantApplication saved = repository.save(application);
        Instant now = saved.getUpdatedAt();

        emitAudit("ONBOARDING_APPLICATION_REJECTED", saved.getApplicationId(), now,
                "decidedBy=" + request.getDecidedBy());
        emitOutbox("onboarding.application.rejected", saved.getApplicationId(), now,
                "{\"applicationId\":\"" + saved.getApplicationId() + "\",\"decidedBy\":\"" + request.getDecidedBy() + "\"}");

        return new ApplicationDecisionResult(saved, decision);
    }

    // ---------------------------------------------------------------------------
    // Activation readiness check
    // ---------------------------------------------------------------------------

    /**
     * Checks whether an APPROVED application has all required documents accepted.
     *
     * Approval is a business decision — it means the reviewer accepted the application.
     * Readiness is the gate that follows — it confirms every document blocker is cleared
     * so activation can safely proceed without surprises.
     *
     * This method does not change application status or trigger any downstream action.
     * An outbox fact is emitted only when the result is ready, so downstream systems
     * can act without Onboarding making direct cross-module calls.
     */
    public ActivationReadinessResult checkActivationReadiness(UUID applicationId,
                                                               List<RequiredDocument> requiredDocuments) {
        if (applicationId == null)      throw new IllegalArgumentException("applicationId must not be null");
        if (requiredDocuments == null)  throw new IllegalArgumentException("requiredDocuments must not be null");

        MerchantApplication application = requireApplication(applicationId);

        // Verify all provided documents belong to this application.
        for (RequiredDocument doc : requiredDocuments) {
            if (!applicationId.equals(doc.getApplicationId())) {
                throw new IllegalArgumentException(
                        "Document " + doc.getRequiredDocumentId() + " belongs to a different application");
            }
        }

        // Application must be APPROVED before readiness can be considered.
        if (application.getApplicationStatus() != ApplicationStatus.APPROVED) {
            return ActivationReadinessResult.notReady(
                    applicationId,
                    List.of(),
                    "Application must be APPROVED before readiness can be assessed, "
                            + "current status: " + application.getApplicationStatus());
        }

        // Collect any document requirements that are not yet ACCEPTED.
        List<DocumentType> unaccepted = new ArrayList<>();
        for (RequiredDocument doc : requiredDocuments) {
            if (doc.getRequirementStatus() != RequiredDocumentStatus.ACCEPTED) {
                unaccepted.add(doc.getDocumentType());
            }
        }

        if (!unaccepted.isEmpty()) {
            return ActivationReadinessResult.notReady(
                    applicationId,
                    unaccepted,
                    "One or more required documents are not yet accepted: " + unaccepted);
        }

        // All checks passed — emit facts so downstream can act without a direct call.
        Instant now = Instant.now();
        emitAudit("ONBOARDING_APPLICATION_READY_FOR_ACTIVATION", applicationId, now,
                "applicationId=" + applicationId);
        emitOutbox("onboarding.application.ready_for_activation", applicationId, now,
                "{\"applicationId\":\"" + applicationId + "\"}");

        return ActivationReadinessResult.ready(applicationId);
    }

    // ---------------------------------------------------------------------------
    // Audit and outbox helpers
    // ---------------------------------------------------------------------------

    // Audit facts are for traceability — they record that a governed action happened.
    private void emitAudit(String actionType, UUID applicationId, Instant occurredAt, String detail) {
        auditSink.record(new AuditRecord(
                actionType,
                "MERCHANT_APPLICATION",
                applicationId.toString(),
                "SYSTEM",
                "LOCAL",
                occurredAt,
                detail
        ));
    }

    // Outbox facts are durable handoff signals — not direct cross-module writes.
    private void emitOutbox(String eventType, UUID applicationId, Instant occurredAt, String payload) {
        outboxSink.save(new OutboxMessage(
                eventType,
                "MERCHANT_APPLICATION",
                applicationId.toString(),
                payload,
                "LOCAL",
                occurredAt
        ));
    }

    // ---------------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------------

    private MerchantApplication requireApplication(UUID applicationId) {
        return repository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Onboarding application not found: " + applicationId));
    }

    private void validateDecisionRequest(DecisionRequest request) {
        if (request == null) throw new IllegalArgumentException("request must not be null");
        if (request.getApplicationId() == null) throw new IllegalArgumentException("applicationId must not be null");
        if (request.getDecidedBy() == null) throw new IllegalArgumentException("decidedBy must not be null");
        if (request.getDecisionReasonSummary() == null || request.getDecisionReasonSummary().isBlank()) {
            throw new IllegalArgumentException("decisionReasonSummary must not be blank");
        }
    }
}
