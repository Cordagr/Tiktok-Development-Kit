package com.Panda.tiktokDevKit.model;
public class TiktokComment {
    private String id;
    private String text;
    private String authorId;
    private long createdAt;
    private String description; 


    public String getDescription() 
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description; 
    }

    public String getId() {
        return id;
    }

    public void setId(String id)
    {
        this.id = id; 
    }

    public void setText(String text)
    {
        this.text = text; 
    }

    
    public String getText() {
        return text;
    }

    public String getAuthorId() {
        return authorId;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
