//package com.example.lasyst;
package SecuGen.Demo;
import androidx..app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EnrollFingerActivity extends AppCompatActivity {

    Spinner listView;
    Spinner studSp;
    String[] classID;
    String[] studName;
    public String[] stud;
    public int[] attend;
    String url;
    String cid1="678";
    String sid1="234";

    ProgressDialog pDialog,p1Dialog;
    private Button buttonStart;
    private String item;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enroll_finger);

        buttonStart = (Button) findViewById(R.id.StartAttendance);
        buttonStart.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        //openActivity2();

                    }
                });


        listView = (Spinner) findViewById(R.id.listView);
        studSp = (Spinner) findViewById(R.id.studSp);

        getJSON("http://192.168.0.113/getclassname.php");
        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).equals("Select Class") )
                {
                    //do nothing
                }
                else
                {
                    item=parent.getItemAtPosition(position).toString() ;
                    Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_SHORT).show();
                    item = ""+classID[position];
                    Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_SHORT).show();
                    getJSONStud("http://192.168.0.113/getStudents.php", item);

                    studSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent1, View view, int position, long id) {
                            if (parent1.getItemAtPosition(position).equals("Select Student") )
                            {
                                //do nothing
                            }
                            else
                            {
                                String item=parent1.getItemAtPosition(position).toString() ;
                                Toast.makeText(parent1.getContext(), "Selected: " + item, Toast.LENGTH_SHORT).show();
                                item = ""+studName[position];
                                Toast.makeText(parent1.getContext(), "Selected: " + item, Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //getJSONSub("http://192.168.0.104/SubFetch.php", item[0]);
    }
    private void getJSON(final String urlWebService) {

        class GetJSON extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(EnrollFingerActivity.this);
                pDialog.setMessage("Fetching Classes");
                pDialog.show();
            }


            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (pDialog.isShowing())
                    pDialog.dismiss();

                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                try
                {
                    loadIntoListView(s);
                    //loadIntoSubListView(s);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    //Toast.makeText(getApplicationContext(), "ssdgsdfgsdd", Toast.LENGTH_SHORT).show();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;



                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }
        }
        GetJSON getJSON = new GetJSON();
        getJSON.execute();
    }

    public void getJSONStud(final String urlWebService,final String csid) {

        class GetJSON extends AsyncTask<String, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                p1Dialog = new ProgressDialog(EnrollFingerActivity.this);
                p1Dialog.setMessage("Fetching Students");
                p1Dialog.show();
            }


            @Override
            protected void onPostExecute(String s) {
                if (p1Dialog.isShowing())
                    p1Dialog.dismiss();
                super.onPostExecute(s);
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                try
                {
                    loadIntoStudListView(s);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

            }

            @Override
            protected String doInBackground(String... param ) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    String json;

                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("C_ID",csid));

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os, StandardCharsets.UTF_8));
                    writer.write(getQuery(params));
                    writer.flush();
                    writer.close();
                    os.close();

                    conn.connect();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();

                } catch (Exception e) {
                    return null;
                }
            }
        }
        GetJSON getJSON = new GetJSON();
        getJSON.execute();
    }
    private void loadIntoListView(String json) throws JSONException
    {
        JSONArray jsonArray = new JSONArray(json);
        int length = jsonArray.length();
        String[] heroes = new String[length+1];
        classID = new String[length+1];
        heroes[0]="Select Class";
        for (int i = 0; i <length; i++) {

            JSONObject obj = jsonArray.getJSONObject(i);
            heroes[i+1] = obj.getString("ClassName");
            classID[i+1] = obj.getString("ClassID");

        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, heroes);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listView.setAdapter(arrayAdapter);


    }

    private void loadIntoStudListView(String json) throws JSONException {



        //Toast.makeText(getApplicationContext(), json, Toast.LENGTH_SHORT).show();

        //Toast.makeText(getApplicationContext(), json, Toast.LENGTH_SHORT).show();
        JSONArray jsonArray = new JSONArray(json);
        String[] stud = new String[jsonArray.length()+1];
        studName = new String[jsonArray.length()+1];
        studName[0]="Select Student";
        Toast.makeText(getApplicationContext(), stud[0], Toast.LENGTH_SHORT).show();
        //Toast.makeText(getApplicationContext(), jsonArray.length(), Toast.LENGTH_SHORT).show();
        for (int j = 0; j <jsonArray.length(); j++) {

            JSONObject obj = jsonArray.getJSONObject(j);
            stud[j+1] =obj.getString("SID");
            studName[j+1] =obj.getString("Name");
        }

        //Toast.makeText(getApplicationContext(), stud[0], Toast.LENGTH_SHORT).show();
        // Toast.makeText(getApplicationContext(), sub[3], Toast.LENGTH_SHORT).show();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, studName);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        studSp.setAdapter(arrayAdapter);

    }
    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }
        return result.toString();
    }
}