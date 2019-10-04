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
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        final EditText Statut = (EditText)findViewById(R.id.statut);
        Statut.setText("etudiant");


        final Intent intent = new Intent(LoginActivity.this, MainActivity.class);


        Button connect = (Button) findViewById(R.id.inscrire);
        Button inscription = (Button) findViewById(R.id.inscription);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch(checkedId){
                    case R.id.Etudiant:
                        Statut.setText("etudiant");
                        break;
                    case R.id.professeur:
                        Statut.setText("professeur");
                        break;
                }

            }
        });


        inscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, InscriptionActivity.class));
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final String mdp = pass.getText().toString();
                final String mail = email.getText().toString();

                if(!mail.equals("") && !mdp.equals("")) {

                    //****************************************************************************************************
                    class Api extends AsyncTask<String, Void, String> {

                        final String s = Statut.getText().toString();


                        String link = "http://reservationsalles.yj.fr/Site/ApiConnection?email=" + mail + "&pwd=" + md5(mdp) + "&statut=" + s;

                        @Override
                        protected String doInBackground(String... strings) {

                            String data = "";

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
                                } in.close();

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
                            if (result.equals("WRONG_EMAIL")) {
                                Toast.makeText(getApplicationContext(),
                                        "Ce compte n'existe pas: " + mail, Toast.LENGTH_SHORT).show();
                            } else if (result.equals("BAD_IDENTIFIERS")) {
                                Toast.makeText(getApplicationContext(),
                                        "Identifiant incorrect", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Connecté", Toast.LENGTH_SHORT).show();

                                intent.putExtra("info", result);

                                startActivity(intent);
                            }
                        }
                    }


                    //****************************************************************************************************


                    new Api().execute();

                }else{
                    Toast.makeText(getApplicationContext(),
                            "Veillez entrer des informations valides",Toast.LENGTH_SHORT).show();
                }


            }
        });

    }

    private String md5(String pass) {

        String password = null;
        MessageDigest mdEnc;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
            mdEnc.update(pass.getBytes(), 0, pass.length());
            pass = new BigInteger(1, mdEnc.digest()).toString(16);
            while (pass.length() < 32) {
                pass = "0" + pass;
            }
            password = pass;
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return password;

    }


}
