package com.fendy.myfirebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText mFullName, mEmail, mPassword, mPhone;
    Button mRegister;
    TextView mLoginButton;
    FirebaseAuth auth;
    ProgressBar mProgressBar;
    FirebaseFirestore firestore;
    String userID;
    String md5Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFullName = (EditText) findViewById(R.id.fullName);
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mPhone = (EditText) findViewById(R.id.phone);

        mRegister = (Button) findViewById(R.id.btnRegister);
        mLoginButton = (TextView) findViewById(R.id.tValready);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        mProgressBar = (ProgressBar) findViewById(R.id.progressBarDialog);

        if (auth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString().trim();
                final String password = mPassword.getText().toString().trim();
                md5Password = MD5.getMd5(password);
                final String fullName = mFullName.getText().toString();
                final String phone = mPhone.getText().toString();


                if (TextUtils.isEmpty(email)){
                    mEmail.setError("email is required");
                    return;
                }

                if (TextUtils.isEmpty(password)){
                    mPassword.setError("password is required");
                    return;
                }

                if (password.length() < 6){
                    mPassword.setError("password must more than 6 characters");
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);

                //register user
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            FirebaseUser newUser = auth.getCurrentUser();
                            newUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplication(), "Email Verification is been sent", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("loginFirebase", "onFailure : email verification is not sent " + e.getMessage());
                                }
                            });

                            Toast.makeText(getApplication(), "user created.", Toast.LENGTH_SHORT).show();
                            userID = auth.getCurrentUser().getUid();
                            DocumentReference documentReference = firestore.collection("users").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("fullName", fullName);
                            user.put("email", email);
                            user.put("password", md5Password);
                            user.put("phone", phone);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplication(), "onSuccess: User profile is created for " + userID,  Toast.LENGTH_SHORT).show();
                                }
                            });
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            Toast.makeText(getApplication(), "Error..!!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });
    }

    public static class MD5 {
        public static String getMd5(String input) {
            try {

                // Static getInstance method is called with hashing MD5
                MessageDigest md = MessageDigest.getInstance("MD5");

                // digest() method is called to calculate message digest
                //  of an input digest() return array of byte
                byte[] messageDigest = md.digest(input.getBytes());

                // Convert byte array into signum representation
                BigInteger no = new BigInteger(1, messageDigest);

                // Convert message digest into hex value
                String hashtext = no.toString(16);
                while (hashtext.length() < 32) {
                    hashtext = "0" + hashtext;
                }
                return hashtext;
            }

            // For specifying wrong message digest algorithms
            catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().hide();
    }
}