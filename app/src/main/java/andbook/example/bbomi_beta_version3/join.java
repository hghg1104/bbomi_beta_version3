package andbook.example.bbomi_beta_version3;

import android.app.Activity;
import android.app.AlertDialog;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;

import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class join extends Activity {

    int overlap = 0;
    Boolean overlap_c = false;
    Boolean checkID = false;

    Button join_btn, overlap_btn;
    EditText edit_id, edit_PW, edit_cName, edit_pName, edit_cAge, edit_pwCheck;
    TextView checkPWText;

    public InputFilter filterKor = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[ㄱ-ㅣ가-힣]*$");
            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.join);

        overlap_btn = (Button) findViewById(R.id.overlap_check);
        join_btn = (Button) findViewById(R.id.join_btn);

        edit_id = (EditText) findViewById(R.id.editId);
        edit_PW = (EditText) findViewById(R.id.editPw);
        edit_cName = (EditText) findViewById(R.id.editCName);
        edit_pName = (EditText) findViewById(R.id.editPNmae);
        edit_cAge = (EditText) findViewById(R.id.editCAge);
        edit_pwCheck = (EditText) findViewById(R.id.editPwCheck);

        checkPWText = (TextView) findViewById(R.id.PWCheckText);


        // id 중복확인하는 코드 작성 (데이터베이스 접속해 id 비교 후 같은게 있으면 1, 같은게 없으면(정상) 2)
        overlap_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkID = CheckID();
                System.out.println("Check ID : " + checkID);
                if (checkID) {
                    alert_module("아이디에 특수문자가 포함되어 있습니다.");
                } else if (edit_id.getText().toString().length() < 4) {
                    alert_module("아이디의 길이가 적합하지 않습니다.");
                } else {
                    overlap_check();
                    if (overlap == 2) {
                        Toast.makeText(join.this, "중복된 아이디기 존재합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        overlap_c = true;
                    }
                }
            }
        });

        join_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // overlap 변수 확인해서 중복확인 완료 했는지 체크 (하지 않았다면 중복확인을 하게끔 토스트 출력)
                // if()
                if (edit_id.length() == 0 || edit_PW.length() == 0 || edit_cName.length() == 0 || edit_pName.length() == 0 || edit_cAge.length() == 0) {
                    alert_module("모든 정보를 입력해주세요.");
                } else if (!CheckPW1() || !CheckPW2() || edit_PW.getText().toString().length() > 20 || edit_PW.getText().toString().length() < 4 || CheckPW3()) {
                    alert_module("비밀번호 조건에 적합하지 않습니다.(4~8자리 / 문자와 숫자 조합 / 공백 제외)");
                } else if (!overlap_c) {
                    alert_module("ID 중복확인을 해주세요.");
                } else if (!edit_PW.getText().toString().equals(edit_pwCheck.getText().toString())) {
                    System.out.println(edit_PW.getText().toString() + " = " + edit_pwCheck.getText().toString());
                    alert_module("비밀번호 확인 입력이 다릅니다.");
                } else {
                    sendInfo();     //문제 없다면 정보들 웹 서버로 전송
                    Toast.makeText(join.this, "회원가입 완료! 로그인 해주세요.", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }
        });

        edit_pwCheck.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                confirmPW();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        edit_pName.setFilters(new InputFilter[]{filterKor});
        edit_cName.setFilters(new InputFilter[]{filterKor});

    }
/*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }
*/
    public void sendInfo() {
        new Thread() {
            public void run() {
                StringBuilder sb = new StringBuilder();
                String http = "https://bbomi-lkwkci.appspot.com/join";

                HttpURLConnection urlConnection = null;
                try

                {
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
                    jsonParm.put("id", edit_id.getText().toString());
                    jsonParm.put("password", edit_PW.getText().toString());
                    jsonParm.put("kid_name", edit_cName.getText().toString());
                    jsonParm.put("pr_name", edit_pName.getText().toString());
                    jsonParm.put("kid_age", edit_cAge.getText().toString());
                    OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                    System.out.println(jsonParm.toString());
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
                        br.close();

                        System.out.println("" + sb.toString());
                    } else {
                        System.out.println(urlConnection.getResponseMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
        }.start();
    }

    public void overlap_check() {

        new Thread() {
            public void run() {
                StringBuilder sb = new StringBuilder();
                String http = "https://bbomi-lkwkci.appspot.com/overlap";

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

                    jsonParm.put("id", edit_id.getText().toString());
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
                        br.close();

                        if (HttpResult == 200) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    overlap_btn.setText("ok");
                                    overlap_btn.setEnabled(false);
                                    overlap_c = true;
                                }
                            });
                        }
                    } else if (HttpResult == 400) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                alert_module("이미 존재하는 ID 입니다.");
                            }
                        });

                    } else  System.out.println(urlConnection.getResponseMessage());

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null)  urlConnection.disconnect();
                }
            }
        }.start();
    }

    public boolean CheckPW1() {
        final String regex = "([a-zA-Z])";
        Pattern pattern = Pattern.compile(regex);
        Matcher match = pattern.matcher(edit_PW.getText().toString());

        return match.find();
    }

    public boolean CheckPW2() {
        final String regex = "([0-9])";
        Pattern pattern = Pattern.compile(regex);
        Matcher match = pattern.matcher(edit_PW.getText().toString());

        return match.find();
    }

    public boolean CheckPW3(){
        final String regex = "([ ])";
        Pattern pattern = Pattern.compile(regex);
        Matcher match = pattern.matcher(edit_PW.getText().toString());
        return match.find();
    }

    public boolean CheckID() {
        final String regex = "([ !@#$%^&*()<>?\"=:{}|<>';+|])";
        Pattern pattern = Pattern.compile(regex);
        Matcher match = pattern.matcher(edit_id.getText().toString());
        return match.find();
    }

    public void alert_module(String msg) {
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(join.this);
        alert_confirm.setMessage(msg);
        alert_confirm.setPositiveButton("확인", null);
        AlertDialog alert = alert_confirm.create();
        alert.setIcon(R.drawable.ic_launcher_foreground);
        alert.show();
    }

    public void confirmPW() {
        String password = edit_PW.getText().toString();
        String confirm = edit_pwCheck.getText().toString();

        if (password.equals(confirm)) {
            checkPWText.setText("일치");
            checkPWText.setTextColor(Color.GREEN);
        } else {
            checkPWText.setText("불일치");
            checkPWText.setTextColor(Color.RED);
        }
    }
}