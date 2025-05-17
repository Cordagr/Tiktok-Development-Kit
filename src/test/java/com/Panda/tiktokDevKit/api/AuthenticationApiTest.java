package com.Panda.tiktokDevKit.api;

import com.Panda.tiktokDevKit.exception.AuthenticationException;
import com.Panda.tiktokDevKit.util.HttpUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// TODO: Import environment variables // 

class AuthenticationServiceTest {
    
    private AuthenticationService authService;
    private final String CLIENT_KEY = "test_client_key";
    private final String CLIENT_SECRET = "test_client_secret";
    private final String REDIRECT_URI = "https://test-redirect.com";
    
    @BeforeEach
    void setUp() {
        authService = new AuthenticationService(CLIENT_KEY, CLIENT_SECRET, REDIRECT_URI);
    }
    
    @Test
    void testGenerateAuthorizationUrl() {
        String[] scope = {"user.info.basic", "video.list"};
        String state = "test_state";
        
        String url = authService.generateAuthorizationUrl(scope, state);
        
        assertTrue(url.contains("client_key=" + CLIENT_KEY));
        assertTrue(url.contains("redirect_uri=" + REDIRECT_URI));
        assertTrue(url.contains("scope=user.info.basic,video.list"));
        assertTrue(url.contains("state=" + state));
    }
    
    @Test
    void testExchangeCodeForToken() throws Exception {
        String code = "test_code";
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("access_token", "test_access_token");
        mockResponse.put("refresh_token", "test_refresh_token");
        mockResponse.put("expires_in", 3600);
        
        try (MockedStatic<HttpUtils> mockedStatic = mockStatic(HttpUtils.class)) {
            mockedStatic.when(() -> HttpUtils.post(anyString(), anyMap()))
                .thenReturn(mockResponse);
            
            authService.exchangeCodeForToken(code);
            
            // Verify token was stored correctly
            Field accessTokenField = AuthenticationService.class.getDeclaredField("accessToken");
            accessTokenField.setAccessible(true);
            assertEquals("test_access_token", accessTokenField.get(authService));
            
            Field refreshTokenField = AuthenticationService.class.getDeclaredField("refreshToken");
            refreshTokenField.setAccessible(true);
            assertEquals("test_refresh_token", refreshTokenField.get(authService));
        }
    }
    
    @Test
    void testExchangeCodeForTokenFailure() throws Exception {
        String code = "invalid_code";
        Map<String, Object> mockResponse = new HashMap<>();
        // Empty response to simulate failure
        
        try (MockedStatic<HttpUtils> mockedStatic = mockStatic(HttpUtils.class)) {
            mockedStatic.when(() -> HttpUtils.post(anyString(), anyMap()))
                .thenReturn(mockResponse);
            
            assertThrows(AuthenticationException.class, () -> 
                authService.exchangeCodeForToken(code));
        }
    }
    
    @Test
    void testRefreshAccessToken() throws Exception {
        // Set up initial token state
        Field refreshTokenField = AuthenticationService.class.getDeclaredField("refreshToken");
        refreshTokenField.setAccessible(true);
        refreshTokenField.set(authService, "initial_refresh_token");
        
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("access_token", "new_access_token");
        mockResponse.put("refresh_token", "new_refresh_token");
        mockResponse.put("expires_in", 7200);
        
        try (MockedStatic<HttpUtils> mockedStatic = mockStatic(HttpUtils.class)) {
            mockedStatic.when(() -> HttpUtils.post(anyString(), anyMap()))
                .thenReturn(mockResponse);
            
            authService.refreshAccessToken();
            
            // Verify tokens were updated
            Field accessTokenField = AuthenticationService.class.getDeclaredField("accessToken");
            accessTokenField.setAccessible(true);
            assertEquals("new_access_token", accessTokenField.get(authService));
            assertEquals("new_refresh_token", refreshTokenField.get(authService));
        }
    }
    
    @Test
    void testIsTokenExpired() throws Exception {
        // Test with null access token
        assertTrue(authService.isTokenExpired());
        
        // Set up token with expired time
        Field accessTokenField = AuthenticationService.class.getDeclaredField("accessToken");
        accessTokenField.setAccessible(true);
        accessTokenField.set(authService, "test_token");
        
        Field expiresInField = AuthenticationService.class.getDeclaredField("expiresIn");
        expiresInField.setAccessible(true);
        expiresInField.set(authService, 3600L);
        
        Field tokenTimeField = AuthenticationService.class.getDeclaredField("tokenCreationTime");
        tokenTimeField.setAccessible(true);
        
        // Set creation time to now
        long currentTime = System.currentTimeMillis() / 1000;
        tokenTimeField.set(authService, currentTime);
        
        // Should not be expired
        assertFalse(authService.isTokenExpired());
        
        // Set creation time to expired (more than expiresIn seconds ago)
        tokenTimeField.set(authService, currentTime - 3601);
        assertTrue(authService.isTokenExpired());
    }
    
    @Test
    void testGetAccessToken() throws Exception {
        // Case 1: Token exists and is not expired
        Field accessTokenField = AuthenticationService.class.getDeclaredField("accessToken");
        accessTokenField.setAccessible(true);
        accessTokenField.set(authService, "valid_token");
        
        Field expiresInField = AuthenticationService.class.getDeclaredField("expiresIn");
        expiresInField.setAccessible(true);
        expiresInField.set(authService, 3600L);
        
        Field tokenTimeField = AuthenticationService.class.getDeclaredField("tokenCreationTime");
        tokenTimeField.setAccessible(true);
        tokenTimeField.set(authService, System.currentTimeMillis() / 1000);
        
        assertEquals("valid_token", authService.getAccessToken());
        
        // Case 2: Token is expired but refresh token exists
        Field refreshTokenField = AuthenticationService.class.getDeclaredField("refreshToken");
        refreshTokenField.setAccessible(true);
        refreshTokenField.set(authService, "refresh_token");
        
        tokenTimeField.set(authService, 0L); // Set to expired
        
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("access_token", "refreshed_token");
        mockResponse.put("refresh_token", "new_refresh_token");
        mockResponse.put("expires_in", 7200L);
        
        try (MockedStatic<HttpUtils> mockedStatic = mockStatic(HttpUtils.class)) {
            mockedStatic.when(() -> HttpUtils.post(anyString(), anyMap()))
                .thenReturn(mockResponse);
            
            assertEquals("refreshed_token", authService.getAccessToken());
        }
    }
    
    @Test
    void testRevokeAccessToken() throws Exception {
        // Set up an access token
        Field accessTokenField = AuthenticationService.class.getDeclaredField("accessToken");
        accessTokenField.setAccessible(true);
        accessTokenField.set(authService, "token_to_revoke");
        
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("data", "ok");
        
        try (MockedStatic<HttpUtils> mockedStatic = mockStatic(HttpUtils.class)) {
            mockedStatic.when(() -> HttpUtils.post(anyString(), anyMap()))
                .thenReturn(mockResponse);
            
            assertTrue(authService.revokeAccessToken());
            
            // Verify token was cleared
            assertNull(accessTokenField.get(authService));
        }
    }
}