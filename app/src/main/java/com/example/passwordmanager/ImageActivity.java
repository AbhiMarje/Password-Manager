package com.example.passwordmanager;

import static java.security.AccessController.getContext;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.passwordmanager.Adapter.ImageAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.bson.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

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
        }else {

            if (user != null) {
                MongoClient client = user.getMongoClient("mongodb-atlas");
                MongoDatabase database = client.getDatabase("manager");
                Document document = new Document().append("email", extras.getString("email"));

                database.getCollection("users").findOne(document).getAsync(task -> {
                    if (task.isSuccess()) {

                        images = new ArrayList<>();
                        images.addAll((ArrayList<String>) task.get().get(extras.getString("domain")));

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

                                ArrayList newImages;
                                if (arrayList.subList(0,15).contains(images.get(Global.getLinks().size()))) {
                                    newImages = new ArrayList<>(arrayList.subList(0, 15));
                                }else {
                                    newImages = new ArrayList<>(arrayList.subList(0, 14));
                                    newImages.add(images.get(Global.getLinks().size()));
                                }

                                Collections.shuffle(newImages);

                                adapter = new ImageAdapter(newImages, this);
                                recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
                                recyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                recyclerView.smoothScrollToPosition(0);
                            }
                        });

                    }else {
                        Log.e("tag", "Cant find domain");
                    }
                });

            }

        }

        button.setOnClickListener((View v) -> {
            if (Global.getBytes().size() < 5) {
                if (extras.getString("isNewUser").equals("true")) {
                    getBytes();
                }else {
                    checkBytes();
                }
            }else {
                if (extras.getString("isNewUser").equals("true")) {
                    generateFinalHash();
                }else {
                    showResult();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showResult() {

        if (Global.getBytes().size() == 5) {

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

                if (images.get(5).equals(Base64.getEncoder().encodeToString(hash))) {
                    Log.e("tag", "Correct");
                }else {
                    Log.e("tag", "not Correct");
                }

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

        }else {
            Log.e("Error", "bytes not found");
        }

    }

    private void checkBytes() {

        Log.e("tag", "clicked");

        ArrayList<String> selected = adapter.getSelected();

        if (Global.getBytes().size() < 5) {
            Log.e("image", images.get(Global.getBytes().size()));
            if (selected.get(0).equals(images.get(Global.getBytes().size()))) {
                Global.addLink(selected.get(0));


                    Runnable runnable = () -> {
                        try {
                             Global.addBytes(Glide.with(this).as(byte[].class).load(selected.get(0)).submit().get());
                             for (byte[] bytes : Global.getBytes()) {
                                 Log.e("bytes", Arrays.toString(bytes));
                             }
                            ImageActivity.this.runOnUiThread(this::update);

                        }catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    };
                    new Thread(runnable).start();

            }
        }else if (Global.getBytes().size() == 5){
            selectedImageCount.setText(String.valueOf(Global.getBytes().size()));
            button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_check_24));
        }

    }

    private void update() {
        selectedImageCount.setText(String.valueOf(Global.getBytes().size()));
        ImageActivity.this.recreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createHash() {

        if (bytes.size() == 1) {

            if (Global.getLinks().size() < 5) {
                for (String link : Global.getLinks()) {
                    Log.e("link", link);
                }
                ImageActivity.this.recreate();
            }else if (Global.getLinks().size() == 5) {
                selectedImageCount.setText(String.valueOf(Global.getLinks().size()));
                button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_check_24));
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void generateFinalHash() {

        if (Global.getBytes().size() == 5) {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ArrayList<String> hashValues = new ArrayList<>();

            for (byte[] bytes : Global.getBytes()) {
                try {
                    byteArrayOutputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            for (String link : Global.getLinks()) {

                hashValues.add(link);

            }

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
                        }else {
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
                        Global.addLink(selected.get(0));
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