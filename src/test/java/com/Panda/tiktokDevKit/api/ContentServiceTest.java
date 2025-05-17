package com.Panda.tiktokDevKit.api;

import com.Panda.tiktokDevKit.exception.TiktokApiException;
import com.Panda.tiktokDevKit.util.HttpUtils; 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test; 
import org.mockito.MockedStatic; 

import com.Panda.tiktokDevKit.model.TiktokVideo;
import java.util.HashMap; 
import java.util.Map; 
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*; 
import static org.mockito.ArgumentMatchers.anyMap; 
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ContentServiceTest
{

    private ContentService contentService; 
    
     /* 
    @Test 
    void testPostVideo() throws Exception
    {
        String title = "Test Video"; 
        String privacyLevel = "public";
        boolean disableDuet = false; 
        boolean disableComment = false; 
        boolean disableStitch = false;
        Long videoCoverTimestampMs = 0L;
        Long videoSize = 12345689L; 
        Long chunkSize = 1048576L; 
        int totalChunkCount = 1;


        assertTrue(title.length() > 0, "Title should not be empty");
        assertTrue(privacyLevel.equals("public") || privacyLevel.equals("private"), "Privacy level should be either 'public' or 'private'"); 
        assertTrue(videoSize > 0, "Video size should be greater than 0"); 
        assertTrue(chunkSize > 0, "Chunk size should be greater than 0"); 
        assertTrue(totalChunkCount > 0, "Total chunk count should be greater than 0"); 

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("video_id", "test_video_id");

        try (MockedStatic<HttpUtils> mockedStatic = mockStatic(HttpUtils.class))
        {
            mockedStatic.when(() -> HttpUtils.post(anyString(), anyMap(), anyMap()))
                    .thenReturn(mockResponse);

            contentService = new ContentService("test_access_token", "test_refresh_token", 3600);
            TiktokVideo video = contentService.postVideo(title, privacyLevel, disableDuet, disableComment, disableStitch, videoCoverTimestampMs, videoSize, chunkSize, totalChunkCount);

            assertNotNull(video);
            assertEquals("test_video_id", video.getId());
            assertEquals(title, video.getTitle());
        }
    }
        /* 

     /* @Test
    void testGetVideo() throws Exception
    {
        String videoId = "test_video_id"; 
        Map<String, Object> mockResponse = new HashMap<>(); 
        mockResponse.put("video_id", videoId); 
        mockResponse.put("title", "Test Video");
        mockResponse.put("description", "Test Description");
        mockResponse.put("cover_url", "https://example.com/cover.jpg");
        mockResponse.put("video_url", "https://example.com/video.mp4");
        mockResponse.put("duration", 120);
        mockResponse.put("play_url", "https://example.com/play.mp4");
        mockResponse.put("status", "success");

        try(MockedStatic<HttpUtils> mockedStatic = mockStatic(HttpUtils.class))
        {
            mockedStatic.when(() -> HttpUtils.get(anyString(), anyMap(), anyMap()))
                    .thenReturn(mockResponse);

            contentService = new ContentService("test_access_token", "test_refresh_token", 3600);
            TiktokVideo video = contentService.getVideoById(videoId);

            assertNotNull(video);
            assertEquals(videoId, video.getId());
            assertEquals("Test Video", video.getTitle());
            assertEquals("Test Description", video.getDescription());
        }
     
    }
        /* */
}