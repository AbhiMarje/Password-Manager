package com.example.passwordmanager.Model;

import java.util.ArrayList;

public class User {

    String name;
    String email;
    ArrayList<String> domain;

    public User(String name, String email, ArrayList<String> domain) {
        this.name = name;
        this.email = email;
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<String> getDomain() {
        return domain;
    }

    public void setDomain(ArrayList<String> domain) {
        this.domain = domain;
    }
}
