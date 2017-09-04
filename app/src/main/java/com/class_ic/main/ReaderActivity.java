package com.class_ic.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReaderActivity extends AppCompatActivity{
    private Button scan_btn;
    private Button scan_btn2;
    private SharedPreferences pref;
    private String state;
    String qr_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        pref=getSharedPreferences("Login", Activity.MODE_PRIVATE);

        scan_btn = (Button) findViewById(R.id.scan_btn);
        scan_btn2=(Button) findViewById(R.id.scan_btn2);
        final Activity activity = this;

        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state="inClass";
                Intent intent = getIntent();
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }

        });

        scan_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state="outClass";
                Intent intent = getIntent();
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this,"You cancelled the scanning", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                qr_content=result.getContents();
                Log.d("qr",qr_content);
                new Post().execute();
                // 자신을 호출한 Activity로 데이터를 보낸다.
                finish();
            }
        } else {

            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class Post extends AsyncTask<Void,Void,String> {

        //R.string.base_uri => http://192.168.1.102:8080
        final String url = "http://192.168.0.151:8080"+"/class_ic/attendance";

        @Override
        protected String doInBackground(Void... params) {

            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            Calendar cal = Calendar.getInstance();
            String hour=String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
            String minute=String.valueOf(cal.get(Calendar.MINUTE));
            parameters.add("qr",qr_content);
            parameters.add("email",pref.getString("id",null));
            parameters.add("state",state);

            if(Integer.parseInt(minute)<10){
                parameters.add("time",hour+"0"+minute);
            }else if(Integer.parseInt(minute)==0){
                parameters.add("time",hour+"00");
            }else{
                parameters.add("time",hour+minute);
            }

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(parameters, headers);

            RestTemplate restTemplate = new RestTemplate();

            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            messageConverters.add(new FormHttpMessageConverter());
            messageConverters.add(new StringHttpMessageConverter());
            restTemplate.setMessageConverters(messageConverters);

            String result = restTemplate.postForObject(url, parameters, String.class);

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getApplicationContext(), "출결처리가 완료되었습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ReaderActivity.this,ReaderActivity.class);
            startActivity(intent);
            finish();
        }

    }

}
