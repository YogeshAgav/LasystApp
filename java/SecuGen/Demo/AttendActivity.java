package SecuGen.Demo;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.*;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import androidx.appcompat.app.AppCompatActivity;

public class AttendActivity extends AppCompatActivity {

	Spinner listView;
	Spinner subSp;
	String[] classID;
	String[] subID;
	String url;
	String cid1="678";
	String sid1="234";
	public static String[] stud;
	public String[] attend;
	ProgressDialog pDialog,p1Dialog;
	private Button buttonStart;
	public static String item;
	public static String subitem;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.attend_lec);

		buttonStart = (Button) findViewById(R.id.StartAttendance);
		buttonStart.setOnClickListener(
				new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			  // getJSONStud("http://192.168.0.113/getStudents.php",item);
				//Toast.makeText(getApplicationContext(), stud[1], Toast.LENGTH_SHORT).show();
				openActivity2();
				//Toast.makeText(getApplicationContext(),stud[1], Toast.LENGTH_SHORT).show();

			}
		});

		listView = (Spinner) findViewById(R.id.listView);
		subSp = (Spinner) findViewById(R.id.subjectSp);

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
					//Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_SHORT).show();
					getJSONSub("http://192.168.0.113/SubFetch.php", item);

					subSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?> parent1, View view, int position, long id) {
							if (parent1.getItemAtPosition(position).equals("Select Subject") )
							{
								//do nothing
							}
							else
							{
								String item1=parent1.getItemAtPosition(position).toString() ;
								Toast.makeText(parent1.getContext(), "Selected: " + item1, Toast.LENGTH_SHORT).show();
								item1 = ""+subID[position];
								subitem=item1;
								//Toast.makeText(parent1.getContext(), "Selected: " + item, Toast.LENGTH_SHORT).show();
								getJSONStud("http://192.168.0.113/getStudents.php",item);
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
				pDialog = new ProgressDialog(AttendActivity.this);
				pDialog.setMessage("Fetching Classes");
				pDialog.show();
			}


			@Override
			protected void onPostExecute(String s) {
				super.onPostExecute(s);
				if (pDialog.isShowing())
					pDialog.dismiss();
				//Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
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
	private void getJSONSub(final String urlWebService,final String cid) {

		class GetJSON extends AsyncTask<String, Void, String> {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			   p1Dialog = new ProgressDialog(AttendActivity.this);
				p1Dialog.setMessage("Fetching Subjects");
				p1Dialog.show();
			}


			@Override
			protected void onPostExecute(String s) {
				if (p1Dialog.isShowing())
					p1Dialog.dismiss();
				super.onPostExecute(s);
				//Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
				try
				{
					loadIntoSubListView(s);
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
					//params.add(new BasicNameValuePair("SID",sid1));
					//params.add(new BasicNameValuePair("CSID",cid1));
					params.add(new BasicNameValuePair("C_ID",cid));

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
	public void getJSONStud(final String urlWebService,final String cid) {

		class GetJSON extends AsyncTask<String, Void, String> {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				p1Dialog = new ProgressDialog(AttendActivity.this);
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
					getJSONStudW("http://192.168.0.113/insert.php",item,subitem);
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
					params.add(new BasicNameValuePair("C_ID",cid));
					/*params.add(new BasicNameValuePair("sub",sub));
					for(int i=0;i<stud.length;i++) {
						params.add(new BasicNameValuePair("status", attend[i]));
						params.add(new BasicNameValuePair("SID", stud[i]));
					}*/
					//params.addAll(stud)
					//JSONArray attend = new JSONArray();
					//Toast.makeText(getApplicationContext(), getQuery(params), Toast.LENGTH_SHORT).show();
					//Toast.makeText(getApplicationContext(), getQuery(params).toString(), Toast.LENGTH_SHORT).show();
				   // Toast.makeText(getApplicationContext(), params.toString(), Toast.LENGTH_SHORT).show();
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


	private void loadIntoListView(String json) throws JSONException {
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
	private void loadIntoSubListView(String json) throws JSONException {

		Toast.makeText(getApplicationContext(), json, Toast.LENGTH_SHORT).show();
		JSONArray jsonArray = new JSONArray(json);
		String[] sub = new String[jsonArray.length()+1];
		subID = new String[jsonArray.length()+1];
		sub[0]="Select Subject";
		Toast.makeText(getApplicationContext(), sub[0], Toast.LENGTH_SHORT).show();
		//Toast.makeText(getApplicationContext(), jsonArray.length(), Toast.LENGTH_SHORT).show();
		for (int j = 0; j <jsonArray.length(); j++) {

			JSONObject obj = jsonArray.getJSONObject(j);
			sub[j+1] = obj.getString("SubjectName");
			subID[j+1] = obj.getString("SubjectId");

		}

		Toast.makeText(getApplicationContext(), sub[0], Toast.LENGTH_SHORT).show();
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, sub);
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		subSp.setAdapter(arrayAdapter);
		// Toast.makeText(getApplicationContext(), sub[3], Toast.LENGTH_SHORT).show();

	}
	private void loadIntoStudListView(String json) throws JSONException {

		//Toast.makeText(getApplicationContext(), json, Toast.LENGTH_SHORT).show();
		JSONArray jsonArray = new JSONArray(json);
		stud = new String[jsonArray.length()+1];
		attend = new String[jsonArray.length()+1];
		int limit=jsonArray.length()/2;
		//sub[0]="Select Subject";
		//Toast.makeText(getApplicationContext(), stud[0], Toast.LENGTH_SHORT).show();
		//Toast.makeText(getApplicationContext(), jsonArray.length(), Toast.LENGTH_SHORT).show();
		for (int j = 0; j <jsonArray.length(); j++) {

			JSONObject obj = jsonArray.getJSONObject(j);
			stud[j] =obj.getString("SID");
			if(j>limit)
			{
				attend[j]="1";
			}
			else
			{
			attend[j]="0";}
		}

		//Toast.makeText(getApplicationContext(), stud[0], Toast.LENGTH_SHORT).show();
		// Toast.makeText(getApplicationContext(), sub[3], Toast.LENGTH_SHORT).show();

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
	public void openActivity2() {
		Intent intent = new Intent(this, StartSessionActivity.class);
		startActivity(intent);
	   //getJSONStud("http://192.168.0.113/getStudents.php",item);
	   // Toast.makeText(getApplicationContext(), stud[1], Toast.LENGTH_SHORT).show();
	}
	public void getJSONStudW(final String urlWebService,final String cid,final String sub) {

		class GetJSON extends AsyncTask<String, Void, String> {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				p1Dialog = new ProgressDialog(AttendActivity.this);
				p1Dialog.setMessage("wirting Students");
				p1Dialog.show();
			}


			@Override
			protected void onPostExecute(String s) {
				if (p1Dialog.isShowing())
					p1Dialog.dismiss();
				super.onPostExecute(s);
				Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
				/*try
				{
					//loadIntoStudListView(s);
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}*/

			}

			@Override
			protected String doInBackground(String... param ) {
				try {
					URL url = new URL(urlWebService);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					StringBuilder sb = new StringBuilder();

					String json="";

					conn.setRequestMethod("POST");
					conn.setDoInput(true);
					conn.setDoOutput(true);

					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("C_ID",cid));
					params.add(new BasicNameValuePair("sub",sub));
					for(int i=0;i<stud.length;i++) {
						params.add(new BasicNameValuePair("status", attend[i]));
						params.add(new BasicNameValuePair("SID", stud[i]));
					}
					//params.addAll(stud)
					//JSONArray attend = new JSONArray();
					//Toast.makeText(getApplicationContext(), params.toString(), Toast.LENGTH_SHORT).show();
					OutputStream os = conn.getOutputStream();
					BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os, StandardCharsets.UTF_8));
					writer.write(getQuery(params));
					writer.flush();
					writer.close();
					os.close();
					conn.connect();
					/*
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					while ((json = bufferedReader.readLine()) != null) {
						sb.append(json + "\n");
					}*/
					return sb.toString().trim();

				} catch (Exception e) {
					return null;
				}
			}
		}
		GetJSON getJSON = new GetJSON();
		getJSON.execute();
	}

}