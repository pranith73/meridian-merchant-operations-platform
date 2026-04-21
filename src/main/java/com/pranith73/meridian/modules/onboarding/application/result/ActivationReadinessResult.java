package com.pranith73.meridian.modules.onboarding.application.result;

import com.pranith73.meridian.modules.onboarding.domain.DocumentType;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Describes whether a MerchantApplication is ready for activation.
 *
 * Approval is a business decision — it means the reviewer accepted the application.
 * Readiness is a gate that follows approval — it confirms all blocking document
 * requirements are cleared so activation can safely proceed.
 *
 * A result is not ready when either:
 *  - the application is not yet in APPROVED status, or
 *  - one or more required documents are not ACCEPTED.
 */
public class ActivationReadinessResult {

    private final UUID applicationId;
    private final boolean readyForActivation;

    // Non-empty only when readyForActivation is false due to unaccepted documents.
    private final List<DocumentType> missingOrUnacceptedDocumentTypes;

    // Human-readable explanation of why the application is or is not ready.
    private final String readinessMessage;

    private ActivationReadinessResult(UUID applicationId,
                                       boolean readyForActivation,
                                       List<DocumentType> missingOrUnacceptedDocumentTypes,
                                       String readinessMessage) {
        if (applicationId == null) throw new IllegalArgumentException("applicationId must not be null");
        if (missingOrUnacceptedDocumentTypes == null) throw new IllegalArgumentException("missingOrUnacceptedDocumentTypes must not be null");
        if (readinessMessage == null || readinessMessage.isBlank()) throw new IllegalArgumentException("readinessMessage must not be blank");

        this.applicationId = applicationId;
        this.readyForActivation = readyForActivation;
        this.missingOrUnacceptedDocumentTypes = Collections.unmodifiableList(missingOrUnacceptedDocumentTypes);
        this.readinessMessage = readinessMessage;
    }

    /** Creates a result indicating the application is ready for activation. */
    public static ActivationReadinessResult ready(UUID applicationId) {
        return new ActivationReadinessResult(applicationId, true, List.of(), "Application is ready for activation.");
    }

    /** Creates a result indicating the application is not ready, with a message explaining why. */
    public static ActivationReadinessResult notReady(UUID applicationId,
                                                      List<DocumentType> missingOrUnaccepted,
                                                      String readinessMessage) {
        return new ActivationReadinessResult(applicationId, false, missingOrUnaccepted, readinessMessage);
    }

    public UUID getApplicationId()                               { return applicationId; }
    public boolean isReadyForActivation()                        { return readyForActivation; }
    public List<DocumentType> getMissingOrUnacceptedDocumentTypes() { return missingOrUnacceptedDocumentTypes; }
    public String getReadinessMessage()                          { return readinessMessage; }
}
