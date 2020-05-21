package com.example.cords;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

        public class RegisterActivity extends AppCompatActivity {

            EditText reg_name, reg_lname, reg_user, reg_pass, reg_email, reg_phone;
            Button reg_register, reg_cancel;
            String query;
            BufferedOutputStream os;
            HttpURLConnection con;
            String line = null;
            String result = null;

            final String url_reg = "https://druiza88.000webhostapp.com/reg_users.php";

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_register);

                reg_name = findViewById(R.id.reg_name);
                reg_lname = findViewById(R.id.reg_lname);
                reg_user = findViewById(R.id.reg_user);
                reg_pass = findViewById(R.id.reg_pass);
                reg_email = findViewById(R.id.reg_email);
                reg_phone = findViewById(R.id.reg_phone);

                reg_register = findViewById(R.id.reg_register);
                reg_cancel = findViewById(R.id.reg_cancel);

                reg_register.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder().permitNetwork().build()));
                        if(RegisterUser().equals("User registered successfully")){
                            Intent i = new Intent(RegisterActivity.this, LobbyActivity.class);
                            startActivity(i);
                            finish();
                        }
                    }
                });

                reg_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                });



            }

            public String RegisterUser(){

                String Name = reg_name.getText().toString();
                String LName = reg_lname.getText().toString();
                String Email = reg_email.getText().toString();
                String User = reg_user.getText().toString();
                String Pass = reg_pass.getText().toString();
                String Phone = reg_phone.getText().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String Time = sdf.format(new Date());


                String finalURL = url_reg + "?user_user=" + User +
                "&user_pass=" + Pass +
                "&user_name=" + Name +
                "&user_lname=" + LName +
                "&user_phone=" + Phone +
                "&user_email=" + Email +
                "&user_time=" + Time;


                //Connection
                try {
                    URL url = new URL(finalURL);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Append Parameters
                try {
                Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("user_user", User)
                    .appendQueryParameter("user_password", Pass)
                    .appendQueryParameter("user_name", Name)
                    .appendQueryParameter("user_lname", LName)
                    .appendQueryParameter("user_email", Email)
                    .appendQueryParameter("user_phone", Phone)
                    .appendQueryParameter("user_time", Time);
                    query = builder.build().getEncodedQuery();

                    os = new BufferedOutputStream(con.getOutputStream());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Response
                try {
                    int response_code = con.getResponseCode();
                    // Check if successful connection made
                    if (response_code == HttpURLConnection.HTTP_OK) {
                        // Read data sent from server
                        InputStream input = con.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                        StringBuilder sb = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        os.close();

                        // Pass data to onPostExecute method
                        result = sb.toString();
                        if(result.equalsIgnoreCase("User registered successfully")) {
                            Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_LONG).show();
                        } else if(result.equalsIgnoreCase("Fields cannot be empty")){
                            Toast.makeText(RegisterActivity.this, "Fields cannot be empty", Toast.LENGTH_LONG).show();
                        } else if(result.equalsIgnoreCase("User already exists")){
                            Toast.makeText(RegisterActivity.this, "User already exists", Toast.LENGTH_LONG).show();
                        } else if(result.equalsIgnoreCase("Email already exists")){
                            Toast.makeText(RegisterActivity.this, "Email already exists", Toast.LENGTH_LONG).show();
                        } else if(result.equalsIgnoreCase("Phone already exists")){
                            Toast.makeText(RegisterActivity.this, "Phone already exists", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Oops! Please try again", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    con.disconnect();
                }

                return result;

    }

}
