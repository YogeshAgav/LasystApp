//package com.example.lasyst;
package SecuGen.Demo;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class StartSessionActivity extends AppCompatActivity {
    Spinner stud;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_session);
        stud = (Spinner) findViewById(R.id.stud);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, AttendActivity.stud);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stud.setAdapter(arrayAdapter);
       // AttendActivity attendObj=new AttendActivity();
         //
        //       attendObj.getJSONStud("http://192.168.0.113/getStudents.php",AttendActivity.item);
        for(int i=0;i<AttendActivity.stud.length;i++) {
        Toast.makeText(getApplicationContext(), AttendActivity.stud[i], Toast.LENGTH_SHORT).show();
       // Toast.makeText(this, "fjgfjhgfjhf", Toast.LENGTH_SHORT).show();
      }
}}