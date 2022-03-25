package com.example.passwordmanager;

import java.util.ArrayList;

public class Global {

    public static ArrayList<byte[]> bytes = new ArrayList<>();
    public static void addBytes(byte[] imageByte) {
        bytes.add(imageByte);
    }

    public static ArrayList<byte[]> getBytes() {
        return bytes;
    }

    public static void clearBytes() {
        bytes.clear();
    }


    public static ArrayList<String> links = new ArrayList<>();
    public static void addLink(String link) {
        links.add(link);
    }

    public static ArrayList<String> getLinks() {
        return links;
    }

    public static void clearLinks() {
        links.clear();
    }
}
