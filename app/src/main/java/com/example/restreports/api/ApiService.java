package com.example.restreports.api;

import com.example.restreports.model.Report;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * REST API contract. Every HTTP verb is declared with a Retrofit annotation.
 */
public interface ApiService {

    /**
     * Resource path of the collection. It must match the resource name created in mockapi.io
     * (or the "reports" key of backend/db.json when using json-server).
     * (The public jsonplaceholder server has no /reports endpoint and returns Lorem Ipsum
     * text, so it is NOT used by default. To try it, set RESOURCE = "posts".)
     */
    String RESOURCE = "reports";

    // GET: retrieve all monitoring reports
    @GET(RESOURCE)
    Call<List<Report>> getAllReports();

    // GET: retrieve a specific report by ID
    @GET(RESOURCE + "/{id}")
    Call<Report> getReportById(@Path("id") int reportId);

    // POST: create a new report entry
    @POST(RESOURCE)
    Call<Report> createReport(@Body Report report);

    // PUT: replace an existing report
    @PUT(RESOURCE + "/{id}")
    Call<Report> updateReport(@Path("id") int reportId, @Body Report report);

    // DELETE: remove a report by ID
    @DELETE(RESOURCE + "/{id}")
    Call<Void> deleteReport(@Path("id") int reportId);
}
