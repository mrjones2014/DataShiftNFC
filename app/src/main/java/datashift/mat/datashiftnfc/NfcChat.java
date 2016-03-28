package datashift.mat.datashiftnfc;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import java.util.ArrayList;

public class NfcChat extends Activity {
    private static String publish_key="pub-c-138f8754-66a6-4375-8961-2e0fa80eef95";
    private static String subscribe_key="sub-c-9e3d23d0-e680-11e5-b07b-02ee2ddab7fe";
    private static String channel="droid2pc";
    private static Pubnub pubnub;
    private static final int INCOMING=0;
    private static final int OUTGOING=1;
    private static Callback cb;
    public String user;
    public String msgString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        askForUsernamePopup();
        pubnub=new Pubnub(publish_key, subscribe_key);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_chat);
        cb=new Callback() {
            @Override
            public void connectCallback(String channel, Object message) {
                appendMessage(message.toString(), INCOMING);
            }

            @Override
            public void disconnectCallback(String channel, Object message) {
                appendMessage(message.toString(), INCOMING);
            }

            public void reconnectCallback(String channel, Object message) {
                appendMessage(message.toString(), INCOMING);
            }

            @Override
            public void successCallback(String channel, Object message) {
                appendMessage(message.toString(), INCOMING);
            }
        };
    }

    public void sendMessage(View v) {
        EditText editText = (EditText) findViewById(R.id.editText);
        String msg=editText.getText().toString();
        appendMessage(msg, OUTGOING);
        pubnub.publish(channel, msg, cb);
        editText.setText("");
    }

    private void appendMessage(String message, int direction){
        EditText textBox=(EditText)findViewById(R.id.editText);
        if(direction==INCOMING){
            msgString=msgString + "\n \n" + user.toUpperCase() + ":: " + textBox.getText().toString();
        }
        else{
            msgString=msgString + "\n \n" + "-you" + ":: " + textBox.getText().toString();
        }
        TextView tv=(TextView)findViewById(R.id.textView);
        ScrollView sv=(ScrollView)findViewById(R.id.scroller);
        sv.scrollTo(0, sv.getBottom());
        tv.setText(msgString);
    }

    public void askForUsernamePopup(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Your Name:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                user = input.getText().toString();
                if (user == null || user == "" || user == " ") {
                    askForUsernamePopup();
                }
            }
        });

        builder.show();
    }
}