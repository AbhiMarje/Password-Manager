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

import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class MainActivity extends AppCompatActivity {

    String app_Id = "password-manager-izhmp";
    MaterialButton nextBtn;
    TextInputEditText emailText;
    TextInputEditText domainText;
    ProgressBar progressBar;
    Boolean isLogged = false;
    App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        realmInit();
        init();
        listeners();

    }

    private void listeners() {

        nextBtn.setOnClickListener((View v) -> {


            if (isLogged) {

                User user = app.currentUser();
                ArrayList<com.example.passwordmanager.Model.User> details = new ArrayList<>();

                String email = emailText.getText().toString().trim().toLowerCase();
                String domain = domainText.getText().toString().trim();
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

                if (email.isEmpty() || !email.matches(emailPattern)) {
                    emailText.setError("Please provide valid email");
                }else if (domain.isEmpty()) {
                    domainText.setError("Please provide valid domain");
                }else {
                    if (user != null) {

                        MongoClient client = user.getMongoClient("mongodb-atlas");
                        MongoDatabase database = client.getDatabase("manager");
                        Document filter = new Document().append("email", emailText.getText().toString().trim().toLowerCase());

                        database.getCollection("users").findOne(filter).getAsync(task -> {
                            if (task.isSuccess()) {
                                if (task.get() != null) {

                                    com.example.passwordmanager.Model.User user1 = new com.example.passwordmanager.Model.User(task.get().getString("name"), task.get().getString("email"),
                                            (ArrayList<String>) task.get().get("domain"));
                                    details.add(user1);

                                    if (details.get(0).getDomain().contains(domainText.getText().toString().trim())) {
                                        Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                                        intent.putExtra("isNewUser", "false");
                                        intent.putExtra("email", email);
                                        intent.putExtra("domain", domain);
                                        intent.putExtra("type", task.get().getString(domain+"Type"));
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                                        intent.putExtra("isNewUser", "false");
                                        intent.putExtra("email", email);
                                        intent.putExtra("domain", domain);
                                        intent.putExtra("type", task.get().getString(domain+"Type"));
                                        startActivity(intent);
                                    }

                                } else {
                                    Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                                    intent.putExtra("isNewUser", "true");
                                    intent.putExtra("email", email);
                                    intent.putExtra("domain", domain);
                                    startActivity(intent);
                                }
                            } else {
                                Toast.makeText(this, task.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

            }else {
                Toast.makeText(this, "Please try again later", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void realmInit() {

        Realm.init(this);
        app = new App(new AppConfiguration.Builder(app_Id).build());

        Credentials credentials = Credentials.anonymous();

        app.loginAsync(credentials, it -> {
            if (it.isSuccess()) {
                isLogged = true;
                Log.d("User", "logged successfully");
            }else {
                Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init() {

        nextBtn = findViewById(R.id.next_btn);
        emailText = findViewById(R.id.email_et);
        domainText = findViewById(R.id.domain_et);
        progressBar = findViewById(R.id.ma_progressBar);

        Global.clearBytes();
        Global.clearLinks();
        Global.makeAutoFillTrue();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Global.clearBytes();
        Global.clearLinks();
        Global.makeAutoFillTrue();
    }
}