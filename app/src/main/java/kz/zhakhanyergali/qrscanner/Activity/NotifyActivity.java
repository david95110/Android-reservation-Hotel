package kz.zhakhanyergali.qrscanner.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
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

public class NotifyActivity extends AppCompatActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif);


        final ArrayList<String> items = new ArrayList<String>();
        final ListView myList =(ListView) findViewById(R.id.myList);
        final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1, items){};

        class Notif extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... strings) {

                String data = "";

                String MY_PREF="MyPrefs";

                SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREF, MODE_PRIVATE);

                final String idUser = pref.getString("idUser", null);


                final String link = "http://reservationsalles.yj.fr/Site/reload_notif?API=true&idUser="+idUser;

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
                    JSONArray jsonArray = jsonRootObject.optJSONArray("reload_notif");
                    items.clear();
                    for(int i=0; i < jsonArray.length(); i++){

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String idNotif = jsonObject.getString("idNotif");
                        String idRdv = jsonObject.getString("idRdv");
                        String salle = jsonObject.getString("salle");
                        String Createur = jsonObject.getString("createur_groupe");
                        String  membre = jsonObject.getString("membres");
                        String date = jsonObject.optString("Date");
                        String HeureDebut = jsonObject.optString("HeureDebut");

                        final String MyPREFERENCES = "notif"+i ;
                        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("notifId", idNotif);
                        editor.putString("idRdv", idRdv);
                        editor.putString("membres", membre);
                        editor.commit();

                        data = Createur+" vous invite le " +date+" à "+HeureDebut+" dans la salle "+salle;
                        items.add(data);
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
            }
        }


        new Notif().execute();

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                String selectedItem = (String) parent.getItemAtPosition(position);
                Log.d("Position ",""+position);

                final String MY_PREF = "notif" + position;
                final SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREF, MODE_PRIVATE);

                AlertDialog.Builder builder1 = new AlertDialog.Builder(NotifyActivity.this);
                builder1.setMessage(selectedItem+ " \n Accepter L'invitation ? ");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Oui",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                class accepter extends AsyncTask<String, Void, String> {

                                    @Override
                                    protected String doInBackground(String... strings) {

                                        final String idRdv = pref.getString("idRdv", null);
                                        final String idNotif = pref.getString("idNotif", null);

                                        final String link = "http://reservationsalles.yj.fr/Etudiant/notif_accepted?id=" + idNotif + "&idRdv=" + idRdv + "&API=true";

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
                                            Toast.makeText(NotifyActivity.this, "Rendez-vous accepté", Toast.LENGTH_SHORT).show();

                                            SharedPreferences.Editor editor = pref.edit();
                                            editor.clear();
                                            editor.commit();

                                        }
                                        if (result.equals("0")){
                                            Toast.makeText(NotifyActivity.this, "Echec", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                new accepter().execute();

                            }


                        });

                builder1.setNegativeButton(
                        "Non",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                class refuser extends AsyncTask<String, Void, String> {

                                    @Override
                                    protected String doInBackground(String... strings) {

                                        final String idRdv = pref.getString("idRdv", null);
                                        final String idNotif = pref.getString("idNotif", null);

                                        final String link = "http://reservationsalles.yj.fr/Etudiant/notif_refused?id=" + idNotif + "&idRdv=" + idRdv + "&API=true";

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
                                            Toast.makeText(NotifyActivity.this, "Rendez-vous refusé!", Toast.LENGTH_SHORT).show();

                                            SharedPreferences.Editor editor = pref.edit();
                                            editor.clear();
                                            editor.commit();

                                        }
                                        if (result.equals("0")){
                                            Toast.makeText(NotifyActivity.this, "Echec", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                new refuser().execute();


                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();

            }
        });


    }

}
