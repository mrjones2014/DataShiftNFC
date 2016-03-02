package datashift.mat.datashiftnfc;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private File file;
    private TextView fileNameView;
    private static final int PICKFILE_RESULT_CODE = 1;
    private static final int READ_WRITE_PERMISSON_REQUEST=1;
    private static final int NFC_PERMISSION_REQUEST=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileNameView=(TextView)findViewById(R.id.fileNameView);
    }

    public void openFilePicker(View v){
        int readPermission=ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission=ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(readPermission== PackageManager.PERMISSION_GRANTED && writePermission==PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, PICKFILE_RESULT_CODE);
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_WRITE_PERMISSON_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    String FilePath = data.getData().getPath();
                    fileNameView.setText(FilePath);
                    file = new File(FilePath);
                    Button nextButton=(Button)findViewById(R.id.sendButton);
                    nextButton.setVisibility(View.VISIBLE);
                    nextButton.setEnabled(true);
                }
                break;
        }
    }

    public void readyToSend(View v){
        int nfcPermission=ContextCompat.checkSelfPermission(this, Manifest.permission.NFC);
        if(nfcPermission==PackageManager.PERMISSION_GRANTED){
            sendFile(file);
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.NFC}, NFC_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onBackPressed(){
        setContentView(R.layout.activity_main);
        if(file!=null){
            TextView filenameView=(TextView)findViewById(R.id.fileNameView);
            filenameView.setText(file.getName());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_WRITE_PERMISSON_REQUEST: {
                //If request is cancelled, the result arrays are empty
                final Button browse=(Button)findViewById(R.id.browseButton);
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    browse.callOnClick();

                }
                else {
                   Toast.makeText(this, "Sorry, this app requires read/write permission to work.", Toast.LENGTH_SHORT);
                }
                return;
            }
            case NFC_PERMISSION_REQUEST: {
                final Button sendButton=(Button)findViewById(R.id.sendButton);
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    sendButton.callOnClick();
                }
                else{
                    Toast.makeText(this, "Sorry, this app requires NFC permission to work.", Toast.LENGTH_SHORT);
                }
                return;
            }
        }
    }

    public void sendFile(File fileToSend){
        NfcAdapter nfcAdapter=NfcAdapter.getDefaultAdapter(this);
        if(!nfcAdapter.isEnabled()){
            Toast.makeText(this, "Please enable NFC.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }
        else if(!nfcAdapter.isNdefPushEnabled()){
            Toast.makeText(this, "Please enable Android Beam", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_NFCSHARING_SETTINGS));
        }
        else{
            fileToSend=new File(fileToSend.getAbsolutePath());
            fileToSend.setReadable(true, false);
            FileUriCallback fileUriCallback=new FileUriCallback();
            nfcAdapter.setBeamPushUrisCallback(fileUriCallback, this);
            //nfcAdapter.setBeamPushUris(new Uri[]{Uri.fromFile(fileToSend)}, this);
        }
    }

    private class FileUriCallback implements NfcAdapter.CreateBeamUrisCallback{
        public FileUriCallback(){}
        @Override
        public Uri[] createBeamUris(NfcEvent e){
            return new Uri[]{Uri.fromFile(file)};
        }
    }
}
