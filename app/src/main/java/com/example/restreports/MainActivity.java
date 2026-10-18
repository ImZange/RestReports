package com.example.restreports;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restreports.api.ApiErrors;
import com.example.restreports.api.ApiService;
import com.example.restreports.api.RetrofitClient;
import com.example.restreports.model.Report;
import com.example.restreports.ui.ReportAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements ReportAdapter.Listener {

    private static final String TAG = "RestReports";

    private ApiService apiService;
    private ReportAdapter adapter;

    private View root, header, emptyState, progress, btnRefresh;
    private RecyclerView rvReports;
    private TextView tvCount;
    private TextInputLayout tilSearch;
    private TextInputEditText etSearch;
    private ExtendedFloatingActionButton fab;


    private int pendingCalls = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT));
        setContentView(R.layout.activity_main);

        root = findViewById(R.id.main);
        header = findViewById(R.id.header);
        emptyState = findViewById(R.id.emptyState);
        progress = findViewById(R.id.progress);
        btnRefresh = findViewById(R.id.btnRefresh);
        rvReports = findViewById(R.id.rvReports);
        tvCount = findViewById(R.id.tvCount);
        tilSearch = findViewById(R.id.tilSearch);
        etSearch = findViewById(R.id.etSearch);
        fab = findViewById(R.id.fabNew);

        applyWindowInsets();

        adapter = new ReportAdapter(this);
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        rvReports.setAdapter(adapter);

        // Initialize API service instance (Singleton)
        apiService = RetrofitClient.getApiService();

        btnRefresh.setOnClickListener(v -> loadAllReports());
        fab.setOnClickListener(v -> showReportDialog(null));
        tilSearch.setEndIconOnClickListener(v -> searchById());
        etSearch.setOnEditorActionListener((tv, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchById();
                return true;
            }
            return false;
        });

        if (!RetrofitClient.isConfigured()) {

            updateList();
            showError(getString(R.string.error_not_configured), null);
            return;
        }


        loadAllReports();
    }


    private void loadAllReports() {
        etSearch.setText("");
        tilSearch.setError(null);
        run(apiService.getAllReports(), new UiCallback<List<Report>>("GET_ALL", this::loadAllReports) {
            @Override
            void onSuccess(@Nullable List<Report> body) {
                List<Report> list = body == null ? new ArrayList<>() : body;
                Log.d(TAG, "Retrieved " + list.size() + " records.");
                adapter.setItems(list);
                updateList();
            }
        });
    }


    private void searchById() {
        String text = etSearch.getText() == null ? "" : etSearch.getText().toString().trim();
        if (text.isEmpty()) {
            tilSearch.setError(getString(R.string.err_search_empty));
            return;
        }
        final int id;
        try {
            id = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            tilSearch.setError(getString(R.string.err_search_invalid));
            return;
        }
        tilSearch.setError(null);
        hideKeyboard();

        run(apiService.getReportById(id),
                new UiCallback<Report>("GET_BY_ID", MainActivity.this::searchById) {
                    @Override
                    void onSuccess(@Nullable Report body) {
                        List<Report> result = new ArrayList<>();
                        if (body != null) result.add(body);
                        adapter.setItems(result);
                        updateList();
                    }

                    @Override
                    String httpErrorMessage(int code) {
                        // 404 here simply means "that ID does not exist"
                        return code == 404
                                ? getString(R.string.error_report_not_found, id)
                                : super.httpErrorMessage(code);
                    }
                });
    }

    /** POST /posts : create a report. */
    private void createReport(final Report report) {
        run(apiService.createReport(report),
                new UiCallback<Report>("POST", () -> createReport(report)) {
                    @Override
                    void onSuccess(@Nullable Report created) {
                        if (created == null) created = report;
                        Log.i(TAG, "Report created with ID " + created.getId());
                        adapter.addFirst(created);
                        rvReports.scrollToPosition(0);
                        updateList();
                        showMessage(getString(R.string.msg_created, created.getId()));
                    }
                });
    }


    private void updateReport(final Report old, final Report data) {
        run(apiService.updateReport(old.getId(), data),
                new UiCallback<Report>("PUT", () -> updateReport(old, data)) {
                    @Override
                    void onSuccess(@Nullable Report updated) {
                        adapter.replace(old, updated != null ? updated : data);
                        showMessage(getString(R.string.msg_updated, old.getId()));
                    }
                });
    }

    /** DELETE /posts/{id} : remove a report. */
    private void deleteReport(final Report report) {
        run(apiService.deleteReport(report.getId()),
                new UiCallback<Void>("DELETE", () -> deleteReport(report)) {
                    @Override
                    void onSuccess(@Nullable Void body) {
                        adapter.remove(report);
                        updateList();
                        showMessage(getString(R.string.msg_deleted, report.getId()));
                    }
                });
    }


    private <T> void run(Call<T> call, UiCallback<T> callback) {
        startLoading();
        call.enqueue(callback);
    }


    private abstract class UiCallback<T> implements Callback<T> {
        private final String tag;
        private final Runnable retry;

        UiCallback(String tag, @Nullable Runnable retry) {
            this.tag = tag;
            this.retry = retry;
        }

        abstract void onSuccess(@Nullable T body);

        String httpErrorMessage(int code) {
            return ApiErrors.fromHttpCode(MainActivity.this, code);
        }

        @Override
        public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
            stopLoading();
            if (isFinishing() || isDestroyed()) return;
            String request = call.request().method() + " " + call.request().url();
            if (response.isSuccessful()) {
                Log.d(TAG, tag + " -> HTTP " + response.code() + " (" + request + ")");
                onSuccess(response.body());
            } else {
                Log.e(TAG, tag + " -> HTTP error " + response.code() + " (" + request + ")");
                showError(httpErrorMessage(response.code()), retry);
            }
        }

        @Override
        public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
            stopLoading();
            if (call.isCanceled() || isFinishing() || isDestroyed()) return;
            Log.e(TAG, tag + " -> NETWORK_FAILURE: " + t.getClass().getSimpleName()
                    + " - " + t.getMessage(), t);
            showError(ApiErrors.fromThrowable(MainActivity.this, t), retry);
        }
    }


    private void showReportDialog(@Nullable final Report existing) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_report, null);
        final TextInputLayout tilMunicipality = form.findViewById(R.id.tilMunicipality);
        final TextInputLayout tilSymptoms = form.findViewById(R.id.tilSymptoms);
        final TextInputEditText etMunicipality = form.findViewById(R.id.etMunicipality);
        final TextInputEditText etSymptoms = form.findViewById(R.id.etSymptoms);
        final MaterialAutoCompleteTextView ddRisk = form.findViewById(R.id.ddRisk);

        String[] levels = {Report.RISK_LOW, Report.RISK_MEDIUM, Report.RISK_HIGH};
        ddRisk.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, levels));
        String currentRisk = Report.RISK_LOW;

        if (existing != null) {
            etMunicipality.setText(existing.getMunicipality());
            etSymptoms.setText(existing.getSymptomSummary());
            for (String level : levels) {
                if (level.equalsIgnoreCase(existing.getRiskLevel())) currentRisk = level;
            }
        }
        ddRisk.setText(currentRisk, false);

        final AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(existing == null
                        ? getString(R.string.dialog_new_title)
                        : getString(R.string.dialog_edit_title, existing.getId()))
                .setView(form)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, null)   // overridden below
                .create();
        dialog.show();

        // Overriding the click listener keeps the dialog open when validation fails
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String municipality = etMunicipality.getText() == null ? "" : etMunicipality.getText().toString().trim();
            String symptoms = etSymptoms.getText() == null ? "" : etSymptoms.getText().toString().trim();
            String risk = ddRisk.getText().toString().trim();

            tilMunicipality.setError(municipality.isEmpty() ? getString(R.string.err_municipality_required) : null);
            tilSymptoms.setError(symptoms.isEmpty() ? getString(R.string.err_symptoms_required) : null);
            if (municipality.isEmpty() || symptoms.isEmpty()) return;

            Report data = new Report(municipality, symptoms, risk);
            dialog.dismiss();
            if (existing == null) {
                createReport(data);
            } else {
                updateReport(existing, data);
            }
        });
    }

    @Override
    public void onEdit(Report report) {
        showReportDialog(report);
    }

    @Override
    public void onDelete(final Report report) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, report.getId()))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (d, which) -> deleteReport(report))
                .show();
    }

    // ------------------------------------------------------------------ UI HELPERS

    private void startLoading() {
        pendingCalls++;
        if (pendingCalls == 1) {
            progress.setVisibility(View.VISIBLE);
            progress.announceForAccessibility(getString(R.string.loading));   // TalkBack
            btnRefresh.setEnabled(false);
            fab.setEnabled(false);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void stopLoading() {
        pendingCalls = Math.max(0, pendingCalls - 1);
        if (pendingCalls == 0) {
            progress.setVisibility(View.GONE);
            btnRefresh.setEnabled(true);
            fab.setEnabled(true);
        }
    }

    private void updateList() {
        int n = adapter.getItemCount();
        tvCount.setText(getResources().getQuantityString(R.plurals.report_count, n, n));
        boolean empty = n == 0 && pendingCalls == 0;
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvReports.setVisibility(n == 0 ? View.GONE : View.VISIBLE);
    }

    private void showMessage(String message) {
        Snackbar.make(root, message, Snackbar.LENGTH_SHORT).setAnchorView(fab).show();
    }

    private void showError(String message, @Nullable Runnable retry) {
        updateList();
        Snackbar bar = Snackbar.make(root, message, Snackbar.LENGTH_LONG).setAnchorView(fab);
        if (retry != null) {
            bar.setAction(R.string.action_retry, v -> retry.run());
        }
        bar.show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = getSystemService(InputMethodManager.class);
        if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
    }

    private void applyWindowInsets() {
        final int side = dp(20);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, 0, bars.right, 0);
            header.setPadding(side, bars.top + dp(16), side, dp(24));
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
            lp.bottomMargin = dp(20) + bars.bottom;
            fab.setLayoutParams(lp);
            rvReports.setPadding(side, dp(8), side, dp(96) + bars.bottom);
            return insets;
        });
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
