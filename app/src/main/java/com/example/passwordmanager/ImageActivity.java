package com.example.passwordmanager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

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
    TextView selectedImageCount;
    ArrayList<byte[]> bytes;
    ArrayList<String> images;
    App app;
    byte[] key = {59, -1, -22, 15, 7, 69, -21, -13, 44, -9, 56, 105, -82, 44, -23, 80};

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

        arrayList = new ArrayList<>();
        app = new App(new AppConfiguration.Builder(app_Id).build());
        User user = app.currentUser();

        if (extras.getString("isNewUser").equals("true")) {

            if (user != null) {
                MongoClient client = user.getMongoClient("mongodb-atlas");
                MongoDatabase database = client.getDatabase("manager");

                RealmResultTask<MongoCursor<Document>> cursor = database.getCollection("images").find().iterator();
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
                                    images.add(new String(cipher.doFinal(Base64.getDecoder().decode(encoded.get(i)))));
                                }
                            }

                        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                            e.printStackTrace();
                        }

                        RealmResultTask<MongoCursor<Document>> cursor = database.getCollection("images").find().iterator();
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

//                                SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
//
//                                Set<HashMap<String, Set<HashMap<String, String>>>> getSet = new HashSet<>();
//                                getSet.addAll(sharedPreferences.getStringSet("domain", (HashSet) new HashSet<HashMap<String, Set<HashMap<String, String>>>>()));
//                                Log.e("stored", getSet.toString());
//
//                                try {
//                                    SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
//                                    Cipher cipher = Cipher.getInstance("AES");
//                                    cipher.init(Cipher.DECRYPT_MODE, keySpec);
//
//                                    for (HashMap<String, Set<HashMap<String, String>>> hashMap : getSet) {
//                                        for (Map.Entry<String, Set<HashMap<String, String>>> set1 : hashMap.entrySet()) {
//                                            if (set1.getKey().equals(extras.getString("email"))) {
//
//                                                for (HashMap<String, String> map2 : set1.getValue()) {
//                                                    for (Map.Entry<String, String> map3 : map2.entrySet()) {
//
//                                                        Log.e("tag", "came to loop" + " " + map3.getKey() + " " + extras.getString("domain"));
//                                                        if (map3.getKey().equals(extras.getString("domain"))) {
//
//                                                            Log.e("tag", "Came here");
//
//                                                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ImageActivity.this);
//                                                            alertDialog.setMessage("Do you want to Autofill the password")
//                                                                    .setCancelable(false)
//                                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                                                                        @Override
//                                                                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                                                                            try {
//                                                                                if (images.get(6).equals(Base64.getEncoder().encodeToString(cipher.doFinal(Base64.getDecoder().decode(map3.getValue()))))) {
//                                                                                    Intent intent = new Intent(ImageActivity.this, HomeActivity.class);
//                                                                                    startActivity(intent);
//                                                                                }
//                                                                            } catch (BadPaddingException | IllegalBlockSizeException e) {
//                                                                                e.printStackTrace();
//                                                                            }
//
//                                                                        }
//                                                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
//                                                                        @Override
//                                                                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                                                                            Toast.makeText(ImageActivity.this, "Do it yourself", Toast.LENGTH_SHORT).show();
//
//                                                                        }
//                                                                    });
//
//                                                            AlertDialog alert = alertDialog.create();
//                                                            alert.setTitle("AlertDialogExample");
//                                                            alert.show();
//                                                            Log.e("Hash", set1.getKey() + "  " + Base64.getEncoder().encodeToString(cipher.doFinal(Base64.getDecoder().decode(map3.getValue()))));
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
//                                    e.printStackTrace();
//                                }
//
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

                if (images.get(6).equals(Base64.getEncoder().encodeToString(hash))) {

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

                Global.addLink(Base64.getEncoder().encodeToString(cipher.doFinal(selected.get(0).getBytes())));

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

                hashValues.add(Base64.getEncoder().encodeToString(hash));

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

                            Global.addLink(Base64.getEncoder().encodeToString(cipher.doFinal(selected.get(0).getBytes())));

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

        progressBar.setVisibility(View.GONE);

        selectedImageCount.setText(String.valueOf(Global.getBytes().size()));
    }
}