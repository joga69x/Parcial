package com.example.parcial.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.parcial.model.Contribution;
import com.example.parcial.model.SavingGoal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StorageManager {
    private static final String PREF_NAME = "SmartSaverPrefs";
    private static final String KEY_GOALS = "key_goals_list";
    private static final String KEY_CONTRIBUTIONS = "key_contributions_list";
    private static final String KEY_ACTIVE_GOAL_ID = "key_active_goal_id";

    private final SharedPreferences prefs;

    public StorageManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        ensureDefaultGoalsIfEmpty();
    }

    private void ensureDefaultGoalsIfEmpty() {
        List<SavingGoal> goals = getGoals();
        if (goals.isEmpty()) {
            Calendar calTarget1 = Calendar.getInstance();
            calTarget1.add(Calendar.MONTH, 6);

            Calendar calTarget2 = Calendar.getInstance();
            calTarget2.add(Calendar.MONTH, 12);

            Calendar calTarget3 = Calendar.getInstance();
            calTarget3.add(Calendar.MONTH, 3);

            SavingGoal goal1 = new SavingGoal(
                    "Fondo de Emergencia",
                    "Emergencia",
                    3000000.0,
                    1200000.0,
                    calTarget1.getTimeInMillis(),
                    System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 30)
            );

            SavingGoal goal2 = new SavingGoal(
                    "Viaje a San Andrés",
                    "Viajes",
                    2500000.0,
                    750000.0,
                    calTarget2.getTimeInMillis(),
                    System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 15)
            );

            SavingGoal goal3 = new SavingGoal(
                    "Nuevo Computador Pro",
                    "Tecnología",
                    4000000.0,
                    2100000.0,
                    calTarget3.getTimeInMillis(),
                    System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 45)
            );

            goals.add(goal1);
            goals.add(goal2);
            goals.add(goal3);

            saveGoals(goals);
            setActiveGoalId(goal1.getId());

            // Add sample contributions
            List<Contribution> contribs = new ArrayList<>();
            contribs.add(new Contribution(goal1.getId(), goal1.getTitle(), 500000.0, System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 20), "Abono inicial de sueldo"));
            contribs.add(new Contribution(goal1.getId(), goal1.getTitle(), 700000.0, System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 5), "Ahorro extra de quincena"));
            contribs.add(new Contribution(goal2.getId(), goal2.getTitle(), 750000.0, System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 10), "Reserva de pasajes"));
            contribs.add(new Contribution(goal3.getId(), goal3.getTitle(), 2100000.0, System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 15), "Venta de equipo anterior"));
            saveContributions(contribs);
        }
    }

    public List<SavingGoal> getGoals() {
        List<SavingGoal> list = new ArrayList<>();
        String jsonStr = prefs.getString(KEY_GOALS, null);
        if (jsonStr == null) return list;

        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                list.add(SavingGoal.fromJsonObject(arr.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void saveGoals(List<SavingGoal> goals) {
        JSONArray arr = new JSONArray();
        for (SavingGoal g : goals) {
            try {
                arr.put(g.toJsonObject());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString(KEY_GOALS, arr.toString()).apply();
    }

    public void addGoal(SavingGoal goal) {
        List<SavingGoal> goals = getGoals();
        goals.add(0, goal);
        saveGoals(goals);
        setActiveGoalId(goal.getId());
    }

    public void updateGoal(SavingGoal updatedGoal) {
        List<SavingGoal> goals = getGoals();
        for (int i = 0; i < goals.size(); i++) {
            if (goals.get(i).getId().equals(updatedGoal.getId())) {
                goals.set(i, updatedGoal);
                break;
            }
        }
        saveGoals(goals);
    }

    public void deleteGoal(String goalId) {
        List<SavingGoal> goals = getGoals();
        goals.removeIf(g -> g.getId().equals(goalId));
        saveGoals(goals);

        if (getActiveGoalId().equals(goalId)) {
            if (!goals.isEmpty()) {
                setActiveGoalId(goals.get(0).getId());
            } else {
                setActiveGoalId("");
            }
        }
    }

    public SavingGoal getActiveGoal() {
        String activeId = getActiveGoalId();
        List<SavingGoal> goals = getGoals();
        for (SavingGoal g : goals) {
            if (g.getId().equals(activeId)) {
                return g;
            }
        }
        if (!goals.isEmpty()) {
            setActiveGoalId(goals.get(0).getId());
            return goals.get(0);
        }
        return null;
    }

    public String getActiveGoalId() {
        return prefs.getString(KEY_ACTIVE_GOAL_ID, "");
    }

    public void setActiveGoalId(String goalId) {
        prefs.edit().putString(KEY_ACTIVE_GOAL_ID, goalId).apply();
    }

    public List<Contribution> getContributions() {
        List<Contribution> list = new ArrayList<>();
        String jsonStr = prefs.getString(KEY_CONTRIBUTIONS, null);
        if (jsonStr == null) return list;

        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                list.add(Contribution.fromJsonObject(arr.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void saveContributions(List<Contribution> list) {
        JSONArray arr = new JSONArray();
        for (Contribution c : list) {
            try {
                arr.put(c.toJsonObject());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString(KEY_CONTRIBUTIONS, arr.toString()).apply();
    }

    public void addContribution(Contribution contribution) {
        List<Contribution> list = getContributions();
        list.add(0, contribution);
        saveContributions(list);

        // Update target goal's current amount
        SavingGoal goal = getGoalById(contribution.getGoalId());
        if (goal != null) {
            goal.setCurrentAmount(goal.getCurrentAmount() + contribution.getAmount());
            updateGoal(goal);
        }
    }

    public void deleteContribution(String contributionId) {
        List<Contribution> list = getContributions();
        Contribution targetContrib = null;
        for (Contribution c : list) {
            if (c.getId().equals(contributionId)) {
                targetContrib = c;
                break;
            }
        }

        if (targetContrib != null) {
            list.remove(targetContrib);
            saveContributions(list);

            SavingGoal goal = getGoalById(targetContrib.getGoalId());
            if (goal != null) {
                goal.setCurrentAmount(Math.max(0, goal.getCurrentAmount() - targetContrib.getAmount()));
                updateGoal(goal);
            }
        }
    }

    public SavingGoal getGoalById(String goalId) {
        for (SavingGoal g : getGoals()) {
            if (g.getId().equals(goalId)) {
                return g;
            }
        }
        return null;
    }

    public double getTotalSavedAllGoals() {
        double total = 0;
        for (SavingGoal g : getGoals()) {
            total += g.getCurrentAmount();
        }
        return total;
    }

    // EXTRA 1 & 2: 30-Day Challenge & Micro-Savings Storage
    private static final String KEY_CHALLENGE_MASK = "key_challenge_days_mask";
    private static final String KEY_HORMIGA_SAVED = "key_hormiga_saved_amount";
    private static final String KEY_REMINDER_SETTING = "key_reminder_setting";

    public boolean isChallengeDayDone(int day) {
        String mask = prefs.getString(KEY_CHALLENGE_MASK, "");
        return mask.contains("," + day + ",");
    }

    public void toggleChallengeDay(int day) {
        String mask = prefs.getString(KEY_CHALLENGE_MASK, "");
        if (isChallengeDayDone(day)) {
            mask = mask.replace("," + day + ",", "");
        } else {
            mask += "," + day + ",";
        }
        prefs.edit().putString(KEY_CHALLENGE_MASK, mask).apply();
    }

    public int getChallengeCompletedCount() {
        int count = 0;
        for (int i = 1; i <= 30; i++) {
            if (isChallengeDayDone(i)) count++;
        }
        return count;
    }

    public double getHormigaSavedTotal() {
        return Double.longBitsToDouble(prefs.getLong(KEY_HORMIGA_SAVED, Double.doubleToLongBits(0.0)));
    }

    public void addHormigaSaved(double amount) {
        double current = getHormigaSavedTotal();
        prefs.edit().putLong(KEY_HORMIGA_SAVED, Double.doubleToLongBits(current + amount)).apply();
    }

    public boolean isReminderEnabled() {
        return prefs.getBoolean(KEY_REMINDER_SETTING, true);
    }

    public void setReminderEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_REMINDER_SETTING, enabled).apply();
    }
}