
package com.genai.email.service;

import com.genai.email.model.EmailResponseDTO;
import org.apache.tika.Tika;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailService {

    private final String HUGGINGFACE_API_TOKEN = "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";
    private final String HUGGINGFACE_API_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-mnli";
    private final String HUGGINGFACE_LLM_URL = "https://api-inference.huggingface.co/models/google/flan-t5-base";

    public EmailResponseDTO processFile(MultipartFile file) throws Exception {
        Tika tika = new Tika();
        String content = tika.parseToString(file.getInputStream());

        // Candidate label lists
        List<String> requestTypes = Arrays.asList(
                "Loan Repayment", "Loan Drawdown", "Facility Request", "Rate Inquiry", "Prepayment", "Loan Extension"
        );

        List<String> subRequestTypes = Arrays.asList(
                "SOFR Payment", "Term Loan Payment", "Principal Repayment", "Interest Rate Reset", "Balance Inquiry"
        );

        // Classify using HuggingFace
        String requestType = classifyZeroShot(content, requestTypes);
        String subRequestType = classifyZeroShot(content, subRequestTypes);

        // Extract fields using regex with LLM fallback
        Map<String, String> fields = extractFieldsWithRegexAndFallback(content);

        // Build response DTO
        EmailResponseDTO dto = new EmailResponseDTO();
        dto.setRequestType(requestType);
        dto.setSubRequestType(subRequestType);
        dto.setPrimaryIntent(requestType + " - " + subRequestType);
        dto.setDuplicate(false);
        dto.setExtractedFields(fields);

        return dto;
    }

    private String classifyZeroShot(String text, List<String> labels) throws Exception {
        JSONObject body = new JSONObject();
        body.put("inputs", text);

        JSONObject parameters = new JSONObject();
        parameters.put("candidate_labels", new JSONArray(labels));
        body.put("parameters", parameters);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HUGGINGFACE_API_URL))
                .header("Authorization", "Bearer " + HUGGINGFACE_API_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String bodyStr = response.body().trim();
        System.out.println("Zero-shot raw response: " + bodyStr); // Debug log

        JSONObject jsonResponse = new JSONObject(bodyStr);

        if (!jsonResponse.has("labels")) {
            System.err.println("Error: Missing 'labels' in HuggingFace response: " + bodyStr);
            return "Unknown"; // fallback label
        }

        return jsonResponse.getJSONArray("labels").getString(0); // Top prediction
    }

    private Map<String, String> extractFieldsWithRegexAndFallback(String content) throws Exception {
        Map<String, String> fields = new HashMap<>();

        // Regex for amount
        Pattern amountPattern = Pattern.compile("USD\\s[\\d,]+\\.\\d{2}");
        Matcher amtMatcher = amountPattern.matcher(content);
        if (amtMatcher.find()) {
            fields.put("amount", amtMatcher.group());
        }

        // Regex for dealId (generalized)
        Pattern dealIdPattern = Pattern.compile("(?i)(deal id|reference|ref)[:\\s]*([\\w\\s\\-\\d]+)");
        Matcher dealMatcher = dealIdPattern.matcher(content);
        if (dealMatcher.find()) {
            fields.put("dealId", dealMatcher.group(2).trim());
        }

        // Fallback to LLM if anything is missing
        if (!fields.containsKey("amount") || !fields.containsKey("dealId")) {
            fields.putAll(extractFieldsWithLLM(content));
        }

        return fields;
    }

    private Map<String, String> extractFieldsWithLLM(String content) throws Exception {
        JSONObject body = new JSONObject();
        String prompt = "From the below message, extract:\n" +
                "- Amount (in USD)\n" +
                "- Deal ID\n" +
                "Return the result exactly as:\nAmount: <value>; Deal ID: <value>\n\nMessage:\n" + content;

        body.put("inputs", prompt);
        body.put("parameters", new JSONObject());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HUGGINGFACE_LLM_URL))
                .header("Authorization", "Bearer " + HUGGINGFACE_API_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String bodyStr = response.body().trim();
        String output;

        try {
            JSONArray arr = new JSONArray(bodyStr);
            output = arr.getJSONObject(0).getString("generated_text");
        } catch (Exception ex) {
            JSONObject obj = new JSONObject(bodyStr);
            output = obj.optString("generated_text", bodyStr);
        }

        System.out.println("LLM Output: " + output); // debug log

        Map<String, String> fields = new HashMap<>();
        String[] tokens = output.split(";|\n");
        for (String token : tokens) {
            if (token.toLowerCase().contains("amount") && token.contains(":")) {
                fields.put("amount", token.split(":", 2)[1].trim());
            } else if (token.toLowerCase().contains("deal") && token.contains(":")) {
                fields.put("dealId", token.split(":", 2)[1].trim());
            }
        }

        return fields;
    }
}
