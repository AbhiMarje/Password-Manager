package com.example.passwordmanager;

import java.util.ArrayList;

public class Global {

    public static ArrayList<String> bytes = new ArrayList<>();
    public static void addBytes(String imageByte) {
        bytes.add(imageByte);
    }

    public static ArrayList<String> getBytes() {
        return bytes;
    }

    public static void clearBytes() {
        bytes.clear();
    }
}
