package com.videogameaholic.intellij.starcoder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.videogameaholic.intellij.starcoder.settings.StarCoderSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class StarCoderService {

    // TODO: SantaCoder uses - rather than _
    private static final String PREFIX_TAG = "<fim_prefix>";
    private static final String SUFFIX_TAG = "<fim_suffix>";
    private static final String MIDDLE_TAG = "<fim_middle>";
    private static final String END_TAG = "<|endoftext|>";

    public String[] getCodeCompletionHints(CharSequence editorContents, int cursorPosition) {
        StarCoderSettings settings = StarCoderSettings.getInstance();
        if(!settings.isSaytEnabled()) return null;

        if(StringUtils.isEmpty(settings.getApiToken())) {
            Notifications.Bus.notify(new Notification("StarCoder","StarCoder", "StarCoder API token is required.", NotificationType.WARNING));
            return null;
        }

        String contents = editorContents.toString();
        if(contents.contains(PREFIX_TAG) || contents.contains(SUFFIX_TAG) || contents.contains(MIDDLE_TAG) || contents.contains(END_TAG)) return null;

        String prefix = contents.substring(0, cursorPosition);
        String suffix = contents.substring(cursorPosition, editorContents.length());
        String starCoderPrompt = generateFIMPrompt(prefix, suffix);

        HttpPost httpPost = buildApiPost(settings, starCoderPrompt);
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(httpPost);
            String[] suggestionList = null;

            // Check the response status code
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                return null;
            }

            Gson gson = new Gson();
            String responseBody = EntityUtils.toString(response.getEntity());
            JsonArray responseArray = gson.fromJson(responseBody, JsonArray.class);
            String generatedText;
            if(responseArray.size()>0) {
                JsonObject responseObject = responseArray.get(0).getAsJsonObject();
                if(responseObject.get("generated_text")!=null) {
                    generatedText = responseObject.get("generated_text").getAsString();
                    if(generatedText.contains(MIDDLE_TAG)) {
                        String[] parts = generatedText.split(MIDDLE_TAG);
                        String suggestion = parts[1].replace(END_TAG, "");
                        suggestionList = StringUtils.splitPreserveAllTokens(suggestion, "\n");
                    }
                }
            }

            httpClient.close();
            return suggestionList;


        } catch (IOException e) {
            // TODO log exception
            return null;
        }
    }

    private String generateFIMPrompt(String prefix, String suffix) {
        return PREFIX_TAG + prefix + SUFFIX_TAG + suffix + MIDDLE_TAG;
    }

    private HttpPost buildApiPost (StarCoderSettings settings, String starCoderPrompt) {
        String apiURL = settings.getApiURL();
        String bearerToken = settings.getApiToken();
        float temperature = settings.getTemperature();
        int maxNewTokens = settings.getMaxNewTokens();
        float topP = settings.getTopP();
        float repetitionPenalty = settings.getRepetitionPenalty();

        HttpPost httpPost = new HttpPost(apiURL);
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
        JsonObject httpBody = new JsonObject();
        httpBody.addProperty("inputs", starCoderPrompt);

        JsonObject parameters = new JsonObject();
        parameters.addProperty("temperature", temperature);
        parameters.addProperty("max_new_tokens", maxNewTokens);
        parameters.addProperty("top_p", topP);
        parameters.addProperty("repetition_penalty", repetitionPenalty);
        httpBody.add("parameters", parameters);

        StringEntity requestEntity = new StringEntity(httpBody.toString(), ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);
        return httpPost;
    }

    public String replacementSuggestion (String prompt) {
        // Default to returning the same text.
        String replacement = prompt;

        // TODO if no API key then stop me from getting here.
        StarCoderSettings settings = StarCoderSettings.getInstance();
        if(StringUtils.isEmpty(settings.getApiToken())) {
            Notifications.Bus.notify(new Notification("StarCoder","StarCoder", "StarCoder API token is required.", NotificationType.WARNING));
            return replacement;
        }

        HttpPost httpPost = buildApiPost(settings, prompt);

        // TODO combine this with above
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(httpPost);

            // Check the response status code
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                return replacement;
            }

            Gson gson = new Gson();
            String responseBody = EntityUtils.toString(response.getEntity());
            JsonArray responseArray = gson.fromJson(responseBody, JsonArray.class);
            String generatedText;
            if(responseArray.size()>0) {
                JsonObject responseObject = responseArray.get(0).getAsJsonObject();
                if(responseObject.get("generated_text")!=null) {
                    generatedText = responseObject.get("generated_text").getAsString();
                    replacement = generatedText.replace(END_TAG, "");
                }
            }

            httpClient.close();


        } catch (IOException e) {
            // TODO log exception
        }

        return replacement;
    }
}
