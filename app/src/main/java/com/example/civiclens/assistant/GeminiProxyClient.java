package com.example.civiclens.assistant;

import com.example.civiclens.ai.JsonText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Sends the grounded prompt to the Gemini API or backend proxy server.
 * Call this from a background thread.
 */
public final class GeminiProxyClient {

    private GeminiProxyClient() { }

    public static String ask(String baseUrl, String prompt) throws IOException {
        try {
            URL url = new URL(baseUrl + "/ask");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            byte[] body = ("{\"prompt\":" + JsonText.quote(prompt) + "}")
                    .getBytes(StandardCharsets.UTF_8);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(body);
            }

            int code = connection.getResponseCode();
            InputStream stream = code >= 200 && code < 300
                    ? connection.getInputStream() : connection.getErrorStream();
            String response = read(stream);
            connection.disconnect();

            if (code != 200) {
                throw new IOException("Assistant returned HTTP " + code);
            }
            return new JSONObject(response).getString("text");
        } catch (org.json.JSONException e) {
            throw new IOException("Unexpected response from assistant", e);
        }
    }

    public static String askDirect(String apiKey, String prompt) throws IOException {
        String[] models = {"gemini-3.0-flash", "gemini-3.5-flash", "gemini-flash-latest"};
        IOException lastException = null;

        for (String model : models) {
            try {
                return callGeminiEndpoint("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey, prompt);
            } catch (IOException e) {
                lastException = e;
            }
        }
        throw lastException != null ? lastException : new IOException("Failed to call Gemini API");
    }

    private static String callGeminiEndpoint(String endpointUrl, String prompt) throws IOException {
        try {
            URL url = new URL(endpointUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            JSONObject payload = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);
            parts.put(textPart);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            payload.put("contents", contents);

            byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(body);
            }

            int code = connection.getResponseCode();
            InputStream stream = code >= 200 && code < 300
                    ? connection.getInputStream() : connection.getErrorStream();
            String response = read(stream);
            connection.disconnect();

            if (code != 200) {
                throw new IOException("Gemini API returned HTTP " + code);
            }

            JSONObject root = new JSONObject(response);
            JSONArray candidates = root.getJSONArray("candidates");
            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject candContent = firstCandidate.getJSONObject("content");
            JSONArray candParts = candContent.getJSONArray("parts");
            return candParts.getJSONObject(0).getString("text");
        } catch (org.json.JSONException e) {
            throw new IOException("Unexpected response from Gemini API", e);
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
