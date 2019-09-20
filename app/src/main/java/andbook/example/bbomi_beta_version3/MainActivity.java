package andbook.example.bbomi_beta_version3;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();
        mHandler.postDelayed(mMyTask, 3000);
    }
    private Runnable mMyTask = new Runnable() {
        @Override
        public void run() {
            Intent goToLogin = new Intent(MainActivity.this, login.class);
            startActivity(goToLogin);
        }
    };
}