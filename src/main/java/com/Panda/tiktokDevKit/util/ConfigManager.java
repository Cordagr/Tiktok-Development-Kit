package com.Panda.tiktokDevKit.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
   private final String clientKey;
   private final String clientSecret;
   private final String apiBaseUrl;
   private final String redirectUri;
   private final Properties properties;
   
   public ConfigManager(String clientKey, String clientSecret) {
       this.clientKey = clientKey;
       this.clientSecret = clientSecret;
       this.properties = loadProperties();
       this.apiBaseUrl = getProperty("api.base.url", "https://open-api.tiktok.com/v2/");
       this.redirectUri = getProperty("redirect.uri", "");
   }
   
   public ConfigManager(String configFilePath, String clientKey, String clientSecret) {
       this.clientKey = clientKey;
       this.clientSecret = clientSecret;
       this.properties = loadProperties(configFilePath);
       this.apiBaseUrl = getProperty("api.base.url", "https://open-api.tiktok.com/v2/");
       this.redirectUri = getProperty("redirect.uri", "");
   }
   
   private Properties loadProperties() {
       Properties props = new Properties();
       try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
           if (input != null) {
               props.load(input);
           }
       } catch (IOException ex) {
           // Silently continue with default values
       }
       return props;
   }
   
   private Properties loadProperties(String filePath) {
       Properties props = new Properties();
       try (InputStream input = new FileInputStream(filePath)) {
           props.load(input);
       } catch (IOException ex) {
           // Silently continue with default values
       }
       return props;
   }
   
   private String getProperty(String key, String defaultValue) {
       String value = properties.getProperty(key);
       return (value != null && !value.isEmpty()) ? value : defaultValue;
   }
   
   public String getClientKey() {
       return clientKey;
   }
   
   public String getClientSecret() {
       return clientSecret;
   }
   
   public String getApiBaseUrl() {
       return apiBaseUrl;
   }
   
   public String getRedirectUri() {
       return redirectUri;
   }
   
   public String getProperty(String key) {
       return properties.getProperty(key);
   }
   
   public int getIntProperty(String key, int defaultValue) {
       String value = properties.getProperty(key);
       if (value != null && !value.isEmpty()) {
           try {
               return Integer.parseInt(value);
           } catch (NumberFormatException e) {
               return defaultValue;
           }
       }
       return defaultValue;
   }
   
   public boolean getBooleanProperty(String key, boolean defaultValue) {
       String value = properties.getProperty(key);
       if (value != null && !value.isEmpty()) {
           return Boolean.parseBoolean(value);
       }
       return defaultValue;
   }
}