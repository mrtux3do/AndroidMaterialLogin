package com.delaroystudios.materiallogin;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.Bind;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    @Bind(R.id.input_email) EditText email_text;
    @Bind(R.id.input_password) EditText password_text;
    @Bind(R.id.btn_login) Button login_button;
    @Bind(R.id.link_signup) TextView signup_link;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        login_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        signup_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    public void login() {

        if (!validate()) {
            onLoginFailed("Login failed");
            return;
        }

        login_button.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        final String email = email_text.getText().toString();
        final String password = password_text.getText().toString();

        // TODO: Implement your own authentication logic here.

        new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                    new AsyncTaskNetwork(email,password).execute("http://192.168.0.74/REST/users/login.json");
                    progressDialog.dismiss();
                }
            }, 3000
        );
    }



    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        login_button.setEnabled(true);
        finish();
    }

    public void onLoginFailed(String error) {
        email_text.setText("");
        password_text.setText("");
        Toast.makeText(getBaseContext(), error, Toast.LENGTH_LONG).show();
        login_button.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = email_text.getText().toString();
        String password = password_text.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_text.setError("enter a valid email address");
            valid = false;
        } else {
            email_text.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            password_text.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            password_text.setError(null);
        }

        return valid;
    }

    protected class AsyncTaskNetwork extends AsyncTask<String, Void, String>{
        OkHttpClient client = new OkHttpClient();
        String email, password;
        public AsyncTaskNetwork(String email, String password) {
            this.email = email;
            this.password = password;
        }
        @Override
        protected String doInBackground(String... params) {
            RequestBody formBody = new FormBody.Builder()
                    .add("email", email)
                    .add("password", password)
                    .build();
            final Request request = new Request.Builder()
                    .url(params[0]).post(formBody)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String val = response.body().string();
                    Integer code = response.code();
                    if(code == 400){
                        try{
                            JSONObject obj = new JSONObject(val);
                            final String error_msg = obj.getJSONObject("error").getString("message");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onLoginFailed(error_msg);
                                }
                            });
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
            return null;
        }

    }
}
