package com.Panda.tiktokDevKit.model;

public class TiktokVideo {
    private String id;
    private String title;
    private String authorId;
    private String caption;
    private String thumbnailUrl;
    private long duration;
    private long likeCount;
    private long commentCount;
    private String description;
    private String uploadUrl;
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getId() {
        return id;
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
    
    public String getCaption() {
        return caption;
    }
    
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
    public long getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }
    
    public long getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }
    
    public String getUploadUrl() {
        return uploadUrl;
    }
    
    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }
}