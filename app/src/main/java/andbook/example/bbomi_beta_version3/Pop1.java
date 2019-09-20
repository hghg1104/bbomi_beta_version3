package andbook.example.bbomi_beta_version3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class Pop1 extends Activity {
    Button doneBtn1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.taskselected);

        doneBtn1 = (Button)findViewById(R.id.donebtn1);
        doneBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Pop1.this, taskdone.class);
                startActivity(intent);
            }
        });
    }
}

