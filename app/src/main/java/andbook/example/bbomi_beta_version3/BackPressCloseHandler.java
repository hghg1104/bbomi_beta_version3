package andbook.example.bbomi_beta_version3;

import android.app.Activity;
import androidx.core.app.ActivityCompat;
import android.widget.Toast;

public class BackPressCloseHandler {
    private long backKeyPressedTime = 0;
    private Toast toast;

    private Activity activity;

    public BackPressCloseHandler(Activity context){
        this.activity = context;
    }

    public void onBackPressed(){
        if(System.currentTimeMillis() > backKeyPressedTime + 2000){
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            ActivityCompat.finishAffinity(activity);
            System.runFinalization();
            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());

        }

    }

    public void showGuide(){
        toast = Toast.makeText(activity,"뒤로 버튼을 한번 더 누르시면 종료됩니다.",Toast.LENGTH_LONG);
        toast.show();
    }


}
