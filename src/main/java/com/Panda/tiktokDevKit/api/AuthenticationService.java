package com.Panda.tiktokDevKit.api;
import com.Panda.tiktokDevKit.exception.AuthenticationException;
import java.util.HashMap;
import java.util.Map; 
import com.Panda.tiktokDevKit.util.HttpUtils;

// Handles Tiktokauthentication flows //
public class AuthenticationService {
    private String clientKey; 
    private String clientSecret;
    private String redirectUri;

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private long tokenCreationTime; 

    private HttpUtils httpUtils = new HttpUtils();

    
    public AuthenticationService(String clientKey, String clientSecret, String redirectUri) {
        this.clientKey = clientKey;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.httpUtils = new HttpUtils();
    }

    public AuthenticationService(String clientKey, String clientSecret, String accessToken, String refreshToken, long expiresIn) {
        this.clientKey = clientKey;
        this.clientSecret = clientSecret;
        this.httpUtils = new HttpUtils();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.tokenCreationTime = System.currentTimeMillis() / 1000;
    }

    // Generating authorization URL for user to authorize the application //
    public String generateAuthorizationUrl(String[] scope, String state) {
        StringBuilder urlBuilder = new StringBuilder("https://www.tiktok.com/v2/auth/authorize/");
        urlBuilder.append("?client_key=").append(clientKey);
        urlBuilder.append("&response_type=code");
        urlBuilder.append("&redirect_uri=").append(redirectUri); // Fixed missing equals sign
        urlBuilder.append("&scope=").append(String.join(",", scope));

        if (state != null && !state.isEmpty()) {
            urlBuilder.append("&state=").append(state);
        }
        return urlBuilder.toString(); 
    }


    public void exchangeCodeForToken(String code) throws AuthenticationException {
        String endpoint = "https://open-api.tiktok.com/oauth/access_token/";
        
        Map<String, String> params = new HashMap<>();
        params.put("client_key", clientKey);
        params.put("client_secret", clientSecret);
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", redirectUri);
        
        try {
            Map<String, Object> response = httpUtils.post(endpoint, params);

            // Store the tokens from the response
            if (response != null && response.containsKey("access_token")) {
                this.accessToken = (String) response.get("access_token");
                this.refreshToken = (String) response.get("refresh_token");
                this.expiresIn = Long.parseLong(response.get("expires_in").toString());
                this.tokenCreationTime = System.currentTimeMillis() / 1000;
            } else {
                throw new AuthenticationException("Failed to retrieve access token");
            }
        } catch (Exception e) {
            throw new AuthenticationException("Authentication failed: " + e.getMessage(), e);
        }
    }


    public void refreshAccessToken() throws AuthenticationException {
       if (refreshToken == null || refreshToken.isEmpty()) {
           throw new AuthenticationException("No refresh token available");
       }
       
       String endpoint = "https://open-api.tiktok.com/oauth/refresh_token/";
       
       Map<String, String> params = new HashMap<>();
       params.put("client_key", clientKey);
       params.put("grant_type", "refresh_token");
       params.put("refresh_token", refreshToken);
       
       try {
           Map<String, Object> response = httpUtils.post(endpoint, params);
           
           // Update tokens from the response
           if (response != null && response.containsKey("access_token")) {
               this.accessToken = (String) response.get("access_token");
               this.refreshToken = (String) response.get("refresh_token");
               this.expiresIn = Long.parseLong(response.get("expires_in").toString());
               this.tokenCreationTime = System.currentTimeMillis() / 1000;
           } else {
               throw new AuthenticationException("Failed to refresh access token");
           }
       } catch (Exception e) {
           throw new AuthenticationException("Token refresh failed: " + e.getMessage(), e);
       }
   }
   
   /**
    * Check if the current access token is expired.
    * 
    * @return true if the token is expired, false otherwise
    */
   public boolean isTokenExpired() {
       if (accessToken == null || expiresIn == 0) {
           return true;
       }
       
       long currentTime = System.currentTimeMillis() / 1000;
       return (currentTime - tokenCreationTime) >= expiresIn;
   }
   
   /**
    * Get the current access token, refreshing if necessary.
    * 
    * @return The current valid access token
    * @throws AuthenticationException if token refresh fails
    */
   public String getAccessToken() throws AuthenticationException {
       if (isTokenExpired() && refreshToken != null) {
           refreshAccessToken();
       }
       return accessToken;
   }
   
   /**
    * Get the refresh token.
    * 
    * @return The refresh token
    */
   public String getRefreshToken() {
       return refreshToken;
   }
   
   /**
    * Get the token expiration time in seconds.
    * 
    * @return The token expiration time
    */
   public long getExpiresIn() {
       return expiresIn;
   }
   
   /**
    * Revoke the current access token.
    * 
    * @return true if revocation was successful
    * @throws AuthenticationException if revocation fails
    */
   public boolean revokeAccessToken() throws AuthenticationException {
       if (accessToken == null) {
           return false;
       }
       
       String endpoint = "https://open-api.tiktok.com/oauth/revoke/";
       
       Map<String, String> params = new HashMap<>();
       params.put("client_key", clientKey);
       params.put("token", accessToken);
       
       try {
           Map<String, Object> response = httpUtils.post(endpoint, params);
           
           // Check if revocation was successful
           boolean success = response != null && "ok".equals(response.get("data"));
           
           if (success) {
               // Clear token data
               this.accessToken = null;
               this.refreshToken = null;
               this.expiresIn = 0;
           }
           
           return success;
       } catch (Exception e) {
           throw new AuthenticationException("Token revocation failed: " + e.getMessage(), e);
       }
   }
}