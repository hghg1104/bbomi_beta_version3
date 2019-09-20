package andbook.example.bbomi_beta_version3;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

public class login extends AppCompatActivity {

    public static Context context;

    public static String cookie;
    private BackPressCloseHandler backPressCloseHandler;
    TextView join_btn;
    Button login_btn;
    EditText IID,IPW;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        context = this;

        backPressCloseHandler = new BackPressCloseHandler(login.this);
        join_btn = (TextView) findViewById(R.id.join_btn);
        login_btn = (Button) findViewById(R.id.login_btn);

        IID = (EditText) findViewById(R.id.IID);
        IPW = (EditText) findViewById(R.id.IPW);
        join_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent join_intent = new Intent(getApplicationContext(), join.class);
                startActivity(join_intent);
            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(IID.getText().toString().length() == 0)
                    alert_module("ID를 입력해주세요.");
                else if(IPW.getText().toString().length() == 0)
                    alert_module("패스워드를 입력해주세요.");
                else
                    login_bbomi();
            }
        });
    }

    public void login_bbomi(){
        new Thread() {
            public void run() {
                StringBuilder sb = new StringBuilder();
                String http = "https://bbomi-lkwkci.appspot.com/login";

                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL(http);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setUseCaches(false);
                    urlConnection.setConnectTimeout(10000);
                    urlConnection.setReadTimeout(10000);
                    urlConnection.setRequestProperty("Content-Type", "application/json");

                    urlConnection.connect();

                    JSONObject jsonParm = new JSONObject();

                    jsonParm.put("userID", IID.getText().toString());
                    jsonParm.put("userPWD", IPW.getText().toString());
                    OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                    out.write(jsonParm.toString());
                    out.close();


                    int HttpResult = urlConnection.getResponseCode();

                    if (HttpResult == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(
                                urlConnection.getInputStream(), "utf-8"));
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        Log.d("data", sb.toString());
                        getCookie(sb.toString());
                        br.close();
                        IPW.setText("");

                        goToMain();
                    }
                    else if (HttpResult == 400){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                alert_module("ID/패스워드를 확인해주세요.");
                            }
                        });
                    }
                    else {
                        System.out.println(urlConnection.getResponseMessage());
                    }
                }catch (UnknownHostException unknowE){              //네트워크 문제
                    unknowE.printStackTrace();
                }catch (SocketTimeoutException timeoutE){           //네트워크 타임 아웃
                    timeoutE.printStackTrace();
                }
                catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed(){
        backPressCloseHandler.onBackPressed();
    }

    public void alert_module(String msg){
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(login.this);
        alert_confirm.setMessage(msg);
        alert_confirm.setPositiveButton("확인", null);
        AlertDialog alert = alert_confirm.create();
        alert.setIcon(R.drawable.ic_launcher_foreground);
        alert.show();
    }

    public void goToMain(){
        Intent toMainIntent = new Intent(login.this, bbomi_main.class);
        toMainIntent.putExtra("ID", IID.getText().toString());
        startActivity(toMainIntent);
    }

    public void getCookie(String json){
        try {
            JSONObject jsonObject = new JSONObject(json);
            cookie = jsonObject.getString("access_token");
            System.out.println("token is "+cookie);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
