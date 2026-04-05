package com.ai.ai_triage.dto;

public class FeedbackRequest {
    private String rating;  // "useful" or "not useful"
    private String comment;

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}