package kz.zhakhanyergali.qrscanner.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
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
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;

import kz.zhakhanyergali.qrscanner.R;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        RadioGroup rg = (RadioGroup) findViewById(R.id.radiogroup);
        rg.check(R.id.Etudiant);
        final EditText email = (EditText)findViewById(R.id.email);
        final EditText pass = (EditText)findViewById(R.id.pass);

        Button connect = (Button) findViewById(R.id.connect_btn);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.Etudiant:
                        Toast.makeText(getApplicationContext(), "Etudiant", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.professeur:
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


                    @Override
                    protected String doInBackground(String... strings) {

                        @SuppressLint("WrongThread") String link = "https://reservationsalles.000webhostapp.com/Site/conexion?email="+email.getText()+"?password"+pass.getText();

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
                        Toast.makeText(getApplicationContext(),
                                result,Toast.LENGTH_SHORT).show();
                        if(result.equals("good")) {
                            Toast.makeText(getApplicationContext(),
                                    "Connecté",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(),
                                    "Echec",Toast.LENGTH_SHORT).show();
                        }
                    }
                }


                //****************************************************************************************************



                new Api().execute();

            }
        });

    }


}
