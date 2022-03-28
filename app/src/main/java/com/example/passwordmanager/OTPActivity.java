package com.example.passwordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class OTPActivity extends AppCompatActivity {

    TextInputEditText editText;
    MaterialButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpactivity);

        editText = findViewById(R.id.otp);
        button = findViewById(R.id.submit);

        button.setOnClickListener((View v) -> {

            Bundle bundle = getIntent().getExtras();
            if (editText.getText().toString().trim().equals(bundle.getString("otp"))) {
                Toast.makeText(this, "Correct", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OTPActivity.this, ImageActivity.class);
                intent.putExtra("email", bundle.getString("email", ""));
                intent.putExtra("domain", bundle.getString("domain", ""));
                intent.putExtra("isNewUser", "true");
                intent.putExtra("isReset", "true");
                startActivity(intent);
            }else {
                Toast.makeText(this, "Not Correct", Toast.LENGTH_SHORT).show();
            }

        });

    }
}