package com.Panda.tiktokDevKit;

import com.Panda.tiktokDevKit.api.AuthenticationService;
import com.Panda.tiktokDevKit.exception.AuthenticationException;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TiktokMain {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        String clientKey = dotenv.get("CLIENT_KEY");
        String clientSecret = dotenv.get("CLIENT_SECRET");
        String redirectUri = dotenv.get("REDIRECT_URI");
        
        AuthenticationService authService = new AuthenticationService(clientKey, clientSecret, redirectUri);
        
        String[] scopes = {"user.info.basic", "video.list"};
        String state = "random_state_string_" + System.currentTimeMillis();
        
        try {
            String authUrl = authService.generateAuthorizationUrl(scopes, state);
            System.out.println("Authorization URL:");
            System.out.println(authUrl);
            System.out.println("Enter the authorization code from the redirect URL:");
            
            String authCode;
            if (args.length > 0) {
                authCode = args[0];
                System.out.println("Using authorization code from command line argument");
            } else {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    authCode = reader.readLine();
                    if (authCode != null) {
                        authCode = authCode.trim();
                    }
                } catch (IOException e) {
                    System.err.println("Unable to read from console. Please pass the authorization code as a command line argument.");
                    System.err.println("Usage: gradle run --args=\"YOUR_AUTHORIZATION_CODE\"");
                    return;
                }
            }
            
            if (authCode == null || authCode.isEmpty() || authCode.equals("AUTHORIZATION_CODE_FROM_REDIRECT")) {
                System.err.println("Please provide a valid authorization code.");
                System.err.println("Either run: gradle run --args=\"YOUR_AUTHORIZATION_CODE\"");
                System.err.println("Or provide it when prompted.");
                return;
            }
            
            authService.exchangeCodeForToken(authCode);
            
            String accessToken = authService.getAccessToken();
            System.out.println("Access Token: " + accessToken);
            System.out.println("Refresh Token: " + authService.getRefreshToken());
            System.out.println("Expires In: " + authService.getExpiresIn() + " seconds");
            
            boolean isExpired = authService.isTokenExpired();
            System.out.println("Is token expired: " + isExpired);
            
            if (isExpired) {
                authService.refreshAccessToken();
                System.out.println("Token refreshed. New Access Token: " + authService.getAccessToken());
            }
            
        } catch (AuthenticationException e) {
            System.err.println("Authentication Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}