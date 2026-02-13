package com.assignmente.researchtool.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Service
public class AIService {

    private final String API_KEY = System.getenv("GROQ_API_KEY");


    public String extractFinancialData(String text) {
        try {
            URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "llama-3.3-70b-versatile");

            JSONArray messages = new JSONArray();

            JSONObject sys = new JSONObject();
            sys.put("role", "system");
            sys.put("content",
                    "You are a financial data extraction AI.\n" +
                            "Extract revenue and profit year wise from text.\n" +
                            "Return ONLY valid JSON array.\n" +
                            "Strict JSON format example:\n" +
                            "[{\"Year\":\"2022\",\"Revenue\":\"5000\",\"Profit\":\"1400\"}," +
                            "{\"Year\":\"2023\",\"Revenue\":\"6500\",\"Profit\":\"2100\"}]\n" +
                            "Rules:\n" +
                            "1. Use double quotes only\n" +
                            "2. Add comma between fields\n" +
                            "3. No explanation\n" +
                            "4. No text outside JSON\n" +
                            "5. Only JSON output"
            );

            messages.put(sys);

            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content", text);
            messages.put(user);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0);

            OutputStream os = conn.getOutputStream();
            os.write(requestBody.toString().getBytes());
            os.flush();
            os.close();

            Scanner sc;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300)
                sc = new Scanner(conn.getInputStream());
            else
                sc = new Scanner(conn.getErrorStream());

            StringBuilder response = new StringBuilder();
            while (sc.hasNext()) response.append(sc.nextLine());

            sc.close();

            JSONObject json = new JSONObject(response.toString());
            String aiText = json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            return cleanJson(aiText);

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    private String cleanJson(String aiText) {

        // remove markdown
        aiText = aiText.replace("```json", "")
                .replace("```", "")
                .trim();

        // remove tabs/newlines
        aiText = aiText.replace("\n", "")
                .replace("\r", "")
                .replace("\t", ",");

        // fix missing quotes on keys
        aiText = aiText.replace("Revenue:", "\"Revenue\":\"");
        aiText = aiText.replace("Profit:", "\"Profit\":\"");
        aiText = aiText.replace("Year:", "\"Year\":\"");

        // fix missing commas between fields
        aiText = aiText.replace("\"2022\" \"Revenue\"", "\"2022\",\"Revenue\"");
        aiText = aiText.replace("\"2023\" \"Revenue\"", "\"2023\",\"Revenue\"");

        // fix object separation
        aiText = aiText.replace("} {", "},{");

        // ensure array format
        if (!aiText.startsWith("[")) aiText = "[" + aiText;
        if (!aiText.endsWith("]")) aiText = aiText + "]";

        return aiText;
    }


}
