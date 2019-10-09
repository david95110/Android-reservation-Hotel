package kz.zhakhanyergali.qrscanner.Activity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.net.URL;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.google.zxing.Result;

import butterknife.BindView;
import butterknife.OnClick;
import kz.zhakhanyergali.qrscanner.Entity.History;
import kz.zhakhanyergali.qrscanner.R;
import kz.zhakhanyergali.qrscanner.SQLite.ORM.HistoryORM;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    // Init ui elements
    @BindView(R.id.lightButton) ImageView flashImageView;

    //Variables
    Intent i;
    HistoryORM h = new HistoryORM();
    private ZXingScannerView mScannerView;
    private boolean flashState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CAMERA},
                1);

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);


        final String MyPREFERENCES = "MyPrefs" ;
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        try {
            JSONObject reader = new JSONObject(info);
            final int id = Integer.parseInt(reader.getString("idUser"));
            final String nom = reader.getString("nom");
            final String prenom = reader.getString("prenom");
            final String  mail = reader.getString("mail");
            final  String  logged = reader.getString("logged_in");
            final  String statut = reader.getString("statut");

            String name = "Bienvenu "+prenom +" "+ nom ;

            final TextView text = (TextView) this.findViewById(R.id.titleActionBar);
            text.setText(name);

            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("idUser", String.valueOf(id));
            editor.putString("nom", nom);
            editor.putString("prenom", prenom);
            editor.putString("mail", mail);
            editor.putString("logged_in", logged);
            editor.putString("statut", statut);

            editor.commit();


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(final Result rawResult) {

        // adding result to history
        String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        History history = new History();
        history.setContext(rawResult.getText());
        history.setDate(mydate);
        h.add(getApplicationContext(), history);

        // show custom alert dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_dialog);

        View v = dialog.getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);

        final TextView text = (TextView) dialog.findViewById(R.id.someText);
        text.setText(rawResult.getText());

        final EditText salle = (EditText) dialog.findViewById(R.id.salle);
        salle.setText(rawResult.getText());

        //*******************************************************************************


        final EditText date;
        final DatePickerDialog[] datePickerDialog = new DatePickerDialog[1];

        date = (EditText) dialog.findViewById(R.id.date);
        // perform click event on edit text
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calender class's instance and get current date , month and year from calender
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog[0] = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                date.setText( year+ "-"
                                        + (monthOfYear + 1) + "-" +dayOfMonth );


                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog[0].show();
            }
        });

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = sdf.format(c.getTime());
        date.setText(strDate);

        final ArrayList<String> items = new ArrayList<String>();
        final ListView myList =(ListView) dialog.findViewById(R.id.myList);

        final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1, items);


        class Api extends AsyncTask<String, Void, String> {

            @SuppressLint("WrongThread")
            @Override
            protected String doInBackground(String... strings) {

                String data = "Disponiblilité de la salle"+ rawResult.getText() +" d'aujourd'hui: \n \n ";

               final String link = "http://reservationsalles.yj.fr/Site/getDispoSalleAPP?num_salles="+salle.getText()+"&date="+date.getText();

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

                        int idSalle = Integer.parseInt(jsonObject.getString("idSalle"));
                        String Date = jsonObject.getString("Date");
                        String  HeureDebut = jsonObject.getString("HeureDebut");
                        String statut = jsonObject.optString("statut");
                        data = salle.getText()+ " | "+Date+ " | " +HeureDebut + " | " +statut;
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

                text.setText(result);
              text.setVisibility(View.GONE);
            }
        }
        new Api().execute();

        myList.setAdapter(mArrayAdapter);

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                final Intent intent = new Intent(MainActivity.this, ReservationActivity.class);
                String selectedItem = (String) parent.getItemAtPosition(position);
                intent.putExtra("info", selectedItem);
                startActivity(intent);


            }
        });


        date.addTextChangedListener(new TextWatcher() {

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
                    new Api().execute();
                myList.setAdapter(mArrayAdapter);
            }
        });


        salle.addTextChangedListener(new TextWatcher() {

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
                    new Api().execute();
                myList.setAdapter(mArrayAdapter);
            }
        });


        //********************************************************************************


        ImageView img = (ImageView) dialog.findViewById(R.id.imgOfDialog);
        img.setImageResource(R.drawable.ic_done_gr);

        Button webSearch = (Button) dialog.findViewById(R.id.searchButton);
        Button copy = (Button) dialog.findViewById(R.id.copyButton);
        Button share = (Button) dialog.findViewById(R.id.shareButton);
        webSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url;
                if(Patterns.WEB_URL.matcher(rawResult.getText()).matches()) {
                    url = rawResult.getText();
                }else {
                    url = "http://www.google.com/#q=" + rawResult.getText();
                }
                Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                dialog.dismiss();
                mScannerView.resumeCameraPreview(MainActivity.this);
            }
        });
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", rawResult.getText());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getApplicationContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
                mScannerView.resumeCameraPreview(MainActivity.this);
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = rawResult.getText();
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share "));

                dialog.dismiss();
                mScannerView.resumeCameraPreview(MainActivity.this);
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mScannerView.resumeCameraPreview(MainActivity.this);
            }
        });
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to camera", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @OnClick
    void mainActivityOnClickEvents(View v) {

        switch (v.getId()) {
            case R.id.historyButton:
                i = new Intent(this, HistoryActivity.class);
                startActivity(i);
                break;
            case R.id.lightButton:

                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("Se déconnecter ? ");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Oui",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.clear();
                                editor.commit();
                                finish();
                                System.exit(0);
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

                break;
        }

    }

}
