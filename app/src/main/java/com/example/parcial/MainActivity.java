package com.example.parcial;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.parcial.adapter.ContributionAdapter;
import com.example.parcial.adapter.GoalAdapter;
import com.example.parcial.adapter.TipAdapter;
import com.example.parcial.data.StorageManager;
import com.example.parcial.databinding.ActivityMainBinding;
import com.example.parcial.databinding.DialogAddContributionBinding;
import com.example.parcial.databinding.DialogNewGoalBinding;
import com.example.parcial.model.Contribution;
import com.example.parcial.model.SavingGoal;
import com.example.parcial.model.SavingTip;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoalAdapter.OnGoalActionListener, ContributionAdapter.OnContributionActionListener {

    private ActivityMainBinding binding;
    private StorageManager storageManager;

    private GoalAdapter goalAdapter;
    private ContributionAdapter contributionAdapter;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currencyFormat.setMaximumFractionDigits(0);
        storageManager = new StorageManager(this);

        setupNavigation();
        setupRecyclerViews();
        setupSimulator();
        setupExtras();
        setupClickListeners();

        refreshAllViews();
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            binding.layoutHome.setVisibility(id == R.id.nav_home ? View.VISIBLE : View.GONE);
            binding.layoutGoals.setVisibility(id == R.id.nav_goals ? View.VISIBLE : View.GONE);
            binding.layoutHistory.setVisibility(id == R.id.nav_history ? View.VISIBLE : View.GONE);
            binding.layoutSimulator.setVisibility(id == R.id.nav_simulator ? View.VISIBLE : View.GONE);
            return true;
        });
    }

    private void setupRecyclerViews() {
        // Goals RecyclerView
        goalAdapter = new GoalAdapter(this);
        binding.rvGoals.setLayoutManager(new LinearLayoutManager(this));
        binding.rvGoals.setAdapter(goalAdapter);

        // History RecyclerView
        contributionAdapter = new ContributionAdapter(this);
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(contributionAdapter);

        // Tips RecyclerView
        TipAdapter tipAdapter = new TipAdapter(getSavingTipsList());
        binding.rvTips.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTips.setAdapter(tipAdapter);
    }

    private void setupClickListeners() {
        binding.btnHomeAddDeposit.setOnClickListener(v -> {
            SavingGoal activeGoal = storageManager.getActiveGoal();
            if (activeGoal != null) {
                showAddContributionDialog(activeGoal);
            } else {
                Toast.makeText(this, "Primero crea una meta de ahorro", Toast.LENGTH_SHORT).show();
                showNewGoalDialog();
            }
        });

        binding.btnHeaderNewGoal.setOnClickListener(v -> showNewGoalDialog());
    }

    private void setupSimulator() {
        binding.btnCalculateSim.setOnClickListener(v -> runSimulation());
        runSimulation(); // Initial simulation run
    }

    private void setupExtras() {
        // Extra 1: Reto 30 días
        binding.btnCheckTodayChallenge.setOnClickListener(v -> {
            int completed = storageManager.getChallengeCompletedCount();
            if (completed < 30) {
                int nextDay = completed + 1;
                storageManager.toggleChallengeDay(nextDay);
                double challengeAmount = 5000.0;

                SavingGoal activeGoal = storageManager.getActiveGoal();
                if (activeGoal != null) {
                    Contribution contrib = new Contribution(
                            activeGoal.getId(),
                            activeGoal.getTitle(),
                            challengeAmount,
                            System.currentTimeMillis(),
                            "🏆 Reto Día #" + nextDay
                    );
                    storageManager.addContribution(contrib);
                }

                Toast.makeText(this, "🎉 ¡Día " + nextDay + " completado! + $5,000 abonados a tu meta.", Toast.LENGTH_SHORT).show();
                refreshAllViews();
            } else {
                Toast.makeText(this, "🏆 ¡Felicitaciones! Has completado el Reto de 30 Días.", Toast.LENGTH_SHORT).show();
            }
        });

        // Extra 2: Rastreador de Gastos Hormiga
        binding.btnSaveHormiga.setOnClickListener(v -> {
            String amountStr = binding.etHormigaAmount.getText() != null ? binding.etHormigaAmount.getText().toString().trim() : "";
            if (TextUtils.isEmpty(amountStr)) {
                binding.etHormigaAmount.setError("Ingresa el monto salvado");
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) return;

                storageManager.addHormigaSaved(amount);
                SavingGoal activeGoal = storageManager.getActiveGoal();
                if (activeGoal != null) {
                    Contribution contrib = new Contribution(
                            activeGoal.getId(),
                            activeGoal.getTitle(),
                            amount,
                            System.currentTimeMillis(),
                            "🐜 Gastos hormiga evitados"
                    );
                    storageManager.addContribution(contrib);
                }

                binding.etHormigaAmount.setText("");
                Toast.makeText(this, "🎉 ¡Genial! +" + currencyFormat.format(amount) + " salvados de gastos hormiga.", Toast.LENGTH_SHORT).show();
                refreshAllViews();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        });

        // Extra 3: Interés Compuesto
        binding.btnCalcCompound.setOnClickListener(v -> runCompoundInterestCalculation());

        // Extra 4: Compartir Reporte
        binding.btnShareReport.setOnClickListener(v -> shareFinancialReport());

        // Extra 5: Switch Recordatorio
        binding.switchReminder.setChecked(storageManager.isReminderEnabled());
        binding.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            storageManager.setReminderEnabled(isChecked);
            if (isChecked) {
                Toast.makeText(this, "🔔 Recordatorio diario activado a las 8:00 PM", Toast.LENGTH_SHORT).show();
                binding.tvReminderText.setText("⏰ Recordatorio activo: Todos los días a las 8:00 PM");
            } else {
                Toast.makeText(this, "🔕 Recordatorio diario desactivado", Toast.LENGTH_SHORT).show();
                binding.tvReminderText.setText("⏸️ Recordatorio pausado");
            }
        });
    }

    private void runCompoundInterestCalculation() {
        String rateStr = binding.etInterestRate.getText() != null ? binding.etInterestRate.getText().toString().trim() : "";
        String yearsStr = binding.etCompoundYears.getText() != null ? binding.etCompoundYears.getText().toString().trim() : "";

        if (TextUtils.isEmpty(rateStr) || TextUtils.isEmpty(yearsStr)) return;

        try {
            double rate = Double.parseDouble(rateStr) / 100.0;
            int years = Integer.parseInt(yearsStr);

            double principal = storageManager.getTotalSavedAllGoals();
            if (principal <= 0) principal = 1000000.0;

            double finalAmount = principal * Math.pow(1.0 + rate, years);
            double earnings = finalAmount - principal;

            String text = "Al " + (int)(rate * 100) + "% EA en " + years +
                    " años tus " + currencyFormat.format(principal) + " se convertirán en " +
                    currencyFormat.format(finalAmount) + " (+ " + currencyFormat.format(earnings) + " de ganancias puras).";
            binding.tvCompoundResult.setText(text);

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void shareFinancialReport() {
        double totalSaved = storageManager.getTotalSavedAllGoals();
        SavingGoal activeGoal = storageManager.getActiveGoal();
        int challengeDays = storageManager.getChallengeCompletedCount();
        double hormigaSaved = storageManager.getHormigaSavedTotal();

        StringBuilder reportText = new StringBuilder();
        reportText.append("📊 *MI PLAN DE AHORRO EN SMARTSAVER*\n\n");
        reportText.append("💰 Total Ahorrado: ").append(currencyFormat.format(totalSaved)).append("\n");

        if (activeGoal != null) {
            reportText.append("🎯 Meta Principal: ").append(activeGoal.getTitle()).append("\n");
            reportText.append("📈 Progreso: ").append(activeGoal.getProgressPercentage()).append("% (")
                    .append(currencyFormat.format(activeGoal.getCurrentAmount())).append(" / ")
                    .append(currencyFormat.format(activeGoal.getTargetAmount())).append(")\n");
        }

        reportText.append("🏆 Reto de 30 Días: ").append(challengeDays).append("/30 días completados\n");
        reportText.append("🐜 Gastos Hormiga Evitados: ").append(currencyFormat.format(hormigaSaved)).append("\n\n");
        reportText.append("¡Ahorrando dinero de forma inteligente y efectiva! 💪✨");

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, reportText.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Compartir mi progreso de ahorro");
        startActivity(shareIntent);
    }

    private void runSimulation() {
        String amountStr = binding.etSimAmount.getText() != null ? binding.etSimAmount.getText().toString().trim() : "";
        String monthsStr = binding.etSimMonths.getText() != null ? binding.etSimMonths.getText().toString().trim() : "";

        if (TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(monthsStr)) {
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            double months = Double.parseDouble(monthsStr);

            if (amount <= 0 || months <= 0) return;

            double days = months * 30.0;
            double weeks = months * 4.333;
            double biweeks = months * 2.0;

            double daily = amount / days;
            double weekly = amount / weeks;
            double biweekly = amount / biweeks;
            double monthly = amount / months;

            binding.tvSimDaily.setText("• Cuota Diaria: " + currencyFormat.format(daily) + " / día");
            binding.tvSimWeekly.setText("• Cuota Semanal: " + currencyFormat.format(weekly) + " / semana");
            binding.tvSimBiweekly.setText("• Cuota Quincenal: " + currencyFormat.format(biweekly) + " / quincena");
            binding.tvSimMonthly.setText("• Cuota Mensual: " + currencyFormat.format(monthly) + " / mes");

            binding.layoutSimResults.setVisibility(View.VISIBLE);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void refreshAllViews() {
        // Refresh Goals list & Active Goal
        List<SavingGoal> goals = storageManager.getGoals();
        SavingGoal activeGoal = storageManager.getActiveGoal();
        String activeId = storageManager.getActiveGoalId();

        goalAdapter.setGoals(goals, activeId);
        binding.tvGoalsCountHeader.setText(goals.size() + " metas registradas");

        // Refresh History list
        List<Contribution> contributions = storageManager.getContributions();
        contributionAdapter.setContributions(contributions);
        binding.tvHistoryTotalHeader.setText(contributions.size() + " depósitos realizados en total");

        // Refresh Total Saved Header
        double totalSavedAll = storageManager.getTotalSavedAllGoals();
        binding.tvHomeTotalSaved.setText(currencyFormat.format(totalSavedAll));
        binding.tvHomeGoalsSummary.setText(goals.size() + " metas activas en progreso");

        // Refresh Active Goal Card
        if (activeGoal != null) {
            binding.cardActiveGoal.setVisibility(View.VISIBLE);
            binding.tvActiveTitle.setText(activeGoal.getTitle());
            binding.tvActiveCategory.setText("Categoría: " + activeGoal.getCategory());
            binding.tvActiveCurrent.setText(currencyFormat.format(activeGoal.getCurrentAmount()));
            binding.tvActiveTarget.setText("de " + currencyFormat.format(activeGoal.getTargetAmount()));

            int pct = activeGoal.getProgressPercentage();
            binding.tvActivePct.setText(pct + "%");
            binding.progressActive.setProgress(pct);

            long days = activeGoal.getRemainingDays();
            binding.tvActiveDaysRemaining.setText("⏱️ Faltan " + days + " días para cumplir la meta");

            binding.tvDepositDaily.setText(currencyFormat.format(activeGoal.getSuggestedDepositDaily()));
            binding.tvDepositWeekly.setText(currencyFormat.format(activeGoal.getSuggestedDepositWeekly()));
            binding.tvDepositBiweekly.setText(currencyFormat.format(activeGoal.getSuggestedDepositBiweekly()));
            binding.tvDepositMonthly.setText(currencyFormat.format(activeGoal.getSuggestedDepositMonthly()));

            int iconRes = R.drawable.ic_piggy_bank;
            if ("Emergencia".equalsIgnoreCase(activeGoal.getCategory())) iconRes = R.drawable.ic_emergency;
            else if ("Viajes".equalsIgnoreCase(activeGoal.getCategory())) iconRes = R.drawable.ic_travel;
            else if ("Vehículo".equalsIgnoreCase(activeGoal.getCategory())) iconRes = R.drawable.ic_car;
            else if ("Tecnología".equalsIgnoreCase(activeGoal.getCategory())) iconRes = R.drawable.ic_tech;
            else if ("Educación".equalsIgnoreCase(activeGoal.getCategory())) iconRes = R.drawable.ic_education;

            binding.ivActiveCategoryIcon.setImageResource(iconRes);
            if (activeGoal.isCompleted()) {
                binding.tvActiveHealthStatus.setText("🎉 METAS LOGRADA");
            } else {
                binding.tvActiveHealthStatus.setText("🔥 EN CAMINO");
            }
        } else {
            binding.cardActiveGoal.setVisibility(View.GONE);
        }

        // Refresh Extras UI
        int completedChallenge = storageManager.getChallengeCompletedCount();
        binding.tvChallengeCount.setText(completedChallenge + "/30 DÍAS");
        binding.tvChallengeTotal.setText("Ahorra $5,000 diarios. Total acumulado en reto: " + currencyFormat.format(completedChallenge * 5000.0));

        double hormigaSaved = storageManager.getHormigaSavedTotal();
        binding.tvHormigaTotal.setText("Total acumulado en micro-ahorros: " + currencyFormat.format(hormigaSaved));

        runCompoundInterestCalculation();
    }

    // Dialog: Add Contribution
    private void showAddContributionDialog(SavingGoal goal) {
        DialogAddContributionBinding dialogBinding = DialogAddContributionBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogBinding.tvDialogSubtitle.setText("Ingresa el dinero que vas a guardar para: " + goal.getTitle());

        // Quick amount buttons
        dialogBinding.btnQuick50k.setOnClickListener(v -> dialogBinding.etAmount.setText("50000"));
        dialogBinding.btnQuick100k.setOnClickListener(v -> dialogBinding.etAmount.setText("100000"));
        dialogBinding.btnQuick200k.setOnClickListener(v -> dialogBinding.etAmount.setText("200000"));
        dialogBinding.btnQuick500k.setOnClickListener(v -> dialogBinding.etAmount.setText("500000"));

        dialogBinding.btnCancelContrib.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnConfirmContrib.setOnClickListener(v -> {
            String amountStr = dialogBinding.etAmount.getText() != null ? dialogBinding.etAmount.getText().toString().trim() : "";
            String noteStr = dialogBinding.etNote.getText() != null ? dialogBinding.etNote.getText().toString().trim() : "";

            if (TextUtils.isEmpty(amountStr)) {
                dialogBinding.tilAmount.setError("Ingresa un monto válido");
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    dialogBinding.tilAmount.setError("El monto debe ser mayor a 0");
                    return;
                }

                Contribution contrib = new Contribution(
                        goal.getId(),
                        goal.getTitle(),
                        amount,
                        System.currentTimeMillis(),
                        noteStr
                );

                storageManager.addContribution(contrib);
                dialog.dismiss();
                Toast.makeText(this, "🎉 ¡Aporte de " + currencyFormat.format(amount) + " guardado con éxito!", Toast.LENGTH_LONG).show();
                refreshAllViews();
            } catch (NumberFormatException e) {
                dialogBinding.tilAmount.setError("Número inválido");
            }
        });

        dialog.show();
    }

    // Dialog: Create New Goal
    private void showNewGoalDialog() {
        DialogNewGoalBinding dialogBinding = DialogNewGoalBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        String[] categories = new String[]{"Emergencia", "Viajes", "Vehículo", "Tecnología", "Hogar", "Educación", "General"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        dialogBinding.spGoalCategory.setAdapter(catAdapter);

        dialogBinding.btnCancelGoal.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnCreateGoal.setOnClickListener(v -> {
            String title = dialogBinding.etGoalTitle.getText() != null ? dialogBinding.etGoalTitle.getText().toString().trim() : "";
            String category = dialogBinding.spGoalCategory.getText().toString().trim();
            String targetAmountStr = dialogBinding.etTargetAmount.getText() != null ? dialogBinding.etTargetAmount.getText().toString().trim() : "";
            String monthsStr = dialogBinding.etMonths.getText() != null ? dialogBinding.etMonths.getText().toString().trim() : "";
            String initialStr = dialogBinding.etInitialDeposit.getText() != null ? dialogBinding.etInitialDeposit.getText().toString().trim() : "";

            if (TextUtils.isEmpty(title)) {
                dialogBinding.etGoalTitle.setError("Ingresa un nombre para la meta");
                return;
            }
            if (TextUtils.isEmpty(targetAmountStr)) {
                dialogBinding.etTargetAmount.setError("Ingresa el monto objetivo");
                return;
            }
            if (TextUtils.isEmpty(monthsStr)) {
                dialogBinding.etMonths.setError("Ingresa los meses estimados");
                return;
            }

            try {
                double targetAmount = Double.parseDouble(targetAmountStr);
                int months = Integer.parseInt(monthsStr);
                double initialDeposit = TextUtils.isEmpty(initialStr) ? 0 : Double.parseDouble(initialStr);

                if (targetAmount <= 0 || months <= 0) {
                    Toast.makeText(this, "El monto y los meses deben ser mayores a 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, months);

                SavingGoal newGoal = new SavingGoal(
                        title,
                        category.isEmpty() ? "General" : category,
                        targetAmount,
                        0,
                        cal.getTimeInMillis(),
                        System.currentTimeMillis()
                );

                storageManager.addGoal(newGoal);

                if (initialDeposit > 0) {
                    Contribution initialContrib = new Contribution(
                            newGoal.getId(),
                            newGoal.getTitle(),
                            initialDeposit,
                            System.currentTimeMillis(),
                            "Aporte inicial al crear la meta"
                    );
                    storageManager.addContribution(initialContrib);
                }

                dialog.dismiss();
                Toast.makeText(this, "🎯 ¡Meta \"" + title + "\" creada exitosamente!", Toast.LENGTH_LONG).show();
                refreshAllViews();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Verifica que los montos sean números válidos", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // Goal Adapter Listeners
    @Override
    public void onSelectActive(SavingGoal goal) {
        storageManager.setActiveGoalId(goal.getId());
        Toast.makeText(this, "🎯 Meta principal cambiada a: " + goal.getTitle(), Toast.LENGTH_SHORT).show();
        refreshAllViews();
    }

    @Override
    public void onAddDeposit(SavingGoal goal) {
        showAddContributionDialog(goal);
    }

    @Override
    public void onDeleteGoal(SavingGoal goal) {
        new AlertDialog.Builder(this)
                .setTitle("¿Eliminar Meta?")
                .setMessage("¿Estás seguro de que deseas eliminar la meta \"" + goal.getTitle() + "\"? Se eliminará de tus registros.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    storageManager.deleteGoal(goal.getId());
                    Toast.makeText(this, "Meta eliminada", Toast.LENGTH_SHORT).show();
                    refreshAllViews();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Contribution Adapter Listener
    @Override
    public void onDeleteContribution(Contribution contribution) {
        new AlertDialog.Builder(this)
                .setTitle("¿Eliminar Aporte?")
                .setMessage("¿Deseas eliminar el aporte de " + currencyFormat.format(contribution.getAmount()) + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    storageManager.deleteContribution(contribution.getId());
                    Toast.makeText(this, "Aporte eliminado y saldo actualizado", Toast.LENGTH_SHORT).show();
                    refreshAllViews();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private List<SavingTip> getSavingTipsList() {
        List<SavingTip> tips = new ArrayList<>();
        tips.add(new SavingTip(
                "La Regla 50 / 30 / 20",
                "Estrategia de Presupuesto Balanceado",
                "Divide tus ingresos netos mensuales en 3 categorías: 50% para necesidades fundamentales (arriendo, servicios, mercado), 30% para tus gustos personales y un 20% obligatorio para tu meta de ahorro.",
                R.drawable.ic_lightbulb
        ));
        tips.add(new SavingTip(
                "El Reto de las 52 Semanas",
                "Ahorro Progresivo Automático",
                "Empieza ahorrando $1,000 en la primera semana, $2,000 en la segunda, y así sucesivamente aumentando $1,000 cada semana. ¡Al cabo de un año habrás acumulado casi $1,400,000 de forma indolora!",
                R.drawable.ic_star
        ));
        tips.add(new SavingTip(
                "Fondo de Emergencia Inviolable",
                "Tranquilidad Financiera",
                "Antes de realizar compras de lujo o inversión de alto riesgo, asegura de 3 a 6 meses de tus gastos fijos mensuales en un fondo de alta liquidez para imprevistos médicos o laborales.",
                R.drawable.ic_emergency
        ));
        tips.add(new SavingTip(
                "Elimina los Gastos Hormiga",
                "Pequeñas Fugas, Grandes Pérdidas",
                "Pequeños gustos diarios como tintos, snacks o suscripciones que no usas suman fortunas al mes. Si ahorras $5,000 diarios en gastos hormiga, tendrás $1,800,000 extra al año en tu meta.",
                R.drawable.ic_piggy_bank
        ));
        tips.add(new SavingTip(
                "La Regla de las 48 Horas",
                "Control de Compras Impulsivas",
                "Antes de realizar una compra no planificada superior a $100,000, espera 48 horas obligatorias. En el 80% de los casos te darás cuenta de que no era algo realmente necesario.",
                R.drawable.ic_wallet
        ));
        return tips;
    }
}