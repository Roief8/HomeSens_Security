package com.example.homesens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "EmailPassword";
    private EditText usernameTv,passTv;
    private Button loginBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //the firebase authenticate data
        mAuth = FirebaseAuth.getInstance();

        //set the UI views.
        usernameTv=findViewById(R.id.username);
        passTv=findViewById(R.id.password);
        loginBtn=findViewById(R.id.loginBtn);

        setUp();





    }



    //this method authenticate the user input data with the firebase users data.
    private void signIn(String email, String password) {
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        synchronized (this) {
                                            wait(2000);
                                            Intent intent = new Intent (LoginActivity.this,MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                    catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();



                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }


    // this method read the user input for user name and password. then send the data for sign in method.
    protected void setUp(){



        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username=usernameTv.getText().toString();
                String password=passTv.getText().toString();

                if (username.isEmpty() || password.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Please Enter Login info.",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    signIn(username,password);
                }


            }
        });
    }


}


