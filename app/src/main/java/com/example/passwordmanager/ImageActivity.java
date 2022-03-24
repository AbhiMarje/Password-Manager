package com.example.passwordmanager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    String isNewUser = "";
    ArrayList<String> arrayList;
    RecyclerView recyclerView;
    ImageAdapter adapter;
    MaterialButton button;
    ProgressBar progressBar;
    TextView selectedImageCount;
    ArrayList<byte[]> bytes;
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
                        }else {
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

        button.setOnClickListener((View v) -> {
            if (Global.getBytes().size() < 5) {
                getBytes();
            }else {
                generateFinalHash();
                Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createHash() {

        if (bytes.size() == 1) {

            byte[] finalByteArray = bytes.get(0);

            if (Global.getBytes().size() < 5) {
                Global.addBytes(Base64.getEncoder().encodeToString(finalByteArray));
                Log.e("base64", Base64.getEncoder().encodeToString(finalByteArray));
                for (String base : Global.getBytes()) {
                    Log.e("base641", base);
                }
                ImageActivity.this.recreate();
            }

        }
    }

    private void generateFinalHash() {

        if (Global.getBytes().size() == 5) {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            for (String base : Global.getBytes()) {
                try {
                    byteArrayOutputStream.write(base.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                byte[] finalByteArray = byteArrayOutputStream.toByteArray();
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                byte[] hash = messageDigest.digest(finalByteArray);

                Log.e("hash", Arrays.toString(hash));

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getBytes() {
        Log.e("tag", "clicked");
        progressBar.setVisibility(View.VISIBLE);
        ArrayList<String> selected = adapter.getSelected();
        Log.e("tag", selected.toString());
        bytes = new ArrayList<>();

        if (selected.size() == 1) {

            Runnable runnable = () -> {
                try {
                    byte[] bytes1 = Glide.with(ImageActivity.this).as(byte[].class).load(selected.get(0)).submit().get();
                    bytes.add(bytes1);

                    if (bytes.size() == 1) {
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