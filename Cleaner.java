import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Cleaner class that adds an impact coefficient to each user and cleans up the file a bit.
 */
public class Cleaner {

    public static void main(String[] args) throws IOException, ParseException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("yelp_academic_dataset_user.json")));
        BufferedWriter writer = new BufferedWriter(new FileWriter("filtered_clean.json"));
        String line = "";
        JSONParser parser = new JSONParser();
        HashSet<String> utentiVeri = new HashSet<>();
        while((line = reader.readLine()) != null){
            JSONObject utente = (JSONObject) parser.parse(line);
            utentiVeri.add(utente.get("user_id").toString());
        }
        System.out.println(utentiVeri.size());
        reader.close();
        //Filtering degli utenti
        BufferedReader reader2 = new BufferedReader(new FileReader(new File("yelp_academic_dataset_user.json")));
        while((line = reader2.readLine()) != null){
            JSONObject nuovo = new JSONObject();
            JSONObject utente = (JSONObject) parser.parse(line);
            JSONArray friends = (JSONArray) utente.get("friends");
            friends.removeIf(amico -> !utentiVeri.contains(amico));
            nuovo.put("user_id",utente.get("user_id").toString());
            nuovo.put("name",utente.get("user_id").toString());
            nuovo.put("impatto",calcImpatto(utente));
            nuovo.put("friends",friends);
            writer.write(nuovo + "\n");
            writer.flush();
        }
        reader.close();
        writer.close();
    }

    private static float calcImpatto(JSONObject utente) {
        float[] max_val = {186543, 4691, 170896, 195160,11284, 4699,13,266318};
        float[] percents = {0.15f,0.1f,0.15f,0.1f,0.1f, 0.15f, 0.15f,0.1f};
        long useful = (long) utente.get("useful");
        long fans = (long) utente.get("fans");
        long funny = (long) utente.get("funny");
        long cool = (long) utente.get("cool");
        long review_count = (long) utente.get("review_count");
        int amici = ((JSONArray) utente.get("friends")).size();
        int elite = ((JSONArray) utente.get("elite")).size();
        long sommaComp = ((long)utente.get("compliment_hot"))+((long)utente.get("compliment_more"))+((long)utente.get("compliment_profile"))+((long)utente.get("compliment_cute"))+((long)utente.get("compliment_list"))+((long)utente.get("compliment_note"))+((long)utente.get("compliment_plain"))+((long)utente.get("compliment_cool"))+((long)utente.get("compliment_funny"))+((long)utente.get("compliment_writer"))+((long)utente.get("compliment_photos"));
        return ((useful*percents[0]/max_val[0])+(fans*percents[1]/max_val[1])+(funny*percents[2]/max_val[2])+(cool*percents[3]/max_val[3])+(review_count*percents[4]/max_val[4])+(amici*percents[5]/max_val[5])+(elite*percents[6]/max_val[6])+(sommaComp*percents[7]/max_val[7]));
    }

}
