package com.pranith73.meridian.modules.merchantcore.application;

import com.pranith73.meridian.modules.merchantcore.application.request.ChangeMerchantStatusRequest;
import com.pranith73.meridian.modules.merchantcore.application.request.CreateMerchantRequest;
import com.pranith73.meridian.modules.merchantcore.application.request.SearchMerchantsRequest;
import com.pranith73.meridian.modules.merchantcore.application.request.UpdateMerchantProfileRequest;
import com.pranith73.meridian.modules.merchantcore.domain.Merchant;
import com.pranith73.meridian.modules.merchantcore.domain.MerchantStatus;
import com.pranith73.meridian.modules.merchantcore.infrastructure.InMemoryMerchantRepository;
import com.pranith73.meridian.shared.audit.InMemoryAuditSink;
import com.pranith73.meridian.shared.outbox.InMemoryOutboxSink;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MerchantApplicationServiceTest {

    @Test
    void createMerchantCreatesPendingMerchantAndEmitsAuditAndOutboxFacts() {
        InMemoryMerchantRepository merchantRepository = new InMemoryMerchantRepository();
        InMemoryAuditSink auditSink = new InMemoryAuditSink();
        InMemoryOutboxSink outboxSink = new InMemoryOutboxSink();

        MerchantApplicationService service =
                new MerchantApplicationService(merchantRepository, auditSink, outboxSink);

        CreateMerchantRequest request = new CreateMerchantRequest(
                "  Pranith Legal LLC  ",
                "  Pranith Store  "
        );

        Merchant merchant = service.createMerchant(request);

        assertNotNull(merchant.getMerchantId());
        assertEquals("Pranith Legal LLC", merchant.getLegalName());
        assertEquals("Pranith Store", merchant.getDisplayName());
        assertEquals(MerchantStatus.PENDING, merchant.getMerchantStatus());

        assertEquals(1, auditSink.getRecords().size());
        assertEquals("MERCHANT_CREATED", auditSink.getRecords().get(0).actionType());
        assertEquals("MERCHANT", auditSink.getRecords().get(0).subjectType());
        assertEquals(merchant.getMerchantId().toString(), auditSink.getRecords().get(0).subjectId());

        assertEquals(1, outboxSink.getMessages().size());
        assertEquals("merchant.created", outboxSink.getMessages().get(0).eventType());
        assertEquals("MERCHANT", outboxSink.getMessages().get(0).aggregateType());
        assertEquals(merchant.getMerchantId().toString(), outboxSink.getMessages().get(0).aggregateId());
    }

    @Test
    void updateMerchantProfileUpdatesNamesAndEmitsAuditAndOutboxFacts() {
        InMemoryMerchantRepository merchantRepository = new InMemoryMerchantRepository();
        InMemoryAuditSink auditSink = new InMemoryAuditSink();
        InMemoryOutboxSink outboxSink = new InMemoryOutboxSink();

        MerchantApplicationService service =
                new MerchantApplicationService(merchantRepository, auditSink, outboxSink);

        Merchant created = service.createMerchant(new CreateMerchantRequest(
                "Original Legal LLC",
                "Original Store"
        ));

        Merchant updated = service.updateMerchantProfile(new UpdateMerchantProfileRequest(
                created.getMerchantId(),
                "  Updated Legal LLC  ",
                "  Updated Store  "
        ));

        assertEquals(created.getMerchantId(), updated.getMerchantId());
        assertEquals("Updated Legal LLC", updated.getLegalName());
        assertEquals("Updated Store", updated.getDisplayName());
        assertEquals(MerchantStatus.PENDING, updated.getMerchantStatus());

        assertEquals(2, auditSink.getRecords().size());
        assertEquals("MERCHANT_PROFILE_UPDATED", auditSink.getRecords().get(1).actionType());
        assertEquals("MERCHANT", auditSink.getRecords().get(1).subjectType());
        assertEquals(updated.getMerchantId().toString(), auditSink.getRecords().get(1).subjectId());

        assertEquals(2, outboxSink.getMessages().size());
        assertEquals("merchant.profile.updated", outboxSink.getMessages().get(1).eventType());
        assertEquals("MERCHANT", outboxSink.getMessages().get(1).aggregateType());
        assertEquals(updated.getMerchantId().toString(), outboxSink.getMessages().get(1).aggregateId());
    }

    @Test
    void changeMerchantStatusAllowsPendingToActiveAndEmitsAuditAndOutboxFacts() {
        InMemoryMerchantRepository merchantRepository = new InMemoryMerchantRepository();
        InMemoryAuditSink auditSink = new InMemoryAuditSink();
        InMemoryOutboxSink outboxSink = new InMemoryOutboxSink();

        MerchantApplicationService service =
                new MerchantApplicationService(merchantRepository, auditSink, outboxSink);

        Merchant created = service.createMerchant(new CreateMerchantRequest(
                "Lifecycle Legal LLC",
                "Lifecycle Store"
        ));

        Merchant updated = service.changeMerchantStatus(new ChangeMerchantStatusRequest(
                created.getMerchantId(),
                MerchantStatus.ACTIVE
        ));

        assertEquals(created.getMerchantId(), updated.getMerchantId());
        assertEquals(MerchantStatus.ACTIVE, updated.getMerchantStatus());

        assertEquals(2, auditSink.getRecords().size());
        assertEquals("MERCHANT_STATUS_CHANGED", auditSink.getRecords().get(1).actionType());
        assertEquals("MERCHANT", auditSink.getRecords().get(1).subjectType());
        assertEquals(updated.getMerchantId().toString(), auditSink.getRecords().get(1).subjectId());

        assertEquals(2, outboxSink.getMessages().size());
        assertEquals("merchant.status.changed", outboxSink.getMessages().get(1).eventType());
        assertEquals("MERCHANT", outboxSink.getMessages().get(1).aggregateType());
        assertEquals(updated.getMerchantId().toString(), outboxSink.getMessages().get(1).aggregateId());
    }

    @Test
    void changeMerchantStatusRejectsIllegalTransitionAndDoesNotEmitNewFacts() {
        InMemoryMerchantRepository merchantRepository = new InMemoryMerchantRepository();
        InMemoryAuditSink auditSink = new InMemoryAuditSink();
        InMemoryOutboxSink outboxSink = new InMemoryOutboxSink();

        MerchantApplicationService service =
                new MerchantApplicationService(merchantRepository, auditSink, outboxSink);

        Merchant created = service.createMerchant(new CreateMerchantRequest(
                "Illegal Transition Legal LLC",
                "Illegal Transition Store"
        ));

        assertThrows(IllegalStateException.class, () ->
                service.changeMerchantStatus(new ChangeMerchantStatusRequest(
                        created.getMerchantId(),
                        MerchantStatus.CLOSED
                ))
        );

        assertEquals(MerchantStatus.PENDING, service.getMerchantById(created.getMerchantId()).getMerchantStatus());

        // Only the original create action should have emitted facts.
        assertEquals(1, auditSink.getRecords().size());
        assertEquals("MERCHANT_CREATED", auditSink.getRecords().get(0).actionType());

        assertEquals(1, outboxSink.getMessages().size());
        assertEquals("merchant.created", outboxSink.getMessages().get(0).eventType());
    }

    @Test
    void searchMerchantsReturnsOnlyMatchingMerchants() {
        InMemoryMerchantRepository merchantRepository = new InMemoryMerchantRepository();
        InMemoryAuditSink auditSink = new InMemoryAuditSink();
        InMemoryOutboxSink outboxSink = new InMemoryOutboxSink();

        MerchantApplicationService service =
                new MerchantApplicationService(merchantRepository, auditSink, outboxSink);

        Merchant alpha = service.createMerchant(new CreateMerchantRequest(
                "Alpha Foods LLC",
                "Alpha Store"
        ));

        service.createMerchant(new CreateMerchantRequest(
                "Beta Retail LLC",
                "Beta Shop"
        ));

        List<Merchant> results = service.searchMerchants(new SearchMerchantsRequest("alpha"));

        assertEquals(1, results.size());
        assertEquals(alpha.getMerchantId(), results.get(0).getMerchantId());
        assertEquals("Alpha Foods LLC", results.get(0).getLegalName());
    }
}
