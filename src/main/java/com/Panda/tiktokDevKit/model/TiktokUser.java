// Removed all import statements
package com.Panda.tiktokDevKit.model;
public class TiktokUser {
    private String id;
    private String username;
    private String avatarUrl;
    private int followerCount;
    private int followingCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }
}
