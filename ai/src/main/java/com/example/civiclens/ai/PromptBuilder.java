package com.example.civiclens.ai;

import java.util.List;

/**
 * Builds the grounded prompt sent to Gemini. The model is told to answer only from the records
 * supplied here, to mark unverified data, and to avoid claiming to be an official.
 */
public final class PromptBuilder {

    private PromptBuilder() { }

    public static String build(String question, List<CatalogEntry> entries, String language) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are the citizen information assistant of CivicLens BD. Follow these rules.\n");
        sb.append("1. Answer ONLY from the verified records below. If they do not answer the question, ")
          .append("say the information is not available here and point to the official source.\n");
        sb.append("2. Never invent fees, deadlines, phone numbers, addresses, or legal requirements.\n");
        sb.append("3. You are an information assistant. You are not a government official or a legal authority.\n");
        sb.append("4. Records marked DEMO must be described as unverified sample data.\n");
        sb.append("5. Finish with the official source link for each service you mention.\n");
        sb.append("6. Reply in ").append(language).append(".\n\n");

        sb.append("VERIFIED RECORDS:\n");
        if (entries == null || entries.isEmpty()) {
            sb.append("(no matching records)\n");
        } else {
            for (CatalogEntry e : entries) {
                sb.append("- Service: ").append(e.name)
                  .append(" | Authority: ").append(e.authority)
                  .append(" | Status: ").append(e.demo ? "DEMO, NOT VERIFIED" : "verified on " + e.lastVerified)
                  .append(" | Source: ").append(e.sourceUrl)
                  .append(" | Required documents: ").append(join(e.mandatoryDocuments))
                  .append(" | Conditional documents: ").append(join(e.conditionalDocuments))
                  .append(" | Summary: ").append(e.description)
                  .append('\n');
            }
        }

        sb.append("\nCITIZEN QUESTION:\n").append(question == null ? "" : question.trim()).append('\n');
        return sb.toString();
    }

    private static String join(List<String> items) {
        if (items.isEmpty()) {
            return "none listed";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(items.get(i));
        }
        return sb.toString();
    }
}
