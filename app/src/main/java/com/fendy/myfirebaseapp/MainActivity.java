package com.fendy.myfirebaseapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fendy.myfirebaseapp.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class MainActivity extends AppCompatActivity {

    ImageView profileImage;
    TextView tvFname, tvEmail, tvPhone, tVVerify;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    String userID;
    Button btnVerify, changePassword;
    FirebaseUser user;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileImage = (ImageView) findViewById(R.id.iVProfile);
        tvFname = (TextView) findViewById(R.id.tVname);
        tvEmail = (TextView) findViewById(R.id.tVEmail);
        tvPhone = (TextView) findViewById(R.id.tVphone);
        changePassword = (Button) findViewById(R.id.btnChangePassword);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        userID = auth.getCurrentUser().getUid();

        user = auth.getCurrentUser();

        btnVerify = (Button) findViewById(R.id.btnVerify);
        tVVerify = (TextView) findViewById(R.id.idVerify);

        if (!user.isEmailVerified()){
            tVVerify.setVisibility(View.VISIBLE);
            btnVerify.setVisibility(View.VISIBLE);
            btnVerify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
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
                }
            });
        }


        DocumentReference documentReference = firestore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null){
                    tvFname.setText("Fullname : " + documentSnapshot.getString("fullName"));
                    tvEmail.setText("Email : " + documentSnapshot.getString("email"));
                    tvPhone.setText("Phone : " + documentSnapshot.getString("phone"));
                } else {
                    tvFname.setText("Fullname : Your Full Name" );
                    tvEmail.setText("Email : Your Email" );
                    tvPhone.setText("Phone : Your Phone" );
                }
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText edResetPassword = new EditText(view.getContext());
                AlertDialog.Builder resetPasswordDialog = new AlertDialog.Builder(view.getContext());
                resetPasswordDialog.setTitle("Reset Password");
                resetPasswordDialog.setMessage("Enter new Password must > 6 characters");
                resetPasswordDialog.setView(edResetPassword);
                resetPasswordDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newPassword = edResetPassword.getText().toString().trim();
                        user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplication(), "Password is successfully reset", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplication(), "Error password not success reset " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                resetPasswordDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                resetPasswordDialog.create().show();
            }
        });
    }



    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }
}