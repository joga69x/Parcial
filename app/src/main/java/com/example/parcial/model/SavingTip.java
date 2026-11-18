package com.example.parcial.model;

public class SavingTip {
    private String title;
    private String subtitle;
    private String fullDescription;
    private int iconResId;

    public SavingTip(String title, String subtitle, String fullDescription, int iconResId) {
        this.title = title;
        this.subtitle = subtitle;
        this.fullDescription = fullDescription;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public int getIconResId() {
        return iconResId;
    }
}