package com.qoobico.remindme.dto;

/**
 * Created by Bodia on 30.10.2016.
 */
public class RemindDTO {
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
