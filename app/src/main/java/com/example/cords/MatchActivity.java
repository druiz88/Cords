package com.example.cords;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class MatchActivity extends AppCompatActivity {


    TextView textView;
    String strk, playerName;
    ImageView[] imageViews = new ImageView[11];
    FirebaseDatabase database;
    ArrayList<String> al;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        database = FirebaseDatabase.getInstance();

        Intent intent = getIntent();
        strk = intent.getStringExtra("match");
        playerName = intent.getStringExtra("player");

        textView = findViewById(R.id.textView);
        textView.setText("User: " + playerName);

        for(int u = 0; u < 11; u++){
            String imageID = "imageView" + (u+1);
            int resID = getResources().getIdentifier(imageID,"id",getPackageName());
            imageViews[u] = findViewById(resID);
        }

        final DatabaseReference handRef = database.getReference("Matches/" + strk).child("Players").child(playerName);

        handRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String hand = dataSnapshot.child("Hand").getValue(String.class);
                Long cards = dataSnapshot.child("Cards").getValue(Long.class);
                assert hand != null;
                assert cards != null;
                String num = hand.substring(1,hand.length()-1);
                String[] str = num.split(", ");
                al = new ArrayList<>(Arrays.asList(str).subList(0, cards.intValue()));
                drawHand(al, cards);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void Sort(View view){
        Collections.sort(al);
        updateHand(al.toString());
    }

    public void Start(View view){
        drawCard();
    }

    public void updateHand(String vHand){
        final DatabaseReference handRef = database.getReference("Matches/" + strk).child("Players").child(playerName).child("Hand");
        handRef.setValue(vHand);
    }

    public void drawHand(ArrayList<String> playerOrder, Long ncards){

        for(int k = 0; k < ncards; k++) {
            String fnm = playerOrder.get(k);
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

    public void drawCard(){
        final DatabaseReference handRef = database.getReference("Matches/" + strk).child("Players");

        handRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String hand = dataSnapshot.child("Deck").child("Hand").getValue(String.class);
                Long dcards = dataSnapshot.child("Deck").child("Cards").getValue(Long.class);
                assert hand != null;
                String num = hand.substring(1,hand.length()-1);
                String[] str = num.split(", ");
                assert dcards != null;
                ArrayList<String> decklist = new ArrayList<>(Arrays.asList(str).subList(0, dcards.intValue()));
                String lcard = decklist.get(dcards.intValue()-1);
                Log.d("last card", lcard);
                Log.d("Deck cards", String.valueOf(dcards));
                decklist.remove(dcards.intValue()-1);
                handRef.child("Deck").child("Hand").setValue(decklist.toString());
                handRef.child("Deck").child("Cards").setValue(dcards.intValue()-1);

                String hand2 = dataSnapshot.child(playerName).child("Hand").getValue(String.class);
                Long dcards2 = dataSnapshot.child(playerName).child("Cards").getValue(Long.class);
                assert hand2 != null;
                String num2 = hand2.substring(1,hand2.length()-1);
                String[] str2 = num2.split(", ");
                assert dcards2 != null;
                Log.d("player hand", hand2);
                Log.d("player cards", String.valueOf(dcards2));
                al = new ArrayList<>(Arrays.asList(str2).subList(0, dcards2.intValue()));
                al.add(lcard);
                Log.d("al", al.toString());
                handRef.child(playerName).child("Hand").setValue(al.toString());
                handRef.child(playerName).child("Cards").setValue(al.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



}
