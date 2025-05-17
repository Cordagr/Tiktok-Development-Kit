package com.Panda.tiktokDevKit.util;

import java.util.*;
import java.net.*;
import java.io.*;
import com.google.gson.Gson;

public class HttpUtils {

    private static final Gson gson = new Gson();

    // send JSON POST with headers and body
    public static Map<String, Object> post(String endpoint, Map<String, String> headers, Map<String, Object> data) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        String jsonData = gson.toJson(data);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonData.getBytes("UTF-8"));
        }

        return readResponse(conn);
    }

    // helper for JSON POST without body
    public static Map<String, Object> post(String endpoint, Map<String, String> headers) throws IOException {
        return post(endpoint, headers, new HashMap<>());
    }


    public static Map<String, Object> postForm(String endpoint, Map<String, String> params) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        StringBuilder postData = new StringBuilder();
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=').append(URLEncoder.encode(param.getValue(), "UTF-8"));
            }
        }

        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.writeBytes(postData.toString());
            wr.flush();
        }

        return readResponse(conn);
    }

    public static Map<String, Object> get(String endpoint, Map<String, String> headers, Map<String, String> params) throws IOException {
        if (params != null && !params.isEmpty()) {
            StringBuilder queryString = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> param : params.entrySet()) {
                queryString.append(first ? '?' : '&');
                first = false;
                queryString.append(URLEncoder.encode(param.getKey(), "UTF-8"))
                           .append('=')
                           .append(URLEncoder.encode(param.getValue(), "UTF-8"));
            }
            endpoint += queryString.toString();
        }

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        return readResponse(conn);
    }

    public static Map<String, Object> delete(String endpoint, Map<String, String> headers, Map<String, String> params) throws IOException {
        if (params != null && !params.isEmpty()) {
            StringBuilder queryString = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> param : params.entrySet()) {
                queryString.append(first ? '?' : '&');
                first = false;
                queryString.append(URLEncoder.encode(param.getKey(), "UTF-8"))
                           .append('=')
                           .append(URLEncoder.encode(param.getValue(), "UTF-8"));
            }
            endpoint += queryString.toString();
        }

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        return readResponse(conn);
    }

    private static Map<String, Object> readResponse(HttpURLConnection conn) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                conn.getResponseCode() == HttpURLConnection.HTTP_OK ?
                        conn.getInputStream() : conn.getErrorStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        return gson.fromJson(response.toString(), Map.class);
    }

    public static Map<String,Object> uploadChunk(String uploadUrl, Map<String, String> headers, byte[] chunkData) throws IOException {
        URL url = new URL(uploadUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/octet-stream");

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        try (OutputStream os = conn.getOutputStream()) {
            os.write(chunkData);
        }

        return readResponse(conn);
    }
}
 