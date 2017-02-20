package org.bogdan.remindme.util;

import org.bogdan.remindme.content.User;
import org.bogdan.remindme.content.UserVK;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;


/**
 * Created by Bodia on 09.02.2017.
 */

public interface RemindMeRestAPI {
    @POST("/user/save")
    Call<Integer> saveUser(@Body List<User> users);

    @GET("/user/get/all")
    Call<List<UserVK>> getUsers();
}
