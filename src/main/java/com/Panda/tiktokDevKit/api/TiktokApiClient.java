package com.Panda.tiktokDevKit.api;
import com.Panda.tiktokDevKit.util.ConfigManager;
import com.Panda.tiktokDevKit.util.HttpUtils;
import java.util.HashMap; 
import java.util.Map; 
import com.Panda.tiktokDevKit.exception.TiktokApiException;
import com.Panda.tiktokDevKit.model.TiktokVideo;

public class TiktokApiClient {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private static final String BASE_URL = "https://open.tiktokapis.com/v2/"; // base URL for TikTok API
    private final String apiBaseUrl;
    private final String clientKey;
    private final String clientSecret;
    private final HttpUtils httpUtils;; 

    

    public TiktokApiClient(ConfigManager configManager) {
        this.apiBaseUrl = configManager.getApiBaseUrl();
        this.clientKey = configManager.getClientKey();
        this.clientSecret = configManager.getClientSecret();
        this.httpUtils = new HttpUtils();
    }



     public Map<String, Object> get(String endpoint, String accessToken, Map<String, String> params) throws TiktokApiException {
        try {
            String url = buildUrl(endpoint);
            Map<String, String> headers = buildHeaders(accessToken);
            
            // Add client key to params if it's not already there
            if (params == null) {
                params = new HashMap<>();
            }
            if (!params.containsKey("client_key")) {
                params.put("client_key", clientKey);
            }
            
            Map<String, Object> response = httpUtils.get(url, headers, params);
            validateResponse(response);
            return response;
        } catch (Exception e) {
            throw new TiktokApiException("GET request failed: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> post(String endpoint, String accessToken, Map<String, Object> data) throws TiktokApiException {
        try {
            String url = buildUrl(endpoint);
            Map<String, String> headers = buildHeaders(accessToken);
            
            // Add client key to data if it's not already there
            if (data == null) {
                data = new HashMap<>();
            }
            if (!data.containsKey("client_key")) {
                data.put("client_key", clientKey);
            }
            
            Map<String, Object> response = httpUtils.post(url, headers, data);
            validateResponse(response);
            return response;
        } catch (Exception e) {
            throw new TiktokApiException("POST request failed: " + e.getMessage(), e);
        }
    }


    private Map<String, String> buildHeaders(String accessToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        
        if (accessToken != null && !accessToken.isEmpty()) {
            headers.put("Authorization", "Bearer " + accessToken);
        }
        
        return headers;
    }
    
    private void validateResponse(Map<String, Object> response) throws TiktokApiException {
        if (response == null) {
            throw new TiktokApiException("Null response from API");
        }
        
        // Check for error field in response
        if (response.containsKey("error")) {
            Map<String, Object> error = response.get("error") instanceof Map ? (Map<String, Object>) response.get("error") : new HashMap<>();
            String code = error.containsKey("code") ? error.get("code").toString() : "unknown";
            String message = error.containsKey("message") ? error.get("message").toString() : "Unknown error";
            int codeString = Integer.valueOf(code);
            throw new TiktokApiException(codeString, message);
        }
        
        // Check status code if present
        if (response.containsKey("status_code")) {
            int statusCode = ((Number) response.get("status_code")).intValue();
            if (statusCode != 0 && statusCode != 200) {
                String message = response.containsKey("status_msg") ? 
                                response.get("status_msg").toString() : "API error";
                    
                throw new TiktokApiException((statusCode),message);
            }
        }
    }


    private String buildUrl(String endpoint) {
        // If the endpoint already starts with http, assume it's a full URL
        if (endpoint.startsWith("http")) {
            return endpoint;
        }
        
        // append it to the base URL otherwise
        if (endpoint.startsWith("/")) {
            return apiBaseUrl + endpoint.substring(1);
        } else {
            return apiBaseUrl + endpoint;
        }
    }
    

}