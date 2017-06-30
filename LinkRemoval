import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.HashSet;

/**
* This class removes nodes with no friends and dangling friendships (non bidirectional friendships)
*/
public class LinkRemoval {
    public static void main(String[] args) throws IOException, ParseException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("filtered_clean.json")));
        BufferedWriter writer = new BufferedWriter(new FileWriter("filtered.json"));
        String line = "";
        JSONParser parser = new JSONParser();
        HashSet<String> utentiVeri = new HashSet<>();
        int i = 0;
        while((line = reader.readLine()) != null){
            JSONObject utente = (JSONObject) parser.parse(line);
            if(isGoodUtente(utente)){
                writer.write(utente.toJSONString() + "\n");
                writer.flush();
                i++;
            }
            utentiVeri.add(utente.get("user_id").toString());
        }
        System.out.println(i);
        writer.close();

    }

    private static boolean isGoodUtente(JSONObject utente) {/*
        Long useful = (Long) utente.get("useful");
        Long funny = (Long) utente.get("funny");
        Long cool = (Long) utente.get("cool");
        Long fans = (Long) utente.get("fans");
        Long reviews = (Long) utente.get("review_count");*/
        JSONArray friends_t = (JSONArray) utente.get("friends");
        Integer friends = friends_t.size();
        if(friends == 1 && friends_t.get(0).toString().equalsIgnoreCase("none"))
            friends = 0;
        return friends >= 1;
    }

}
