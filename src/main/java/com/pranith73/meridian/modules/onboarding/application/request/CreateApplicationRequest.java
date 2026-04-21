package com.pranith73.meridian.modules.onboarding.application.request;

import java.util.List;
import java.util.UUID;

/**
 * Input data needed to open a new onboarding application.
 *
 * Either merchantId or prospectReference must be provided.
 * merchantId is used when a Merchant Core record already exists.
 * prospectReference is used when the applicant has not yet been
 * assigned a canonical merchant identity.
 */
public class CreateApplicationRequest {

    private UUID merchantId;
    private String prospectReference;
    private List<String> requestedProducts;

    public CreateApplicationRequest() {}

    public CreateApplicationRequest(UUID merchantId, String prospectReference, List<String> requestedProducts) {
        this.merchantId = merchantId;
        this.prospectReference = prospectReference;
        this.requestedProducts = requestedProducts;
    }

    public UUID getMerchantId() { return merchantId; }
    public void setMerchantId(UUID merchantId) { this.merchantId = merchantId; }

    public String getProspectReference() { return prospectReference; }
    public void setProspectReference(String prospectReference) { this.prospectReference = prospectReference; }

    public List<String> getRequestedProducts() { return requestedProducts; }
    public void setRequestedProducts(List<String> requestedProducts) { this.requestedProducts = requestedProducts; }
}
