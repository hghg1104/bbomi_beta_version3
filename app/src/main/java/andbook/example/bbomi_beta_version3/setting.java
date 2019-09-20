package andbook.example.bbomi_beta_version3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class setting extends AppCompatActivity {

    TextView networkSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        networkSetting = (TextView)findViewById(R.id.network_setting);

        networkSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent networkSettingIntent = new Intent(setting.this, networkSetting.class);
                startActivity(networkSettingIntent);
            }
        });
    }
}