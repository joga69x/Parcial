package com.example.parcial.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.UUID;

public class Contribution implements Serializable {
    private String id;
    private String goalId;
    private String goalTitle;
    private double amount;
    private long date;
    private String note;

    public Contribution(String goalId, String goalTitle, double amount, long date, String note) {
        this.id = UUID.randomUUID().toString();
        this.goalId = goalId;
        this.goalTitle = goalTitle;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }

    public Contribution(String id, String goalId, String goalTitle, double amount, long date, String note) {
        this.id = id;
        this.goalId = goalId;
        this.goalTitle = goalTitle;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public String getGoalId() {
        return goalId;
    }

    public String getGoalTitle() {
        return goalTitle;
    }

    public double getAmount() {
        return amount;
    }

    public long getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("goalId", goalId);
        obj.put("goalTitle", goalTitle);
        obj.put("amount", amount);
        obj.put("date", date);
        obj.put("note", note);
        return obj;
    }

    public static Contribution fromJsonObject(JSONObject obj) throws JSONException {
        return new Contribution(
                obj.getString("id"),
                obj.getString("goalId"),
                obj.optString("goalTitle", "Meta"),
                obj.getDouble("amount"),
                obj.getLong("date"),
                obj.optString("note", "")
        );
    }
}