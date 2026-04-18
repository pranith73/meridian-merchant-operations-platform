package com.pranith73.meridian.modules.merchantcore.application.request;

/**
 * Input data for searching merchants.
 * searchText is optional — if blank or null, all merchants are returned.
 * When provided, it is matched against legalName and displayName
 * using a case-insensitive contains check.
 */
public class SearchMerchantsRequest {

    private String searchText;

    public SearchMerchantsRequest() {}

    public SearchMerchantsRequest(String searchText) {
        this.searchText = searchText;
    }

    public String getSearchText() { return searchText; }
    public void setSearchText(String searchText) { this.searchText = searchText; }
}
