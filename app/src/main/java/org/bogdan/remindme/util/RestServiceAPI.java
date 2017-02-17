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

public interface RestServiceAPI {
     static final String NAME_KEY = "name";
     static final String ID_KEY = "id";
     static final String BIRTH_DATE_KEY = "bdate";
     static final String DATE_FORMAT_KEY = "df";
     static final String AVATAR_URL_KEY = "url";
     static final String NOTIFY_KEY = "notify";

    @POST("/user/save")
    Call<User> saveUser(@Body User user);

    @GET("/user/get/all")
    Call<List<UserVK>> getUsers();
}
