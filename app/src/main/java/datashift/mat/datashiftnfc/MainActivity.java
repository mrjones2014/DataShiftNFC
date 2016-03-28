package datashift.mat.datashiftnfc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goToSendFileActivity(View v){
        startActivity(new Intent(MainActivity.this, SendFileActivity.class));
    }

    public void goToChatActivity(View v){
        startActivity(new Intent(MainActivity.this, NfcChat.class));
    }
}
