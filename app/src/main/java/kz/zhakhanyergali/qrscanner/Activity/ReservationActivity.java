package kz.zhakhanyergali.qrscanner.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import kz.zhakhanyergali.qrscanner.R;

public class ReservationActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);



        Intent intent = getIntent();
        String selectedItem = intent.getStringExtra("info");


        String[] separated = selectedItem.split(" | ");

        String salle = separated[0].trim();
        final String date = separated[2].trim();
        final String heure = separated[4].trim();

        final Button reserver = (Button) findViewById(R.id.reserver);
        final EditText sal = (EditText) findViewById(R.id.salle);
        EditText dat = (EditText) findViewById(R.id.date);
        final EditText heuredeb = (EditText) findViewById(R.id.heurdeb);
        final EditText heurfin = (EditText) findViewById(R.id.heurfin);
        EditText membres = (EditText) findViewById(R.id.membre);

        sal.setText(salle);
        dat.setText(date);
        heuredeb.setText(heure);

        final String[] search = {""};
        final ArrayList<String> items = new ArrayList<String>();
        final ListView myList =(ListView) findViewById(R.id.myList);

        final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1, items);


        class Api extends AsyncTask<String, Void, String> {

            @SuppressLint("WrongThread")
            @Override
            protected String doInBackground(String... strings) {

                final String link = "http://reservationsalles.yj.fr/Site/selectUser?search="+ search[0] +"&type=API";

                String data = "";
                
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


                    JSONTokener tokener = new JSONTokener(sb.toString());
                    JSONArray finalResult = new JSONArray(tokener);
                    items.clear();
                    for(int i=0; i < finalResult.length(); i++){
                        JSONObject jsonObject = finalResult.getJSONObject(i);

                        String  email = jsonObject.getString("email");
                        String statut = jsonObject.optString("statut");
                        data = email + "    |    "+ statut;
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
            protected void onPostExecute(String result){ }
        }

        myList.setAdapter(mArrayAdapter);


        membres.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                items.clear();
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() != 0)
                    search[0] = s.toString();
                    new Api().execute();
                myList.setAdapter(mArrayAdapter);
            }
        });


        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selectedItem = (String) parent.getItemAtPosition(position);

                Toast.makeText(ReservationActivity.this,
                        selectedItem, Toast.LENGTH_SHORT).show();


            }
        });




        heurfin.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                String now = (String) android.text.format.DateFormat.format(
                        "hh:mm", new java.util.Date());
                try {

                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
                Date date1 = sdf.parse(s.toString());
                Date date2 = sdf.parse(now);
                Date date3 = sdf.parse(heuredeb.getText().toString());

                    if(s.length() != 0){

                        reserver.setBackgroundResource(R.color.df_danger);
                        reserver.setEnabled(true);


                        if(date1.before(date2)){
                            Toast.makeText(ReservationActivity.this,
                                    "Vous ne pouvez pas reserver dans le passé!", Toast.LENGTH_SHORT).show();
                            reserver.setEnabled(false);
                            reserver.setBackgroundResource(R.color.gray_bg);
                        }
                        if(date1.before(date3)){
                            Toast.makeText(ReservationActivity.this,
                                    "L'heure de debut doit etre supérieure à celle de fin!", Toast.LENGTH_SHORT).show();
                            reserver.setEnabled(false);
                            reserver.setBackgroundResource(R.color.gray_bg);
                        }
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });



    }

}
