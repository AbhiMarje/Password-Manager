package com.example.passwordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.bson.Document;

import java.util.ArrayList;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoDatabase;

public class RegisterActivity extends AppCompatActivity {

    String app_Id = "password-manager-izhmp";
    App app;
    MaterialButton button;
    TextInputEditText name;
    TextInputEditText email;
    TextInputEditText domain;
    ProgressBar progressBar;
    String isNewUser;
    TextInputLayout nameLayout;
    TextInputLayout emailLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();
        listeners();

    }

    private void listeners() {

        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            isNewUser = extras.getString("isNewUser");

            if (!isNewUser.equals("true")) {
                nameLayout.setVisibility(View.GONE);
                emailLayout.setVisibility(View.GONE);
            }

        }else {
            Toast.makeText(this, "Please try to login again", Toast.LENGTH_SHORT).show();
        }

        button.setOnClickListener((View v) -> {

            app = new App(new AppConfiguration.Builder(app_Id).build());
            User user = app.currentUser();
            MongoClient client = user.getMongoClient("mongodb-atlas");
            MongoDatabase database = client.getDatabase("manager");

            Log.e("tag", extras.getString("isNewUser"));

            if (isNewUser.equals("true")) {
                progressBar.setVisibility(View.VISIBLE);

                String mName = name.getText().toString().trim();
                String mEmail = email.getText().toString().trim();
                String mDomain = domain.getText().toString().trim();
                ArrayList<String> allDomain = new ArrayList<>();

                if (mName.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    name.setError("Please provide valid name");
                } else if (mEmail.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    email.setError("Please provide valid email");
                } else if (mDomain.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    domain.setError("Please provide valid domain");
                } else {

                    allDomain.add(mDomain);

                    Document document = new Document().append("name", mName).append("email", mEmail).append("domain", allDomain);

                    database.getCollection("users").insertOne(document).getAsync(task -> {
                        if (task.isSuccess()) {
                            progressBar.setVisibility(View.GONE);

                            Intent intent = new Intent(RegisterActivity.this, ImageActivity.class);
                            intent.putExtra("isNewUser", "true");
                            intent.putExtra("email", mEmail);
                            intent.putExtra("domain", mDomain);
                            startActivity(intent);
                        } else {
                            progressBar.setVisibility(View.GONE);

                            Log.e("tag", task.getError().getErrorMessage());
                        }
                    });

                }
            }else {

                progressBar.setVisibility(View.VISIBLE);
                ArrayList<com.example.passwordmanager.Model.User> arrayList = new ArrayList<>();

                String mDomain = domain.getText().toString().trim();

                if (mDomain.isEmpty()) {
                    domain.setError("Please provide valid domain");
                }else {

                    Document filter = new Document().append("email", extras.getString("email"));

                    database.getCollection("users").findOne(filter).getAsync(task -> {
                        if (task.isSuccess()) {
                            progressBar.setVisibility(View.GONE);

                            com.example.passwordmanager.Model.User user1 = new com.example.passwordmanager.Model.User(task.get().getString("name"), task.get().getString("email"),
                                    (ArrayList<String>) task.get().get("domain"));
                            arrayList.add(user1);
                            arrayList.get(0).getDomain().add(mDomain);

                            Document queryFilter = new Document().append("email", extras.getString("email"));
                            Document newDocument = new Document("$set", new Document("domain", arrayList.get(0).getDomain()));

                            database.getCollection("users").updateOne(queryFilter, newDocument).getAsync(task1 -> {
                                if (task1.isSuccess()) {
                                    progressBar.setVisibility(View.GONE);

                                    long count = task1.get().getModifiedCount();
                                    if (count == 1) {
                                        Intent intent = new Intent(RegisterActivity.this, ImageActivity.class);
                                        intent.putExtra("isNewUser", "true");
                                        intent.putExtra("email", extras.getString("email"));
                                        intent.putExtra("domain", mDomain);
                                        startActivity(intent);
                                    }else {
                                        Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    progressBar.setVisibility(View.GONE);

                                    Log.e("tag", task1.getError().getErrorMessage());
                                }
                            });

                        }else {
                            progressBar.setVisibility(View.GONE);
                            Log.e("tag", task.getError().getErrorMessage());
                        }
                    });

                }

            }

        });

    }

    private void init() {

        name = findViewById(R.id.res_name_et);
        email = findViewById(R.id.res_email_et);
        domain = findViewById(R.id.res_domain_et);
        progressBar = findViewById(R.id.res_progressBar);
        button = findViewById(R.id.res_next_btn);
        nameLayout = findViewById(R.id.textInputLayoutName);
        emailLayout = findViewById(R.id.textInputLayoutEmail);

        progressBar.setVisibility(View.GONE);

        Global.clearBytes();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Global.clearBytes();
    }
}