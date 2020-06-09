package com.example.cords;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.database.Cursor;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MatchActivity extends AppCompatActivity {

    Deck deck;
    TextView textView;
    Cursor data, data2;
    Map<String, ArrayList<String>> handz;
    String strk, State;
    ImageView[] imageViews = new ImageView[11];
    FirebaseDatabase database;
    Long match_size;
    List<String> playerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        deck = new Deck();
        database = FirebaseDatabase.getInstance();
        playerList = new ArrayList<>();

        Intent intent = getIntent();
        strk = intent.getStringExtra("match");
        match_size = intent.getLongExtra("size",0);

        for(int u = 0; u < 11; u++){
            String imageID = "imageView" + (u+1);
            int resID = getResources().getIdentifier(imageID,"id",getPackageName());
            imageViews[u] = findViewById(resID);
        }

        handz = deck.dealHands(match_size.intValue());

        database.getReference("Matches/" + strk).child("Players").child("Deck").child("Hand").setValue(deck.arrayDeck().toString());

        final ArrayList<String> Order = new ArrayList<>();
        for(int c = 0; c < match_size; c++) {
            Order.add(String.valueOf(c+1));
        }
        Collections.shuffle(Order);

        final DatabaseReference playersRef = database.getReference("Matches/" + strk).child("Players");

        playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            int z = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                playerList.clear();
                Iterable<DataSnapshot> players = dataSnapshot.getChildren();
                for(DataSnapshot snapshot : players){
                    String decker = snapshot.getKey();
                    if(!decker.equals("Deck")){
                        playersRef.child(snapshot.getKey()).child("Order").setValue(Order.get(z));
                        playersRef.child(snapshot.getKey()).child("Hand").setValue(handz.get("n" + (z + 1)).toString());
                        z = z + 1;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        dealHand();
    }

    public void Sort(View view){
        Collections.sort(Objects.requireNonNull(handz.get("n1")));
        dealHand();
        updateData(Objects.requireNonNull(handz.get("n1")).toString());
    }

    public void updateData(String vHand){

    }

    public void dealHand(){

        for(int k = 0; k < 11; k++) {
            String fnm = Objects.requireNonNull(handz.get("n1")).get(k);
            final ImageView img = imageViews[k];
            String PACKAGE_NAME = getApplicationContext().getPackageName();
            int imgId = getResources().getIdentifier(PACKAGE_NAME+":drawable/"+fnm , null, null);
            img.setImageBitmap(BitmapFactory.decodeResource(getResources(),imgId));
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) img.getLayoutParams();
                    if(params.gravity== Gravity.TOP){
                        params.gravity = Gravity.BOTTOM;
                    } else {
                        params.gravity = Gravity.TOP;
                    }
                    img.setLayoutParams(params);
                }
            });
        }

    }

}
