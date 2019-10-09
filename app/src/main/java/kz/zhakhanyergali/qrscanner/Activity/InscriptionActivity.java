package kz.zhakhanyergali.qrscanner.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import kz.zhakhanyergali.qrscanner.R;

public class InscriptionActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription);

        final EditText email = (EditText)findViewById(R.id.email);
        final EditText pass = (EditText)findViewById(R.id.hd);
        final EditText Nom = (EditText)findViewById(R.id.tnom);
        final EditText Prenom = (EditText)findViewById(R.id.membre);

        Button connect = (Button) findViewById(R.id.reserver);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String nom = Nom.getText().toString();
                final String prenom = Prenom.getText().toString();
                final String mdp = pass.getText().toString();
                final String mail = email.getText().toString();

                //****************************************************************************************************
                class Api extends AsyncTask<String, Void, String> {

                    String link = "http://reservationsalles.yj.fr/Etudiant/ApiInscriptionEtudiant?prenom="+prenom+"&nom="+nom+"&mail="+mail+"&pwd="+md5(mdp);


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
                        if(result.equals("1")) {
                            Toast.makeText(getApplicationContext(),
                                    "Compte "+ mail +" créé. Connectez-vous maintenant!",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(InscriptionActivity.this, LoginActivity.class));
                        }else{
                            Toast.makeText(getApplicationContext(),
                                    "Echec de création",Toast.LENGTH_SHORT).show();
                        }
                    }
                }


                //****************************************************************************************************


                new Api().execute();

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
