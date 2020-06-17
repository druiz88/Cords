package com.example.cords;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText log_user, log_pass;
    private BufferedOutputStream os;
    private HttpURLConnection con;
    private FusedLocationProviderClient fusedLocationProviderClient;

    Button btn_login, btn_reg;
    String query, AdLine;
    String line = null;
    String result = null;

    final String url_Login = "https://druiza88.000webhostapp.com/login_user2.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getLocation();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }

        log_user = findViewById(R.id.log_user);
        log_pass = findViewById(R.id.log_pass);
        btn_login = findViewById(R.id.btn_login);
        btn_reg = findViewById(R.id.btn_reg);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder().permitNetwork().build()));
                String id = LoginUser();
                Log.d("playid", id);
                if(!"not inserted".equals(id) && !"fail".equals(id)){
                    int qID = Integer.parseInt(id);
                    Intent i = new Intent(MainActivity.this, LobbyActivity.class);
                    String User = log_user.getText().toString();
                    i.putExtra("user",User);
                    i.putExtra("id", qID);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    public String LoginUser() {

        String User = log_user.getText().toString();
        String Password = log_pass.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String Time = sdf.format(new Date());
        String Location = getLocation();

        String finalURL = url_Login + "?user_user=" + User +
                "&user_password=" + Password +
                "&user_time=" + Time +
                "&user_location=" + Location;

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
                    .appendQueryParameter("user_password", Password)
                    .appendQueryParameter("user_time", Time)
                    .appendQueryParameter("user_location", Location);
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
                if (!"not inserted".equals(result) && !"fail".equals(result)) {
                    Toast.makeText(MainActivity.this, "Successfully Logged In", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Email or Password mismatched!", Toast.LENGTH_LONG).show();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            con.disconnect();
        }
        return result;
    }

    private String getLocation() {

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if(location != null){
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
                        AdLine = String.valueOf(addresses.get(0).getAddressLine(0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return AdLine;
    }
}
