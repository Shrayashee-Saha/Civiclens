package com.example.civiclens.sync;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Talks to the CivicLens report endpoints. Call from a background thread only. */
public final class ReportApi {

    private ReportApi() { }

    public static JSONObject submit(String baseUrl, JSONObject report) throws IOException, JSONException {
        return request("POST", baseUrl + "/reports", report.toString());
    }

    public static JSONObject status(String baseUrl, String serverId) throws IOException, JSONException {
        return request("GET", baseUrl + "/reports/" + serverId, null);
    }

    private static JSONObject request(String method, String urlString, String body)
            throws IOException, JSONException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        try {
            connection.setRequestMethod(method);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(20000);
            connection.setRequestProperty("Accept", "application/json");
            if (body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }
            int code = connection.getResponseCode();
            InputStream stream = code >= 200 && code < 300
                    ? connection.getInputStream() : connection.getErrorStream();
            String response = read(stream);
            if (code < 200 || code >= 300) {
                throw new IOException("Server returned HTTP " + code);
            }
            return new JSONObject(response);
        } finally {
            connection.disconnect();
        }
    }

    private static String read(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
