package com.example.parcial.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.UUID;

public class SavingGoal implements Serializable {
    private String id;
    private String title;
    private String category; // Viajes, Emergencia, Vehículo, Tecnología, Hogar, Educación, General
    private double targetAmount;
    private double currentAmount;
    private long targetDate; // ms
    private long startDate;  // ms

    public SavingGoal(String title, String category, double targetAmount, double currentAmount, long targetDate, long startDate) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.category = category;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
        this.startDate = startDate;
    }

    public SavingGoal(String id, String title, String category, double targetAmount, double currentAmount, long targetDate, long startDate) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
        this.startDate = startDate;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public long getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(long targetDate) {
        this.targetDate = targetDate;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    // Calculations
    public double getRemainingAmount() {
        return Math.max(0, targetAmount - currentAmount);
    }

    public int getProgressPercentage() {
        if (targetAmount <= 0) return 0;
        int pct = (int) Math.round((currentAmount / targetAmount) * 100);
        return Math.min(100, pct);
    }

    public long getRemainingDays() {
        long diffMs = targetDate - System.currentTimeMillis();
        long days = (long) Math.ceil(diffMs / (1000.0 * 60 * 60 * 24));
        return Math.max(1, days);
    }

    public double getRemainingMonths() {
        double days = getRemainingDays();
        return Math.max(0.1, days / 30.0);
    }

    public double getSuggestedDepositDaily() {
        long days = getRemainingDays();
        return getRemainingAmount() / days;
    }

    public double getSuggestedDepositWeekly() {
        double weeks = Math.max(1.0, getRemainingDays() / 7.0);
        return getRemainingAmount() / weeks;
    }

    public double getSuggestedDepositBiweekly() {
        double biweeks = Math.max(1.0, getRemainingDays() / 15.0);
        return getRemainingAmount() / biweeks;
    }

    public double getSuggestedDepositMonthly() {
        double months = getRemainingMonths();
        return getRemainingAmount() / months;
    }

    public boolean isCompleted() {
        return currentAmount >= targetAmount;
    }

    // JSON serialization
    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("title", title);
        obj.put("category", category);
        obj.put("targetAmount", targetAmount);
        obj.put("currentAmount", currentAmount);
        obj.put("targetDate", targetDate);
        obj.put("startDate", startDate);
        return obj;
    }

    public static SavingGoal fromJsonObject(JSONObject obj) throws JSONException {
        return new SavingGoal(
                obj.getString("id"),
                obj.getString("title"),
                obj.getString("category"),
                obj.getDouble("targetAmount"),
                obj.getDouble("currentAmount"),
                obj.getLong("targetDate"),
                obj.getLong("startDate")
        );
    }
}