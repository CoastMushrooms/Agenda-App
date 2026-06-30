package com.example.homeworkapp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Assignment {
    private String id;
    private String title;
    private String description;
    private String subject;
    private String dueDate;
    private long createdAt;
    private String imageUrl;
    private String imageBase64;
    private boolean completed;
    private long completedAt;
    private String priority;

    public Assignment() {}

    public Assignment(String id, String title, String description, String subject, String dueDate, long createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.completed = false;
        this.completedAt = 0;
        this.priority = "None";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public boolean hasImage() {
        return (imageBase64 != null && !imageBase64.isEmpty())
                || (imageUrl != null && !imageUrl.isEmpty());
    }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public boolean isHighPriority() {
        return "High".equals(priority);
    }
    public boolean isMediumPriority() {
        return "Medium".equals(priority);
    }
    public boolean isLowPriority() {
        return "Low".equals(priority);
    }
    public boolean hasPriority() {
        return priority != null && !priority.isEmpty() && !"None".equals(priority);
    }

    public int getPriorityOrder() {
        if ("High".equals(priority)) return 0;
        if ("Medium".equals(priority)) return 1;
        if ("Low".equals(priority)) return 2;
        return 3;
    }

    public LocalDate getDueDateAsLocalDate() {
        try { return LocalDate.parse(dueDate, DateTimeFormatter.ISO_LOCAL_DATE); }
        catch (Exception e) { return null; }
    }

    public int getDaysUntilDue() {
        LocalDate d = getDueDateAsLocalDate();
        if (d != null) return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), d);
        return Integer.MAX_VALUE;
    }

    public boolean isOverdue() { return getDaysUntilDue() < 0; }
    public boolean isDueSoon() { int d = getDaysUntilDue(); return d >= 0 && d <= 3; }
    public boolean isCompleted() { return completed; }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        this.completedAt = completed ? System.currentTimeMillis() : 0;
    }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    public boolean shouldAutoDelete() {
        if (!completed || completedAt == 0) return false;
        return (System.currentTimeMillis() - completedAt) >= 48 * 60 * 60 * 1000L;
    }

    public long getTimeUntilAutoDelete() {
        if (!completed || completedAt == 0) return -1;
        long remaining = (48 * 60 * 60 * 1000L) - (System.currentTimeMillis() - completedAt);
        return remaining > 0 ? remaining : 0;
    }

    public String getCompletionStatus() {
        if (!completed) return "Pending";
        long h = getTimeUntilAutoDelete() / (60 * 60 * 1000);
        return h <= 0 ? "Deleting soon..." : "Completed - " + h + "h left";
    }
}
