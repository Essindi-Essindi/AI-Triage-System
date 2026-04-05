package com.ai.ai_triage.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String rating;
    private String comment;

    public Feedback() {}
    public Feedback(String rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    public Long getId() { return id; }
    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}