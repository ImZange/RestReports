package com.example.restreports.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Data Transfer Object (POJO) that mirrors the JSON returned by the REST API.
 *
 * <p>Our backend (backend/db.json) exposes "reports" with the fields
 * {@code id, municipality, symptomSummary, riskLevel}. The {@code alternate} names
 * additionally let the same model read jsonplaceholder "posts" ({@code title, body,
 * userId}), which is handy for quick tests.</p>
 */
public class Report {

    public static final String RISK_LOW = "Low";
    public static final String RISK_MEDIUM = "Medium";
    public static final String RISK_HIGH = "High";

    // Integer (not int) so that a new report is sent WITHOUT an "id" field:
    // Gson skips nulls, whereas a primitive int would be serialized as "id":0.
    @SerializedName("id")
    private Integer id;

    // Only present in the demo API; used to derive a risk level for display.
    @SerializedName("userId")
    private Integer userId;

    @SerializedName(value = "municipality", alternate = {"title"})
    private String municipality;

    @SerializedName(value = "symptomSummary", alternate = {"body"})
    private String symptomSummary;

    @SerializedName("riskLevel")
    private String riskLevel;

    // Constructor for creating new records (POST / PUT)
    public Report(String municipality, String symptomSummary, String riskLevel) {
        this.municipality = municipality;
        this.symptomSummary = symptomSummary;
        this.riskLevel = riskLevel;
    }

    // Getters
    public int getId() {
        return id == null ? 0 : id;
    }

    public String getMunicipality() {
        return municipality == null ? "" : municipality;
    }

    public String getSymptomSummary() {
        return symptomSummary == null ? "" : symptomSummary;
    }

    /**
     * Returns the risk level sent by the server. The demo API has no such field,
     * so in that case a Low/Medium/High value is derived from {@code userId}
     * just to make the UI meaningful.
     */
    @NonNull
    public String getRiskLevel() {
        if (riskLevel != null && !riskLevel.trim().isEmpty()) {
            return riskLevel;
        }
        if (userId != null) {
            switch (userId % 3) {
                case 0:
                    return RISK_HIGH;
                case 1:
                    return RISK_LOW;
                default:
                    return RISK_MEDIUM;
            }
        }
        return "Unknown";
    }
}
