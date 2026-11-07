package com.example.civiclens.ai;

import java.util.Collections;
import java.util.List;

/** One verified (or clearly marked demo) service record, passed to the assistant as context. */
public final class CatalogEntry {

    public final String name;
    public final String authority;
    public final String description;
    public final String sourceUrl;
    public final String lastVerified;
    public final boolean demo;
    public final List<String> mandatoryDocuments;
    public final List<String> conditionalDocuments;

    public CatalogEntry(String name, String authority, String description, String sourceUrl,
                        String lastVerified, boolean demo,
                        List<String> mandatoryDocuments, List<String> conditionalDocuments) {
        this.name = name;
        this.authority = authority;
        this.description = description;
        this.sourceUrl = sourceUrl;
        this.lastVerified = lastVerified;
        this.demo = demo;
        this.mandatoryDocuments = mandatoryDocuments == null
                ? Collections.<String>emptyList() : mandatoryDocuments;
        this.conditionalDocuments = conditionalDocuments == null
                ? Collections.<String>emptyList() : conditionalDocuments;
    }
}
