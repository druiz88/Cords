package com.example.cords;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;


public class ListAdapter extends ArrayAdapter<String> implements View.OnClickListener {

    private List<String> users;
    private LayoutInflater mInflater;
    private int mViewResourceId;
    private FloatingActionButton itemfab, itemfab2, itemfab3;
    private Context context;
    private HttpURLConnection con;
    private BufferedOutputStream os;
    FirebaseDatabase database;
    String line = null;
    String result = null;
    String query, playerName;
    private String matched;
    public Deck deck;
    public Map<String, ArrayList<String>> handz;
    public List<String> playerList;

    public ListAdapter(Context context, int textViewResourceId, List<String> userList, String playa) {
        super(context, textViewResourceId);
        this.context = context;
        this.users = userList;
        this.playerName = playa;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = textViewResourceId;
    }

    @Override
    public int getCount() {
        return users.size();
    }


    @Override
    public long getItemId(int i) {
        return i;
    }


    @SuppressLint("ViewHolder")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = mInflater.inflate(mViewResourceId,null);

        final Map<String, FloatingActionButton> playerList = new HashMap<>();

        final String splat = users.get(i);
        database = FirebaseDatabase.getInstance();

        if(users != null){
            TextView name = view.findViewById(R.id.textName);
            if(name != null){
                name.setText(splat);
            }
        }

        itemfab3 = view.findViewById(R.id.fab7);
        itemfab3.setEnabled(false);

        playerList.put("fab" + users.get(i), itemfab3);

        database.getReference("Matches/" + splat).child("Players").addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    if(Objects.requireNonNull(childDataSnapshot.getKey()).equals(playerName)){
                        matched = childDataSnapshot.getRef().getParent().getParent().getKey();
                        FloatingActionButton btn = playerList.get("fab" + matched);
                        btn.setEnabled(true);
                        DrawableCompat.setTintList(DrawableCompat.wrap(btn.getDrawable()), ColorStateList.valueOf(Color.BLUE));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        itemfab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference startRef = database.getReference("Matches/" + splat);

                startRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child("Players").child(playerName).exists()){
                            Long bSize = dataSnapshot.child("Size").getValue(Long.class);
                            deal(bSize, Long.parseLong(splat));
                            Intent intent = new Intent(context, MatchActivity.class);
                            intent.putExtra("match", splat);
                            intent.putExtra("size", bSize);
                            intent.putExtra("player", playerName);
                            context.startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        itemfab = view.findViewById(R.id.fab5);
        itemfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder().permitNetwork().build()));
                Toast.makeText(context, abandonLobby(playerName), Toast.LENGTH_SHORT).show();

                final DatabaseReference countRef = database.getReference("Matches/" + splat).child("Count");

                countRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Long aCount = dataSnapshot.getValue(Long.class);
                        countRef.setValue(aCount-1);
                        database.getReference("Matches/" + splat).child("Players").child(playerName).removeValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


        itemfab2 = view.findViewById(R.id.fab6);
        itemfab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DatabaseReference fatRef = database.getReference("Matches/" + splat);
                final DatabaseReference countRef = database.getReference("Matches/" + splat).child("Count");

                //Match size
                fatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final Long aPlayers = dataSnapshot.child("Size").getValue(Long.class);

                        //Full Match condition
                        countRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                Long aCount = dataSnapshot.getValue(Long.class);
                                if (aCount != null) {
                                    final Long points = aCount + 1;
                                    if (points > aPlayers) {
                                        Toast.makeText(context, "The match is full", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String RegMsg = RegMatch(splat);
                                        Toast.makeText(context, RegMsg, Toast.LENGTH_SHORT).show();
                                        if(!"User already".equals(RegMsg.substring(0, 12))) {
                                            fatRef.child("Players").child(playerName).child("Hand").setValue("-");
                                            countRef.setValue(points);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }

                });
            }
        });

        return view;
    }

    private String abandonLobby(String splot){
        final String url_abandon = "https://druiza88.000webhostapp.com/abandon_lobby.php";

        String finalURL = url_abandon + "?user_splot=" + splot;

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
                    .appendQueryParameter("user_splot", String.valueOf(splot));
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

        final String regMatchURL = "https://druiza88.000webhostapp.com/reg_match.php";

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


    private void deal(Long match_size, Long match_id){

        playerList = new ArrayList<>();
        deck = new Deck();

        handz = deck.dealHands(match_size.intValue());

        database.getReference("Matches/" + match_id).child("Players").child("Deck").child("Hand").setValue(deck.arrayDeck().toString());
        database.getReference("Matches/" + match_id).child("Players").child("Discard Pile").child("Hand").setValue("-");

        final ArrayList<String> Order = new ArrayList<>();
        for (int c = 0; c < match_size; c++) {
            Order.add(String.valueOf(c + 1));
        }
        Collections.shuffle(Order);

        final DatabaseReference playersRef = database.getReference("Matches/" + match_id);

        playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            int z = 0;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                playerList.clear();
                Iterable<DataSnapshot> players = dataSnapshot.child("Players").getChildren();
                for (DataSnapshot snapshot : players) {
                    String decker = snapshot.getKey();
                    if (!decker.equals("Deck") && !decker.equals("Discard Pile")) {
                        playersRef.child("Players").child(snapshot.getKey()).child("Order").setValue(Long.parseLong(Order.get(z)));
                        playersRef.child("Players").child(snapshot.getKey()).child("Cards").setValue(11);
                        playersRef.child("Players").child(snapshot.getKey()).child("Hand").setValue(handz.get("n" + Order.get(z)).toString());
                        z = z + 1;
                    } else if (decker.equals("Deck")) {
                        Long size = dataSnapshot.child("Size").getValue(Long.class);
                        playersRef.child("Players").child("Deck").child("Cards").setValue(108 - 11*size);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onClick(View v) {

    }
}
