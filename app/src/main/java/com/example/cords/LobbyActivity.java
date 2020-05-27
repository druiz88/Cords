package com.example.cords;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class LobbyActivity extends AppCompatActivity {


    private HttpURLConnection con;
    private BufferedOutputStream os;
    private Spinner spinner;
    private FirebaseDatabase database;
    private ListView listView;
    private List<String> matchList;

    String query, MatchID, nPlayers, Time, aPlayers;
    String playerName = "";
    String line = null;
    String result = null;
    Button lob_create;

    DatabaseReference matchRef, matchesRef;

    final String url_reg = "https://druiza88.000webhostapp.com/reg_matches.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        database = FirebaseDatabase.getInstance();

        Intent i = getIntent();
        playerName = i.getStringExtra("user");
        assert playerName != null;
        Log.d("User", playerName);

        listView = findViewById(R.id.listView);
        lob_create = findViewById(R.id.lob_create);

        matchList = new ArrayList<>();

        //Create Match
        lob_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder diag = new AlertDialog.Builder(LobbyActivity.this);
                @SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
                spinner = mView.findViewById(R.id.spinner);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(LobbyActivity.this,
                        android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.numlist));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                diag.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(spinner.getSelectedItem().toString().equalsIgnoreCase("Pick players number")){
                            Toast.makeText(LobbyActivity.this, "Please pick a player number", Toast.LENGTH_SHORT).show();
                        } else {
                            MatchID = RegisterMatch();
                            matchRef = database.getReference("matches/" + MatchID);
                            matchRef.child("Count").setValue(1);
                            matchRef.child("Players").setValue(nPlayers);
                            matchRef.child("Player" + 1).setValue(playerName);
                        }
                    }
                });
                diag.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                diag.setView(mView);
                AlertDialog dialg = diag.create();
                dialg.show();
            }
        });

        //Display matches
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String itemValue = (String) listView.getItemAtPosition(position);
                final DatabaseReference countRef = database.getReference("matches/" + itemValue).child("Count");

                database.getReference("matches/" + itemValue).child("Players").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        aPlayers = dataSnapshot.getValue(String.class);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                //Full Match condition
                countRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Long aCount = dataSnapshot.getValue(Long.class);
                        assert aCount != null;
                        if(aCount.equals(Long.parseLong(aPlayers))){
                            Toast.makeText(LobbyActivity.this, "The match is full", Toast.LENGTH_SHORT).show();
                        } else {
                            countRef.setValue(aCount+1);
                            matchRef = database.getReference("matches/" + itemValue);
                            matchRef.child("Player" + (aCount+1)).setValue(playerName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        addRoomsEventListener();
    }


    private void addRoomsEventListener(){
        matchesRef = database.getReference("matches");
        matchesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                matchList.clear();
                Iterable<DataSnapshot> matches = dataSnapshot.getChildren();
                for(DataSnapshot snapshot : matches){
                    matchList.add(snapshot.getKey());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(LobbyActivity.this, android.R.layout.simple_list_item_1, matchList);
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public String RegisterMatch(){

        nPlayers = spinner.getSelectedItem().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        Time = sdf.format(new Date());

        String finalURL = url_reg + "?user_nplayers=" + nPlayers +
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
                    .appendQueryParameter("user_nplayers", nPlayers)
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

                Log.d("Result",result);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            con.disconnect();
        }

        return result;

    }





}
