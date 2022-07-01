package com.example.bebaagua.model;

public class InfoList {

    int id;
    public String title;
    public String text;

    public InfoList(int id, String title, String text){
        this.id = id;
        this.title = title;
        this.text = text;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
    }
}
