package kz.zhakhanyergali.qrscanner.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import kz.zhakhanyergali.qrscanner.R;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        RadioGroup rg = (RadioGroup) findViewById(R.id.radiogroup);
        rg.check(R.id.Etudiant);
        final EditText email = (EditText)findViewById(R.id.email);
        final EditText pass = (EditText)findViewById(R.id.hd);
        final String[] Statut = new String[1];
        Button connect = (Button) findViewById(R.id.connect_btn);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.Etudiant:
                        Statut[0] = "etudiant";
                        Toast.makeText(getApplicationContext(), "Etudiant", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.professeur:
                        Statut[0] = "professeur";
                        Toast.makeText(getApplicationContext(), "Professeur", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //****************************************************************************************************
                class Api extends AsyncTask<String, Void, String> {


                    String link = "https://reservationsalles.000webhostapp.com/Site/connect?mail="
                            +email.getText()+"?pass"+pass.getText()+"statut"+ Statut[0];

                    @Override
                    protected String doInBackground(String... strings) {

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

                        return sb.toString();

                        } catch(Exception e){
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }
                    @Override
                    protected void onPostExecute(String result){
                        if(result.equals("good")) {
                            Toast.makeText(getApplicationContext(),
                                    "Connecté",Toast.LENGTH_SHORT).show();
                        }else{

                            Toast.makeText(getApplicationContext(), result ,Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));

                        }
                    }
                }


                //****************************************************************************************************



                new Api().execute();

            }
        });

    }


}
