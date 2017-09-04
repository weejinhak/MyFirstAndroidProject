package com.class_ic.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
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
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //SharedPreference -> id만 넣어주기
    private EditText etEmail;
    private EditText etPassword;
    private Button btnRegist;
        SharedPreferences pref;
        SharedPreferences.Editor editor;
    private String Email;
    private String ps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        pref=getSharedPreferences("Login", Activity.MODE_PRIVATE);
        editor = pref.edit();
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnRegist = (Button) findViewById(R.id.l_login);
        btnRegist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Post().execute();
            }
        });
        btnRegist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Post().execute();
            }
        });
    }



    private class Post extends AsyncTask<Void,Void,String> {

        //R.string.base_uri => http://192.168.1.102:8080
        final String url = "http://192.168.0.151:8080"+"/class_ic/applogin";

        @Override
        protected void onPreExecute() {
            Email=etEmail.getText().toString();
            ps=etPassword.getText().toString();
        }

        @Override
        protected String doInBackground(Void... params) {

            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            parameters.add("id",Email);
            parameters.add("pw", ps);

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
            JSONObject json = null;
            try {
                json = new JSONObject(s);
                if(json.getString("count").equals("1")){
                    editor.putString("id",Email);
                    editor.commit();
                    Toast.makeText(getApplicationContext(), "로그인이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this,ReaderActivity.class);
                    intent.putExtra("id",Email);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(),"로그인이 안됨",Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}