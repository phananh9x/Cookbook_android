package com.phananh.cookbook;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.phananh.adapter.FoodAdapter;
import com.phananh.api.APIServices;
import com.phananh.api.ApiUtils;
import com.phananh.api.response.LoginResponse;
import com.phananh.config.configuration;
import com.phananh.model.LogIn;
import com.phananh.util.ParserDulieuJSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText userName, passWord;
    String user, pass;
    private ProgressDialog progressDialog;

    private APIServices mAPIService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        anhxa();
        addEvents();
    }

    private void anhxa() {
        userName = (EditText) findViewById(R.id.username);
        passWord = (EditText) findViewById(R.id.password);
        findViewById(R.id.tv_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(userName.getText().toString(), passWord.getText().toString());
            }
        });
    }

    private void login(String username, String pass) {
        mAPIService = ApiUtils.getAPIService();
        mAPIService.login(new LogIn(username, pass)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response != null){
                    Toast.makeText(LoginActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addEvents() {

    }

    private class LoginAsyctask extends AsyncTask<String, LogIn, String> {
        StringBuffer dulieu;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(LoginActivity.this);
            //  progressDialog.setTitle("Vui Lòng Chờ !");
            progressDialog.setMessage("Loading...");
            progressDialog.setIndeterminate(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(LogIn... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                URL url = new URL(params[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);

                String dong = "";

                dulieu = new StringBuffer();

                while ((dong = bufferedReader.readLine()) != null) {
                    dulieu.append(dong);
                }

                bufferedReader.close();
                reader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                Log.d("Dữ liệu", "doInBackground: " + dulieu.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dulieu.toString();
        }

    }
}
