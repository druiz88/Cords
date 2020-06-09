package com.example.cords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class Deck {

    private ArrayList<String> cards_list;

    public Deck(){

        String[] suits = {"ah","bd","cc","ds"};

        cards_list = new ArrayList<>();

        int ndecks = 2;

        for(int x = 0; x < ndecks; x++){
            for(int suit = 0; suit < 4; suit++){
                for(int facenum = 0; facenum < 13; facenum++){
                    if(facenum > 8) {
                        cards_list.add(suits[suit] + (facenum + 1));
                    } else {
                        cards_list.add(suits[suit] + "0" + (facenum + 1));
                    }
                }
            }
            cards_list.add("jk01");
            cards_list.add("jk02");
        }
    }

    public ArrayList<String> arrayDeck(){
        return cards_list;
    }

    public ArrayList<String> shuffleDeck(){
        Collections.shuffle(cards_list);
        return cards_list;
    }

    public Map<String, ArrayList<String>> dealHands(int nPlayers){

        shuffleDeck();

        String[][] dcards = new String[nPlayers][11];

        for(int ncards = 0; ncards < 11; ncards++) {
            for (int cardDealt = 0; cardDealt < nPlayers; cardDealt++) {
                dcards[cardDealt][ncards] = cards_list.get(0);
                cards_list.remove(0);
            }
        }

        Map<String, ArrayList<String>> hands = new HashMap<>();

        for (int y = 0; y < nPlayers; y++) {
            ArrayList<String> stringList = new ArrayList<>(Arrays.asList(dcards[y]));
            hands.put("n" + (y + 1), stringList);
        }

        return hands;
    }

}
