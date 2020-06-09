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
import android.widget.TextView;
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
import java.util.Objects;


public class LobbyActivity extends AppCompatActivity {


    private HttpURLConnection con;
    private BufferedOutputStream os;
    private Spinner spinner;
    private FirebaseDatabase database;
    private ListView listView;
    private List<String> matchList;
    private TextView tvName;

    String query, MatchID, nPlayers, Time, playerID;
    String playerName = "";
    String line = null;
    String result = null;
    Button lob_create;
    ListAdapter adapter;

    DatabaseReference matchRef, matchesRef;

    final String url_reg = "https://druiza88.000webhostapp.com/reg_lobbies.php";
    final String regMatchURL = "https://druiza88.000webhostapp.com/reg_match.php";
    final String checkMatchURL = "https://druiza88.000webhostapp.com/read_log.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        database = FirebaseDatabase.getInstance();

        Intent i = getIntent();
        playerName = i.getStringExtra("user");
        playerID = String.valueOf(i.getIntExtra("id", 0));
        assert playerName != null;

        listView = findViewById(R.id.listView);
        lob_create = findViewById(R.id.lob_create);
        tvName = findViewById(R.id.tvName);

        tvName.setText(playerName);

        matchList = new ArrayList<>();

        //Create Match
        lob_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String chkMatch = checkLogMatch();

                if(chkMatch.equals("Go")){
                    //Player number builder
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
                                MatchID = RegisterLobby();
                                matchRef = database.getReference("Matches/" + MatchID);
                                matchRef.child("Count").setValue(1);
                                matchRef.child("Size").setValue(Long.parseLong(nPlayers));
                                matchRef.child("Players").child(playerName).child("Hand").setValue("-");
                                String RegMsg = RegMatch(MatchID);
                                Toast.makeText(LobbyActivity.this, RegMsg, Toast.LENGTH_SHORT).show();
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
                } else {
                    Toast.makeText(LobbyActivity.this, chkMatch, Toast.LENGTH_SHORT).show();
                }
            }
        });

        addRoomsEventListener();
    }


    private void addRoomsEventListener(){
        matchesRef = database.getReference("Matches");
        matchesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                matchList.clear();
                Iterable<DataSnapshot> matches = dataSnapshot.getChildren();
                for(DataSnapshot snapshot : matches){
                    matchList.add(snapshot.getKey());
                    adapter = new ListAdapter(LobbyActivity.this, R.layout.activity_list_adapter, matchList, playerName);
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public String RegisterLobby(){

        nPlayers = spinner.getSelectedItem().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        Time = sdf.format(new Date());

        String finalURL = url_reg + "?user_nplayers=" + nPlayers +
                "&user_time=" + Time +
                "&user_host=" + playerName;

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
                    .appendQueryParameter("user_time", Time)
                    .appendQueryParameter("user_host", playerName);
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            con.disconnect();
        }

        return result;
    }

    public String RegMatch(String itemvalue){

        String finalLogURL = regMatchURL + "?user_user=" + playerName +
                "&user_match=" + itemvalue;

        //Connection
        try {
            URL url = new URL(finalLogURL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Append Parameters
        try {
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("user_user", playerName)
                    .appendQueryParameter("user_match", itemvalue);
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

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            con.disconnect();
        }

        return result;
    }

    public String checkLogMatch(){

        String finalLogURL = checkMatchURL + "?user_user=" + playerName;

        //Connection
        try {
            URL url = new URL(finalLogURL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Append Parameters
        try {
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("user_user", playerName);
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

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            con.disconnect();
        }

        return result;
    }

}
