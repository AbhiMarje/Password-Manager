package com.example.passwordmanager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.passwordmanager.Adapter.ImageAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bson.Document;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class ImageActivity extends AppCompatActivity {

    String app_Id = "password-manager-izhmp";
    ArrayList<String> arrayList;
    RecyclerView recyclerView;
    ImageAdapter adapter;
    FloatingActionButton button;
    ProgressBar progressBar;
    TextView selectedImageCount, forgetPassword;
    ArrayList<byte[]> bytes;
    ArrayList<String> images;
    App app;
    byte[] key = {59, -1, -22, 15, 7, 69, -21, -13, 44, -9, 56, 105, -82, 44, -23, 80};
    int randomPin;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        init();
        listeners();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void listeners() {

        Bundle extras = getIntent().getExtras();
        Log.e("tag", "new" + extras.getString("isNewUser"));
        Log.e("tag", "email" + extras.getString("email"));
        Log.e("tag", "type" + extras.getString("type"));

        arrayList = new ArrayList<>();
        app = new App(new AppConfiguration.Builder(app_Id).build());
        User user = app.currentUser();

        if (extras.getString("isNewUser").equals("true")) {

            if (user != null) {
                MongoClient client = user.getMongoClient("mongodb-atlas");
                MongoDatabase database = client.getDatabase("manager");
                String collectionName = "images";
                if (extras.getString("type").equals("Fruits")) {
                    collectionName = "images";
                }else if (extras.getString("type").equals("Chocolates")) {
                    collectionName = "chocolate";
                }else if (extras.getString("type").equals("Sports")) {
                    collectionName = "sports";
                }

                Log.e("tagC", collectionName);

                RealmResultTask<MongoCursor<Document>> cursor = database.getCollection(collectionName).find().iterator();
                cursor.getAsync(task -> {
                    if (task.isSuccess()) {
                        MongoCursor<Document> results = task.get();
                        while (results.hasNext()) {
                            Document currentDoc = results.next();
                            if (currentDoc.getString("pic") != null) {
                                arrayList.add(currentDoc.getString("pic"));
                            } else {
                                Log.e("tag", "Doc empty");
                            }
                        }
                        Collections.shuffle(arrayList);
                        adapter = new ImageAdapter(arrayList.subList(0, 15), this);
                        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(0);
                    }
                });
            } else {
                Log.e("tag", "user null");
            }
        } else {

            if (user != null) {
                MongoClient client = user.getMongoClient("mongodb-atlas");
                MongoDatabase database = client.getDatabase("manager");
                Document document = new Document().append("email", extras.getString("email"));

                database.getCollection("users").findOne(document).getAsync(task -> {
                    if (task.isSuccess()) {

                        images = new ArrayList<>();
                        ArrayList<String> encoded = new ArrayList<>((ArrayList<String>) task.get().get(extras.getString("domain")));

                        try {
                            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
                            Cipher cipher = Cipher.getInstance("AES");
                            cipher.init(Cipher.DECRYPT_MODE, keySpec);

                            for (int i = 0; i < encoded.size(); i++) {
                                if (i == 6) {
                                    images.add(encoded.get(i));
                                }else {
                                    try {
                                        images.add(new String(cipher.doFinal(Hex.decodeHex(encoded.get(i)))));
                                    } catch (DecoderException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                            e.printStackTrace();
                        }

                        String collectionName = "images";
                        if (task.get().getString(extras.getString("domain")+"Type").equals("Fruits")) {
                            collectionName = "images";
                        }else if (task.get().getString(extras.getString("domain")+"Type").equals("Chocolates")) {
                            collectionName = "chocolate";
                        }else if (task.get().getString(extras.getString("domain")+"Type").equals("Sports")) {
                            collectionName = "sports";
                        }

                        Log.e("tagC", collectionName);

                        RealmResultTask<MongoCursor<Document>> cursor = database.getCollection(collectionName).find().iterator();
                        cursor.getAsync(task1 -> {
                            if (task1.isSuccess()) {
                                MongoCursor<Document> results = task1.get();
                                while (results.hasNext()) {
                                    Document currentDoc = results.next();
                                    if (currentDoc.getString("pic") != null) {
                                        arrayList.add(currentDoc.getString("pic"));
                                    } else {
                                        Log.e("tag", "Doc empty");
                                    }
                                }
                                Collections.shuffle(arrayList);

                                ArrayList<String> newImages;
                                if (arrayList.subList(0, 15).contains(images.get(Global.getLinks().size()))) {
                                    newImages = new ArrayList<>(arrayList.subList(0, 15));
                                } else {
                                    newImages = new ArrayList<>(arrayList.subList(0, 14));
                                    newImages.add(images.get(Global.getLinks().size()));
                                }

                                Collections.shuffle(newImages);

                                adapter = new ImageAdapter(newImages, this);
                                recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
                                recyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                recyclerView.smoothScrollToPosition(0);

                                SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

                                if (Global.isAutoFill) {
                                    try {
                                        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
                                        Cipher cipher = Cipher.getInstance("AES");
                                        cipher.init(Cipher.DECRYPT_MODE, keySpec);

                                        Set<HashMap<String, HashMap<String, String>>> mainSet = new HashSet<>(sharedPreferences.getStringSet("domain", (HashSet) new HashSet<>()));

                                        for (HashMap<String, HashMap<String, String>> map1 : mainSet) {

                                            for (Map.Entry<String, HashMap<String, String>> map2 : map1.entrySet()) {

                                                if (map2.getKey().equals(extras.getString("email"))) {

                                                    for (Map.Entry<String, String> map3 : map2.getValue().entrySet()) {

                                                        if (map3.getKey().equals(extras.getString("domain"))) {
                                                            try {
                                                                Log.e("hash2", Hex.encodeHexString(cipher.doFinal(Hex.decodeHex(map3.getValue()))));
                                                            } catch (DecoderException e) {
                                                                e.printStackTrace();
                                                            }
                                                            Log.e("hash2", images.get(6));

                                                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ImageActivity.this);
                                                            alertDialog.setMessage("Do you want to Autofill the password")
                                                                    .setCancelable(false)
                                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                                            try {
                                                                                try {
                                                                                    if (images.get(6).equals(Hex.encodeHexString(cipher.doFinal(Hex.decodeHex(map3.getValue()))))) {
                                                                                        Intent intent = new Intent(ImageActivity.this, HomeActivity.class);
                                                                                        startActivity(intent);
                                                                                    }else {
                                                                                        Global.makeAutoFillFalse();
                                                                                    }
                                                                                } catch (DecoderException e) {
                                                                                    e.printStackTrace();
                                                                                }
                                                                            } catch (BadPaddingException | IllegalBlockSizeException e) {
                                                                                e.printStackTrace();
                                                                            }

                                                                        }
                                                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                                    Global.makeAutoFillFalse();
                                                                    Toast.makeText(ImageActivity.this, "Please select the images", Toast.LENGTH_SHORT).show();

                                                                }
                                                            });

                                                            AlertDialog alert = alertDialog.create();
                                                            alert.show();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        });

                    } else {
                        Log.e("tag", "Cant find domain");
                    }
                });

            }

        }

        button.setOnClickListener((View v) -> {
            if (Global.getBytes().size() < 6) {
                if (extras.getString("isNewUser").equals("true")) {
                    getBytes();
                } else {
                    checkBytes();
                }
            } else {
                if (extras.getString("isNewUser").equals("true")) {
                    generateFinalHash();
                } else {
                    showResult();
                }
            }
        });

        forgetPassword.setOnClickListener((View v) -> {
            String email = "2gi19ec067@students.git.edu";
            String password = "345@hb345@hb";

            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", "587");

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(email, password);
                }
            });

            try {
                randomPin = (int) (Math.random()*9000)+1000;
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(email));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(extras.getString("email", "")));
                message.setSubject("Password reset");
                message.setText("Your otp is " + String.valueOf(randomPin));
                new SendMail().execute(message);

            } catch (MessagingException e) {
                e.printStackTrace();
            }

        });

    }

    private class SendMail extends AsyncTask<Message, String, String> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ImageActivity.this, "Please Wait", "Sending OTP...", true, false);

        }

        @Override
        protected String doInBackground(Message... messages) {
            try {
                Transport.send(messages[0]);
                return "Success";
            } catch (MessagingException e) {
                e.printStackTrace();
                return "Error";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            if (s.equals("Success")) {
                Bundle bundle = getIntent().getExtras();

                Intent intent = new Intent(ImageActivity.this, OTPActivity.class);
                intent.putExtra("otp", String.valueOf(randomPin));
                intent.putExtra("email", bundle.getString("email", ""));
                intent.putExtra("domain", bundle.getString("domain", ""));
                intent.putExtra("type", bundle.getString("type", ""));
                startActivity(intent);
            }else {
                Toast.makeText(ImageActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showResult() {

        if (Global.getBytes().size() == 6) {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            for (byte[] bytes : Global.getBytes()) {

                try {
                    byteArrayOutputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                byte[] finalByteArray = byteArrayOutputStream.toByteArray();
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                byte[] hash = messageDigest.digest(finalByteArray);

                Log.e("image", images.get(6));
                Log.e("imageHash", Hex.encodeHexString(hash));

                if (images.get(6).equals(Hex.encodeHexString(hash))) {

                    Bundle bundle = getIntent().getExtras();

                    try {

                        Log.e("hash", Hex.encodeHexString(hash));

                        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
                        Cipher cipher = Cipher.getInstance("AES");
                        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

                        Cipher decipher = Cipher.getInstance("AES");
                        decipher.init(Cipher.DECRYPT_MODE, keySpec);

                        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        Set<HashMap<String, HashMap<String, String>>> mainSet = new HashSet<>(sh.getStringSet("domain", (HashSet) new HashSet<>()));
                        Log.e("data", mainSet.toString());

                        HashMap<String, String> childMap = new HashMap<>();

                        if (mainSet.size() == 0) {
                            Set<HashMap<String, HashMap<String, String>>> pushSet = new HashSet<>();
                            HashMap<String, HashMap<String, String>> newMap = new HashMap<>();
                            childMap.put(bundle.getString("domain"), Hex.encodeHexString(cipher.doFinal(hash)));
                            newMap.put(bundle.getString("email"), childMap);
                            pushSet.add(newMap);
                            editor.putStringSet("domain", (HashSet) pushSet);
                            editor.apply();
                        }

                        for (HashMap<String, HashMap<String, String>> map1 : mainSet) {
                            boolean isNewUser = true;
                            for (Map.Entry<String, HashMap<String, String>> map2 : map1.entrySet()) {

                                if (map2.getKey().equals(bundle.getString("email"))) {
                                    isNewUser = false;
                                    boolean isNewDomain = true;
                                    for (Map.Entry<String, String> map3 : map2.getValue().entrySet()) {
                                        childMap.put(map3.getKey(), map3.getValue());
                                        if (map3.getKey().equals(bundle.getString("domain"))) {
                                            if (bundle.getString("isReset","").equals("true")) {
                                                isNewDomain = true;
                                            }else {
                                                isNewDomain = false;
                                            }
                                            try {
                                                Log.e("hash2", Hex.encodeHexString(decipher.doFinal(Hex.decodeHex(map3.getValue()))));
                                            } catch (DecoderException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    if (isNewDomain) {
                                        Set<HashMap<String, HashMap<String, String>>> pushSet = new HashSet<>();
                                        HashMap<String, HashMap<String, String>> newMap = new HashMap<>();
                                        childMap.put(bundle.getString("domain"), Hex.encodeHexString(cipher.doFinal(hash)));
                                        newMap.put(bundle.getString("email"), childMap);
                                        pushSet.add(newMap);
                                        editor.putStringSet("domain", (HashSet) pushSet);
                                        editor.apply();
                                    }
                                }
                            }
                            if (isNewUser) {
                                Set<HashMap<String, HashMap<String, String>>> pushSet = new HashSet<>();
                                HashMap<String, HashMap<String, String>> newMap = new HashMap<>();
                                childMap.put(bundle.getString("domain"), Hex.encodeHexString(cipher.doFinal(hash)));
                                newMap.put(bundle.getString("email"), childMap);
                                pushSet.add(newMap);
                                editor.putStringSet("domain", (HashSet) pushSet);
                                editor.apply();
                            }

                        }

                        Log.e("data2", sh.getStringSet("domain", new HashSet<>()).toString());


                    } catch (NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                        e.printStackTrace();
                    }

//                    Bundle bundle = getIntent().getExtras();
//
//                    try {
//                        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
//                        Cipher cipher = Cipher.getInstance("AES");
//                        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
//
//                        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
//                        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//
//                        Set<HashMap<String, HashMap<String, String>>> mainSet = new HashSet<>(sh.getStringSet("domain", (HashSet) new HashSet<>()));
//                        editor.putString("email", bundle.getString("email"));
//
//                        HashMap<String, String> set = new HashMap<>();
//                        set.put(bundle.getString("domain", ""), Base64.getEncoder().encodeToString(cipher.doFinal(hash)));
//                        set.add(domains1);
//
//                        HashMap<String, Set<HashMap<String, String>>> childSet = new HashMap<>();
//                        Log.e("tag", bundle.getString("email", ""));
//                        childSet.put(bundle.getString("email", ""), set);
//                        mainSet.add(childSet);
//                        editor.putStringSet("domain", (HashSet) mainSet);
//
//                        Log.e("tag", sh.getStringSet("domain", new HashSet<>()).toString());
//
//                        Log.e("hash", Base64.getEncoder().encodeToString(hash));
//
//                        editor.apply();
//
//                        cipher.init(Cipher.DECRYPT_MODE, keySpec);
//
//                        Set<HashMap<String, Set<HashMap<String, String>>>> getSet = new HashSet<>();
//                        getSet.addAll(sh.getStringSet("domain", (HashSet) new HashSet<HashMap<String, Set<HashMap<String, String>>>>()));
//                        Log.e("hash", getSet.toString());
//
//                        for (HashMap<String, Set<HashMap<String, String>>> hashMap : getSet) {
//                            for (Map.Entry<String, Set<HashMap<String, String>>> set1 : hashMap.entrySet()) {
//                                if (set1.getKey().equals(bundle.getString("email"))) {
//
//                                    for (HashMap<String, String> map2 : set1.getValue()) {
//                                        for (Map.Entry<String, String> map3 : map2.entrySet()) {
//
//                                            if (map3.getKey().equals(bundle.getString("domain"))) {
//                                                Log.e("Hash", set1.getKey() + "  " + Base64.getEncoder().encodeToString(cipher.doFinal(Base64.getDecoder().decode(map3.getValue()))));
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                    } catch (NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
//                        e.printStackTrace();
//                    }

                    Log.e("tag", "Correct");
                    Intent intent = new Intent(ImageActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Wrong Password Please try again", Toast.LENGTH_SHORT).show();
                    Log.e("tag", "not Correct");
                }

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

        } else {
            Log.e("Error", "bytes not found");
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkBytes() {

        Log.e("tag", "clicked");

        ArrayList<String> selected = adapter.getSelected();

        if (Global.getBytes().size() < 6) {
            Log.e("image", images.get(Global.getBytes().size()));

            try {
                SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);

                Global.addLink(Hex.encodeHexString(cipher.doFinal(selected.get(0).getBytes())));

            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            }

            Runnable runnable = () -> {
                try {
                    Global.addBytes(Glide.with(this).as(byte[].class).load(selected.get(0)).submit().get());
                    for (byte[] bytes : Global.getBytes()) {
                        Log.e("bytes", Arrays.toString(bytes));
                    }

                    ImageActivity.this.runOnUiThread(this::update);

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            };
            new Thread(runnable).start();

        } else if (Global.getBytes().size() == 5) {
            selectedImageCount.setText(String.valueOf(Global.getBytes().size()));
            button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_check_24));
        }

    }

    private void update() {
        if (Global.getBytes().size() != 6) {
            selectedImageCount.setText(String.valueOf(Global.getBytes().size()));
            ImageActivity.this.recreate();
        }else {
            selectedImageCount.setText(String.valueOf(Global.getBytes().size()));
            button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_check_24));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createHash() {

        if (bytes.size() == 1) {

            if (Global.getLinks().size() < 6) {
                for (String link : Global.getLinks()) {
                    Log.e("link", link);
                }
                ImageActivity.this.recreate();
            } else if (Global.getLinks().size() == 6) {
                selectedImageCount.setText(String.valueOf(Global.getLinks().size()));
                button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_check_24));
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void generateFinalHash() {

        if (Global.getBytes().size() == 6) {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            for (byte[] bytes : Global.getBytes()) {
                try {
                    byteArrayOutputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ArrayList<String> hashValues = new ArrayList<>(Global.getLinks());

            try {
                byte[] finalByteArray = byteArrayOutputStream.toByteArray();
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                byte[] hash = messageDigest.digest(finalByteArray);

                Log.e("hash", Arrays.toString(hash));

                hashValues.add(Hex.encodeHexString(hash));

                User user = app.currentUser();
                MongoClient client = user.getMongoClient("mongodb-atlas");
                MongoDatabase database = client.getDatabase("manager");
                Bundle extras = getIntent().getExtras();
                Document document = new Document().append("email", extras.getString("email"));
                Document update = new Document("$set", new Document(extras.getString("domain"), hashValues));

                database.getCollection("users").updateOne(document, update).getAsync(task -> {
                    if (task.isSuccess()) {
                        long count = task.get().getModifiedCount();
                        if (count == 1) {
                            Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ImageActivity.this, HomeActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Failed try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                Bundle bundle = getIntent().getExtras();

                if (bundle.getString("isReset", "").equals("true")) {
                    try {

                        Log.e("hash",   Hex.encodeHexString(hash));

                        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
                        Cipher cipher = Cipher.getInstance("AES");
                        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

                        Cipher decipher = Cipher.getInstance("AES");
                        decipher.init(Cipher.DECRYPT_MODE, keySpec);

                        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        Set<HashMap<String, HashMap<String, String>>> mainSet = new HashSet<>(sh.getStringSet("domain", (HashSet) new HashSet<>()));
                        Log.e("data", mainSet.toString());

                        HashMap<String, String> childMap = new HashMap<>();

                        if (mainSet.size() == 0) {
                            Set<HashMap<String, HashMap<String, String>>> pushSet = new HashSet<>();
                            HashMap<String, HashMap<String, String>> newMap = new HashMap<>();
                            childMap.put(bundle.getString("domain"), Hex.encodeHexString(cipher.doFinal(hash)));
                            newMap.put(bundle.getString("email"), childMap);
                            pushSet.add(newMap);
                            editor.putStringSet("domain", (HashSet) pushSet);
                            editor.apply();
                        }

                        for (HashMap<String, HashMap<String, String>> map1 : mainSet) {
                            boolean isNewUser = true;
                            for (Map.Entry<String, HashMap<String, String>> map2 : map1.entrySet()) {

                                if (map2.getKey().equals(bundle.getString("email"))) {
                                    isNewUser = false;
                                    boolean isNewDomain = true;
                                    for (Map.Entry<String, String> map3 : map2.getValue().entrySet()) {
                                        childMap.put(map3.getKey(), map3.getValue());
                                        if (map3.getKey().equals(bundle.getString("domain"))) {
                                            if (bundle.getString("isReset", "").equals("true")) {
                                                isNewDomain = true;
                                            } else {
                                                isNewDomain = false;
                                            }
                                            try {
                                                Log.e("hash2", Hex.encodeHexString(decipher.doFinal(Hex.decodeHex(map3.getValue()))));
                                            } catch (DecoderException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    if (isNewDomain) {
                                        Set<HashMap<String, HashMap<String, String>>> pushSet = new HashSet<>();
                                        HashMap<String, HashMap<String, String>> newMap = new HashMap<>();
                                        childMap.put(bundle.getString("domain"), Hex.encodeHexString(cipher.doFinal(hash)));
                                        newMap.put(bundle.getString("email"), childMap);
                                        pushSet.add(newMap);
                                        editor.putStringSet("domain", (HashSet) pushSet);
                                        editor.apply();
                                    }
                                }
                            }
                            if (isNewUser) {
                                Set<HashMap<String, HashMap<String, String>>> pushSet = new HashSet<>();
                                HashMap<String, HashMap<String, String>> newMap = new HashMap<>();
                                childMap.put(bundle.getString("domain"), Hex.encodeHexString(cipher.doFinal(hash)));
                                newMap.put(bundle.getString("email"), childMap);
                                pushSet.add(newMap);
                                editor.putStringSet("domain", (HashSet) pushSet);
                                editor.apply();
                            }

                        }

                        Log.e("data2", sh.getStringSet("domain", new HashSet<>()).toString());


                    } catch (NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                        e.printStackTrace();
                    }
                }

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getBytes() {
        progressBar.setVisibility(View.VISIBLE);
        ArrayList<String> selected = adapter.getSelected();
        Log.e("tag", selected.toString());
        bytes = new ArrayList<>();

        if (selected.size() == 1) {

            Runnable runnable = () -> {
                try {
                    byte[] bytes1 = Glide.with(ImageActivity.this).as(byte[].class).load(selected.get(0)).submit().get();
                    Global.addBytes(bytes1);
                    bytes.add(bytes1);

                    for (byte[] bytes : Global.getBytes()) {
                        Log.e("byte", Arrays.toString(bytes));
                    }

                    if (bytes.size() == 1) {

                        try {
                            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
                            Cipher cipher = Cipher.getInstance("AES");
                            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

                            Global.addLink(Hex.encodeHexString(cipher.doFinal(selected.get(0).getBytes())));

                        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                            e.printStackTrace();
                        }

                        ImageActivity.this.runOnUiThread(this::createHash);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            };
            new Thread(runnable).start();

        } else {
            Toast.makeText(this, "Please select 1 images", Toast.LENGTH_SHORT).show();
        }
        progressBar.setVisibility(View.GONE);
    }

    private void init() {
        recyclerView = findViewById(R.id.image_recycler_view);
        button = findViewById(R.id.img_next_btn);
        progressBar = findViewById(R.id.img_progressBar);
        selectedImageCount = findViewById(R.id.selectedImages);
        forgetPassword = findViewById(R.id.forgetPassword);

        progressBar.setVisibility(View.GONE);

        selectedImageCount.setText(String.valueOf(Global.getBytes().size()));
    }
}