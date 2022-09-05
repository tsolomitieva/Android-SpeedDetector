package com.example.speeddetector;




import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class Register extends AppCompatActivity {

    FirebaseAuth mAuth;
    TextView error;
    Button singUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        singUp = findViewById(R.id.singUp);
        error = findViewById(R.id.error);

        //register
        singUp.setOnClickListener((view->{
            registerUser();
        }));
    }
    //registration
    private void registerUser(){
        TextView email = (TextView) findViewById(R.id.email);
        TextView password = (TextView) findViewById(R.id.password1);
        TextView password2 = (TextView) findViewById(R.id.password2);

        if(email.getText().toString().isEmpty()){
            Toast.makeText(Register.this,"Please enter email", Toast.LENGTH_SHORT).show();

        }else if(password.getText().toString().isEmpty()){

            Toast.makeText(Register.this,"Please enter password", Toast.LENGTH_SHORT).show();

        }else if(!(password.getText().toString().equals(password2.getText().toString()))){
            Toast.makeText(Register.this,"Password and confirm password don't match", Toast.LENGTH_SHORT).show();

        }else{
            mAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        SharedPreferences.Editor editor = getApplicationContext()
                                .getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                        editor.putString("userEmail", email.getText().toString());
                        editor.apply();

                        Toast.makeText(Register.this,"User registered successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Register.this, com.example.speeddetector.Login.class));
                    }else{
                        Toast.makeText(Register.this,"Something went wrong" , Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }

}
