package com.Panda.tiktokDevKit;
import com.Panda.tiktokDevKit.api.AuthenticationService;
import com.Panda.tiktokDevKit.exception.AuthenticationException;
import io.github.cdimascio.dotenv.Dotenv;

public class TiktokMain {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        String clientKey = dotenv.get("CLIENT_KEY");
        String clientSecret = dotenv.get("CLIENT_SECRET");
        String redirectUri = dotenv.get("REDIRECT_URI");
        
        System.out.println("HERE");
        // initialize the authentication service
        AuthenticationService authService = new AuthenticationService(clientKey, clientSecret, redirectUri);
        
        String[] scopes = {"user.info.basic", "video.list"};
        
        // state parameter for CSRF protection
        String state = "random_state_string";
        
        try {
            String authUrl = authService.generateAuthorizationUrl(scopes, state);
            System.out.println("Please open this URL in your browser to authorize the app:");
            System.out.println(authUrl);
            
       
            
            System.out.println("\nAfter authorization, enter the code received:");
            //TODO: get from your callback endpoint
            String authCode = "AUTHORIZATION_CODE_FROM_REDIRECT";
            
            //  exchange the code for tokens
            authService.exchangeCodeForToken(authCode);
            
            // usable access token for API calls
            String accessToken = authService.getAccessToken();
            System.out.println("Access Token: " + accessToken);
            System.out.println("Refresh Token: " + authService.getRefreshToken());
            System.out.println("Expires In: " + authService.getExpiresIn() + " seconds");
            
            // check if token is expired
            boolean isExpired = authService.isTokenExpired();
            System.out.println("Is token expired? " + isExpired);
            
            // if token is expired, you can refresh it
            if (isExpired) {
                System.out.println("Refreshing token...");
                authService.refreshAccessToken();
                System.out.println("New Access Token: " + authService.getAccessToken());
            }
            
            // revoke the token
            System.out.println("Revoking token...");
            boolean revoked = authService.revokeAccessToken();
            System.out.println("Token revoked: " + revoked);
            
        } catch (AuthenticationException e) {
            System.err.println("Authentication Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}