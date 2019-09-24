package kz.zhakhanyergali.qrscanner.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
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


                final String mdp = pass.getText().toString();
                final String mail = email.getText().toString();

                //****************************************************************************************************
                class Api extends AsyncTask<String, Void, String> {

                    String link = "http://reservationsalles.yj.fr/Site/ApiConnection?email="+mail+"&pwd="+md5(mdp)+"&statut="+Statut[0];

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
                        if(result.equals("WRONG_EMAIL")) {
                            Toast.makeText(getApplicationContext(),
                                    "Ce compte n'existe pas: "+ mail,Toast.LENGTH_SHORT).show();
                        }else if(result.equals("BAD_IDENTIFIERS")){
                            Toast.makeText(getApplicationContext(),
                                    "Email ou mot de passe incorrect" ,Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(),
                                    "Connecté:" + result,Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }
                    }
                }


                //****************************************************************************************************


                new Api().execute();

            }
        });

    }

    private String md5(String s) {

        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

            return hexString.toString();
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";

    }


}
