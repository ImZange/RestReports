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


public interface ApiService {


    String RESOURCE = "reports";

    @GET(RESOURCE)
    Call<List<Report>> getAllReports();

    @GET(RESOURCE + "/{id}")
    Call<Report> getReportById(@Path("id") int reportId);


    @POST(RESOURCE)
    Call<Report> createReport(@Body Report report);


    @PUT(RESOURCE + "/{id}")
    Call<Report> updateReport(@Path("id") int reportId, @Body Report report);


    @DELETE(RESOURCE + "/{id}")
    Call<Void> deleteReport(@Path("id") int reportId);
}
