package kz.zhakhanyergali.qrscanner.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import kz.zhakhanyergali.qrscanner.R;

public class RdvActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rdv);

        final ArrayList<String> items = new ArrayList<String>();
        final ListView myList =(ListView) findViewById(R.id.myList);
        final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1, items){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the current item from ListView
                View view = super.getView(position,convertView,parent);

                // Get the Layout Parameters for ListView Current Item View
                ViewGroup.LayoutParams params = view.getLayoutParams();

                // Set the height of the Item View
                params.height = 400;
                view.setLayoutParams(params);

                return view;
            }
        };

        final ArrayList<String> items1 = new ArrayList<String>();
        final ListView myList2 =(ListView) findViewById(R.id.myList2);
        final ArrayAdapter<String> mArrayAdapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1, items1){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the current item from ListView
                View view = super.getView(position,convertView,parent);

                // Get the Layout Parameters for ListView Current Item View
                ViewGroup.LayoutParams params = view.getLayoutParams();

                // Set the height of the Item View
                params.height = 400;
                view.setLayoutParams(params);

                return view;
            }
        };


        class rdv extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... strings) {

                String data = "";

                String MY_PREF="MyPrefs";

                SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREF, MODE_PRIVATE);

                final String idUser = pref.getString("idUser", null);


                final String link = "http://reservationsalles.yj.fr/Site/load_rdv?API=true&idUser="+idUser;

                try{
                    URL url = new URL(link);
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet();
                    request.setURI(new URI(link));
                    HttpResponse response = client.execute(request);
                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(response.getEntity().getContent()));

                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }  in.close();

                    JSONObject jsonRootObject = new JSONObject(sb.toString());
                    JSONArray Avenir = jsonRootObject.optJSONArray("a_venir");
                    items.clear();
                    for(int i=0; i < Avenir.length(); i++){
                        data = "";
                        JSONObject jsonObject = Avenir.getJSONObject(i);

                        String idSalle = jsonObject.getString("idSalle");
                        String salle = jsonObject.getString("nom_salle");
                        String Createur = jsonObject.getString("demandeur");
                        String  membres = jsonObject.getString("membres");
                        String date = jsonObject.optString("date");
                        String HeureDebut = jsonObject.optString("HeureDebut");
                        String HeureFin = jsonObject.optString("HeureFin");

                        final String MyPREFERENCES = "rdv"+i ;
                        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("date", date);
                        editor.putString("HeureDebut", HeureDebut);
                        editor.putString("idSalle", idSalle);
                        editor.commit();

                        data = "Membres: "+Createur+ membres + "\n Date: " +date+"\n Heure: "+HeureDebut+" à "+HeureFin+"\n Salle: "+salle;
                        items.add(data);
                    }

                    items1.clear();
                    JSONArray passer = jsonRootObject.optJSONArray("passee");
                    for(int i=0; i < passer.length(); i++){
                        String data1 = "";
                        JSONObject jsonObject = passer.getJSONObject(i);
                        String salle = jsonObject.getString("nom_salle");
                        String Createur = jsonObject.getString("demandeur");
                        String  membres = jsonObject.getString("membres");
                        String date = jsonObject.optString("date");
                        String HeureDebut = jsonObject.optString("HeureDebut");
                        String HeureFin = jsonObject.optString("HeureFin");

                        data1 = "Membres: "+Createur+ membres + "\n Date: " +date+"\n Heure: "+HeureDebut+" à "+HeureFin+"\n Salle: "+salle;
                        items1.add(data1);
                    }

                } catch(Exception e){
                    return null;
                }

                return data;

            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(String result){

                myList.setAdapter(mArrayAdapter);
                myList2.setAdapter(mArrayAdapter1);

            }
        }

        new rdv().execute();

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                String selectedItem = (String) parent.getItemAtPosition(position);

                final String MY_PREF = "rdv" + position;
                final SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREF, MODE_PRIVATE);

                AlertDialog.Builder builder1 = new AlertDialog.Builder(RdvActivity.this);
                builder1.setMessage(selectedItem+ " \n \n Annulez le Rendez-vous ? ");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Oui",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                class annuler extends AsyncTask<String, Void, String> {

                                    @Override
                                    protected String doInBackground(String... strings) {

                                        String MY_PREF2="MyPrefs";
                                        SharedPreferences pref1 = getApplicationContext().getSharedPreferences(MY_PREF2, MODE_PRIVATE);

                                        final String idUser = pref1.getString("idUser", null);
                                        final String date = pref.getString("date", null);

                                        final String HeureDebut = pref.getString("HeureDebut", null);
                                        final String salle = pref.getString("idSalle", null);

                                        String[] separ = HeureDebut.split(":");
                                        String HeureDebut1 = separ[0].trim()+":"+separ[1].trim();

                                        final String link = "http://reservationsalles.yj.fr/Etudiant/annuler_rdv?date="+date+"&heure_debut="
                                                +HeureDebut1+"&idSalle="+salle+"&idInterlocuteur="+idUser+"&API=true";

                                        System.out.println(link);

                                        try {
                                            URL url = new URL(link);
                                            HttpClient client = new DefaultHttpClient();
                                            HttpGet request = new HttpGet();
                                            request.setURI(new URI(link));
                                            HttpResponse response = client.execute(request);
                                            BufferedReader in = new BufferedReader(new
                                                    InputStreamReader(response.getEntity().getContent()));

                                            StringBuffer sb = new StringBuffer("");
                                            String line = "";

                                            while ((line = in.readLine()) != null) {
                                                sb.append(line);
                                                break;
                                            }
                                            in.close();
                                            items.clear();
                                            return sb.toString();

                                        } catch (Exception e) {
                                            return null;
                                        }

                                    }

                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                                    }

                                    @Override
                                    protected void onPostExecute(String result) {

                                        if (result.equals("1")){
                                            Toast.makeText(RdvActivity.this, "Rendez-vous annulé", Toast.LENGTH_SHORT).show();

                                            SharedPreferences.Editor editor = pref.edit();
                                            editor.clear();
                                            editor.commit();

                                        }
                                        if (result.equals("0")){
                                            Toast.makeText(RdvActivity.this, "Echec", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                new annuler().execute();

                            }


                        });

                builder1.setNegativeButton(
                        "Non",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();

            }
        });

    }

}

