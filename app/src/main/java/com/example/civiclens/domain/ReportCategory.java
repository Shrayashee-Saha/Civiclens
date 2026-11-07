package com.example.civiclens.domain;

/**
 * Problem categories. Keys are stored in the database. Labels come from strings.xml, in the
 * same order as KEYS. ROUTING_TERMS are used to find the office that handles each category.
 */
public final class ReportCategory {

    public static final String[] KEYS = {"ROAD", "STREETLIGHT", "DRAINAGE", "WASTE", "FACILITY"};

    private static final String[] ROUTING_TERMS = {
            "road pothole repair",
            "streetlight light lamp",
            "drainage waterlogging",
            "waste garbage",
            "facility public toilet"
    };

    private ReportCategory() { }

    public static int indexOf(String key) {
        for (int i = 0; i < KEYS.length; i++) {
            if (KEYS[i].equals(key)) {
                return i;
            }
        }
        return 0;
    }

    public static String routingTerms(String key) {
        return ROUTING_TERMS[indexOf(key)];
    }
}
