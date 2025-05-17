package com.Panda.tiktokDevKit.api;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.Panda.tiktokDevKit.exception.AuthenticationException;
import com.Panda.tiktokDevKit.exception.TiktokApiException;
import com.Panda.tiktokDevKit.model.TiktokComment;
import com.Panda.tiktokDevKit.model.TiktokUser;
import com.Panda.tiktokDevKit.model.TiktokVideo;
import com.Panda.tiktokDevKit.util.ConfigManager; 
import com.Panda.tiktokDevKit.util.HttpUtils;


public class ContentService {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private long tokenCreationTime;
    private static final String BASE_URL = "https://open.tiktokapis.com/v2";

    public ContentService(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.tokenCreationTime = System.currentTimeMillis();
    }

    /**
     * Checks if the access token is expired and refreshes if needed
     */
    private void checkTokenExpiration() throws AuthenticationException {
        long currentTime = System.currentTimeMillis();
        long tokenAgeMs = currentTime - tokenCreationTime;
        
        // If token is 80% of its way to expiration, refresh it
        if (tokenAgeMs > (expiresIn * 0.8 * 1000)) {
            refreshAccessToken();
        }
    }
    
    /**
     * Refreshes the access token using the refresh token
     */
    private void refreshAccessToken() throws AuthenticationException {
        String endpoint = BASE_URL + "/oauth/refresh_token/";
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("refresh_token", refreshToken);
        requestBody.put("grant_type", "refresh_token");
        ConfigManager configManager = new ConfigManager("config.properties", "client_key", "client_secret");
        requestBody.put("client_key", configManager.getClientKey());
        requestBody.put("client_secret", configManager.getClientSecret());
        
        try {
            Map<String, Object> response = HttpUtils.post(endpoint, headers, requestBody);
            
            if (response.containsKey("access_token")) {
                this.accessToken = (String) response.get("access_token");
                this.refreshToken = (String) response.get("refresh_token");
                this.expiresIn = (Long) response.get("expires_in");
                this.tokenCreationTime = System.currentTimeMillis();
            } else {
                throw new AuthenticationException("Failed to refresh token: " + response.get("error_message"));
            }
        } catch (Exception e) {
            throw new AuthenticationException("Error while refreshing token: " + e.getMessage(), e);
        }
    }

    /**
     * Initialize a video upload process
     */
    public TiktokVideo postVideo(String title, String privacyLevel, boolean disableDuet, boolean disableComment, boolean disableStitch, long videoCoverTimestampMs, long videoSize, long chunkSize, int totalChunkCount) throws TiktokApiException {
        checkTokenExpiration();
        String endpoint = BASE_URL + "/post/publish/video/init/";
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Content-Type", "application/json; charset=UTF-8");
        
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> postInfo = new HashMap<>();
        postInfo.put("title", title);
        postInfo.put("privacy_level", privacyLevel);
        postInfo.put("disable_duet", disableDuet);
        postInfo.put("disable_comment", disableComment);
        postInfo.put("disable_stitch", disableStitch);
        postInfo.put("video_cover_timestamp_ms", videoCoverTimestampMs);
        
        Map<String, Object> sourceInfo = new HashMap<>();
        sourceInfo.put("source", "FILE_UPLOAD");
        sourceInfo.put("video_size", videoSize);
        sourceInfo.put("chunk_size", chunkSize);
        sourceInfo.put("total_chunk_count", totalChunkCount);
        
        requestBody.put("post_info", postInfo);
        requestBody.put("source_info", sourceInfo);
        
        try {
            Map<String, Object> response = HttpUtils.post(endpoint, headers, requestBody);
            
            if (response.containsKey("video_id") && response.containsKey("upload_url")) {
                TiktokVideo video = new TiktokVideo();
                video.setId((String) response.get("video_id"));
                video.setTitle(title);
                video.setUploadUrl((String) response.get("upload_url"));
                return video;
            } else {
                throw new TiktokApiException("Failed to post video: " + response.get("error_message"));
            }
        } catch (Exception e) {
            throw new TiktokApiException("Error while posting video: " + e.getMessage(), e);
        }
    }
    
    /**
     * Upload a chunk of video data
     */
    public boolean uploadVideoChunk(String uploadUrl, byte[] chunkData, int chunkIndex) throws TiktokApiException {
        checkTokenExpiration();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Content-Type", "application/octet-stream");
        headers.put("x-chunk-index", String.valueOf(chunkIndex));
        
        try {
            Map<String, Object> response = HttpUtils.uploadChunk(uploadUrl, headers, chunkData);
            
            if (response.containsKey("success") && (Boolean) response.get("success")) {
                return true;
            } else {
                throw new TiktokApiException("Failed to upload chunk: " + response.get("error_message"));
            }
        } catch (Exception e) {
            throw new TiktokApiException("Error while uploading chunk: " + e.getMessage(), e);
        }
    }
    
    /**
     * Complete the video upload process
     */
    public TiktokVideo completeVideoUpload(String videoId) throws TiktokApiException {
        checkTokenExpiration();
        String endpoint = BASE_URL + "/post/publish/video/finish/";
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Content-Type", "application/json; charset=UTF-8");
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("video_id", videoId);
        
        try {
            Map<String, Object> response = HttpUtils.post(endpoint, headers, requestBody);
            
            if (response.containsKey("video_id")) {
                return getVideoById(videoId);
            } else {
                throw new TiktokApiException("Failed to complete video upload: " + response.get("error_message"));
            }
        } catch (Exception e) {
            throw new TiktokApiException("Error while completing video upload: " + e.getMessage(), e);
        }
    }
    
    /**
     * Helper method to upload a video file in chunks
     */
    public TiktokVideo uploadVideo(File videoFile, String title, String privacyLevel) throws TiktokApiException {
        try {
            long fileSize = videoFile.length();
            long chunkSize = 5 * 1024 * 1024; // 5MB chunks
            int totalChunks = (int) Math.ceil((double) fileSize / chunkSize);
            
            // Initialize video upload
            TiktokVideo video = postVideo(
                title, 
                privacyLevel, 
                false, // disableDuet
                false, // disableComment
                false, // disableStitch
                0,     // videoCoverTimestampMs
                fileSize,
                chunkSize,
                totalChunks
            );
            
            // Upload chunks
            FileInputStream fileInputStream = new FileInputStream(videoFile);
            byte[] buffer = new byte[(int) chunkSize];
            int bytesRead;
            int chunkIndex = 0;
            
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                byte[] chunk = bytesRead < buffer.length ? 
                    java.util.Arrays.copyOf(buffer, bytesRead) : buffer;
                    
                uploadVideoChunk(video.getUploadUrl(), chunk, chunkIndex);
                chunkIndex++;
            }
            fileInputStream.close();
            
            // Complete upload
            return completeVideoUpload(video.getId());
        } catch (IOException e) {
            throw new TiktokApiException("Error reading video file: " + e.getMessage(), e);
        }
    }

    /**
     * Get video details by ID
     */
    public TiktokVideo getVideoById(String videoId) throws TiktokApiException {
        checkTokenExpiration();
        String endpoint = BASE_URL + "/video/query/";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Content-Type", "application/json; charset=UTF-8");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("video_id", videoId);
        
        try {
            Map<String, Object> response = HttpUtils.post(endpoint, headers, requestBody);
            
            if (response.containsKey("video")) {
                Map<String, Object> videoData = (Map<String, Object>) response.get("video");
                TiktokVideo video = new TiktokVideo();
                video.setId(videoId);
                video.setTitle((String) videoData.get("title"));
                video.setDuration((Long) videoData.get("create_time"));
                video.setLikeCount((Integer) videoData.get("like_count"));
                video.setCommentCount((Integer) videoData.get("comment_count"));
                // video.setViewCount((Integer) videoData.get("view_count"));
                // video.setVideoUrl((String) videoData.get("video_url"));
                return video;
            } else {
                throw new TiktokApiException("Failed to fetch video: " + response.get("error_message"));
            }
        } catch (Exception e) {
            throw new TiktokApiException("Error while fetching video: " + e.getMessage(), e);
        }
    }

    /**
     * Post a comment on a video
     */
    public TiktokComment postComment(String videoId, String text) throws TiktokApiException {
        checkTokenExpiration();
        String endpoint = BASE_URL + "/comment/post/";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Content-Type", "application/json; charset=UTF-8");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("video_id", videoId);
        requestBody.put("text", text);
        
        try {
            Map<String, Object> response = HttpUtils.post(endpoint, headers, requestBody);
            
            if (response.containsKey("comment_id")) {
                TiktokComment comment = new TiktokComment();
                comment.setId((String) response.get("comment_id"));
                comment.setText(text);
               // comment.setVideoId(videoId);
               // comment.setCreateTime(System.currentTimeMillis() / 1000); // Current time in seconds
                return comment;
            } else {
                throw new TiktokApiException("Failed to post comment: " + response.get("error_message"));
            }
        } catch (Exception e) {
            throw new TiktokApiException("Error while posting comment: " + e.getMessage(), e);
        }
    }

    /**
     * Get comment by ID
     */
    public TiktokComment getCommentById(String commentId) throws TiktokApiException {
        checkTokenExpiration();
        String endpoint = BASE_URL + "/comment/query/";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Content-Type", "application/json; charset=UTF-8");
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("comment_id", commentId);
        
        try {
            Map<String, Object> response = HttpUtils.post(endpoint, headers, requestBody);
            
            if (response.containsKey("comment")) {
                Map<String, Object> commentData = (Map<String, Object>) response.get("comment");
                TiktokComment comment = new TiktokComment();
                comment.setId(commentId);
                comment.setText((String) commentData.get("text"));
                // comment.setVideoId((String) commentData.get("video_id"));
                // comment.setUserId((String) commentData.get("user_id"));
                // comment.setCreateTime((Long) commentData.get("create_time"));
                // comment.setLikeCount((Integer) commentData.get("like_count"));
                return comment;
            } else {
                throw new TiktokApiException("Failed to fetch comment: " + response.get("error_message"));
            }
        } catch (Exception e) {
            throw new TiktokApiException("Error while fetching comment: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get comments for a video
     */
    public List<TiktokComment> getVideoComments(String videoId, int count, String cursor) throws TiktokApiException {
        checkTokenExpiration();
        String endpoint = BASE_URL + "/video/comment/list/";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Content-Type", "application/json; charset=UTF-8");
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("video_id", videoId);
        requestBody.put("count", count);
        
        if (cursor != null && !cursor.isEmpty()) {
            requestBody.put("cursor", cursor);
        }
        
        try {
            Map<String, Object> response = HttpUtils.post(endpoint, headers, requestBody);
            
            if (response.containsKey("comments")) {
                List<Map<String, Object>> commentsData = (List<Map<String, Object>>) response.get("comments");
                List<TiktokComment> comments = new ArrayList<>();
                
                for (Map<String, Object> commentData : commentsData) {
                    TiktokComment comment = new TiktokComment();
                    comment.setId((String) commentData.get("comment_id"));
                    comment.setText((String) commentData.get("text"));
                    // comment.setVideoId(videoId);
                    // comment.setUserId((String) commentData.get("user_id"));
                    // comment.setCreateTime((Long) commentData.get("create_time"));
                    // comment.setLikeCount((Integer) commentData.get("like_count"));
                    comments.add(comment);
                }
                
                return comments;
            } else {
                throw new TiktokApiException("Failed to fetch video comments: " + response.get("error_message"));
            }
        } catch (Exception e) {
            throw new TiktokApiException("Error while fetching video comments: " + e.getMessage(), e);
        }
    }
   
    /**
     * Get user information by ID
     */
    public TiktokUser getUserById(String userId) throws TiktokApiException {
        checkTokenExpiration();
        String endpoint = BASE_URL + "/user/info/";
        Map<String, String> headers = new HashMap<>(); 
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Content-Type", "application/json; charset=UTF-8"); 
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", userId);
        
        try {
            Map<String, Object> response = HttpUtils.post(endpoint, headers, requestBody);
            
            if (response.containsKey("user")) {
                Map<String, Object> userData = (Map<String, Object>) response.get("user");
                TiktokUser user = new TiktokUser();
                user.setId(userId);
                user.setUsername((String) userData.get("username"));
                //user.setDisplayName((String) userData.get("display_name"));
                //user.setAvatarUrl((String) userData.get("avatar_url"));
                //user.setFollowerCount((Integer) userData.get("follower_count"));
                //user.setFollowingCount((Integer) userData.get("following_count"));
                //user.setLikeCount((Integer) userData.get("like_count"));
                //user.setBio((String) userData.get("bio"));
                return user;
            } else {
                throw new TiktokApiException("Failed to fetch user: " + response.get("error_message"));
            }
        } catch (Exception e) {
            throw new TiktokApiException("Error while fetching user: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get videos for a user
     */
    public List<TiktokVideo> getUserVideos(String userId, int count, String cursor) throws TiktokApiException {
        checkTokenExpiration();
        String endpoint = BASE_URL + "/user/videos/";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Content-Type", "application/json; charset=UTF-8");
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", userId);
        requestBody.put("count", count);
        
        if (cursor != null && !cursor.isEmpty()) {
            requestBody.put("cursor", cursor);
        }
        
        try {
            Map<String, Object> response = HttpUtils.post(endpoint, headers, requestBody);
            
            if (response.containsKey("videos")) {
                List<Map<String, Object>> videosData = (List<Map<String, Object>>) response.get("videos");
                List<TiktokVideo> videos = new ArrayList<>();
                
                for (Map<String, Object> videoData : videosData) {
                    TiktokVideo video = new TiktokVideo();
                    video.setId((String) videoData.get("video_id"));
                    video.setTitle((String) videoData.get("title"));
                    // video.setCreateTime((Long) videoData.get("create_time"));
                    video.setLikeCount((Integer) videoData.get("like_count"));
                    video.setCommentCount((Integer) videoData.get("comment_count"));
                    //video.setShareCount((Integer) videoData.get("share_count"));
                    // video.setViewCount((Integer) videoData.get("view_count"));
                    // video.setVideoUrl((String) videoData.get("video_url"));
                    videos.add(video);
                }
                
                return videos;
            } else {
                throw new TiktokApiException("Failed to fetch user videos: " + response.get("error_message"));
            }
        } catch (Exception e) {
            throw new TiktokApiException("Error while fetching user videos: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete a video by ID
     */
    public boolean deleteVideoById(String videoId) throws TiktokApiException {
        checkTokenExpiration();
        String endpoint = BASE_URL + "/video/delete/";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Content-Type", "application/json; charset=UTF-8"); 
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("video_id", videoId);
        
        try {
            Map<String, Object> response = HttpUtils.post(endpoint, headers, requestBody);
            
            if (response.containsKey("success") && (Boolean) response.get("success")) {
                return true;
            } else {
                throw new TiktokApiException("Failed to delete video: " + response.get("error_message"));
            }
        } catch (Exception e) {
            throw new TiktokApiException("Error while deleting video: " + e.getMessage(), e);
        }
    }
}