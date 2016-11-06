package org.bogdan.remindme.dto;

import com.vk.sdk.api.model.VKList;

import java.util.Collection;
import java.util.List;

/**
 * Created by Bodia on 30.10.2016.
 */
public class RemindDTO  {
    private String title;


    public RemindDTO(String title){
        this.title=title;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }




}
