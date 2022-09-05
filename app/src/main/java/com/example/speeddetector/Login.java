package com.example.speeddetector;



import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Login extends AppCompatActivity {


    TextView error;
    FirebaseAuth db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.login);

        db =FirebaseAuth.getInstance();

        //login user
        MaterialButton login = (MaterialButton) findViewById(R.id.login);
        login.setOnClickListener(view -> {
            login();
        });


        //register user
        TextView register = findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toRegister();
            }

        });


    }

    //Login method
    protected void login(){
        TextView email = (TextView) findViewById(R.id.email_login);
        TextView password= (TextView) findViewById(R.id.password);;
        error = findViewById(R.id.error);


        if(email.getText().toString().isEmpty()){
            Toast.makeText(Login.this,"Please enter email", Toast.LENGTH_SHORT).show();

        }else if(password.getText().toString().isEmpty()){

            Toast.makeText(Login.this,"Please enter password", Toast.LENGTH_SHORT).show();

        }else{
            db.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        SharedPreferences.Editor editor = getApplicationContext()
                                .getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                        editor.putString("userEmail", email.getText().toString());
                        editor.apply();
                        Toast.makeText(Login.this,"User logged in successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        startActivity(intent);

                    }else{
                        Toast.makeText(Login.this,"Wrong username or password", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //Go to registration page
    public void toRegister(){
        Intent intent = new Intent(Login.this,Register.class);
        startActivity(intent);

    }

    //if the user is already loged in open the speed detector
    protected void onStart(){
       super.onStart();
        FirebaseUser user = db.getCurrentUser();
        if (user != null){
          startActivity((new Intent(Login.this, MainActivity.class)));
       }
    }

}
