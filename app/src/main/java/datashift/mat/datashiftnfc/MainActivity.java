package datashift.mat.datashiftnfc;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends AppCompatActivity implements NfcAdapter.CreateBeamUrisCallback {
    private TextView textOut;
    private File file;
    private static final int PICKFILE_RESULT_CODE=1;
    private static final int READ_WRITE_PERMISSON_REQUEST=2;

    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textOut = (TextView)findViewById(R.id.textout);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter==null){
            Toast.makeText(MainActivity.this, "nfcAdapter==null, no NFC adapter exists", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(MainActivity.this, "Set Callback(s)", Toast.LENGTH_LONG).show();
            nfcAdapter.setBeamPushUrisCallback(this, this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        intent.setType(getMimeType(Uri.fromFile(file)));
        setIntent(intent);
    }

    public String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = getApplicationContext().getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public void openFilePicker(View v){
        int readPermission=ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(readPermission== PackageManager.PERMISSION_GRANTED && writePermission== PackageManager.PERMISSION_GRANTED) {
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
                    textOut.setText(FilePath);
                    file = new File(FilePath);
                }
                break;
        }
    }

    @Override
    public Uri[] createBeamUris(NfcEvent event) {
        file.setReadable(true, false);
        return new Uri[]{Uri.fromFile(file)};
    }
}
