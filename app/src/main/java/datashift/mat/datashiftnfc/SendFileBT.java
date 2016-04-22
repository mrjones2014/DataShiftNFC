package datashift.mat.datashiftnfc;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.URLConnection;
import java.util.List;

public class SendFileBT extends Activity {
    private static final int PICKFILE_RESULT_CODE=1;
    private static final int READ_WRITE_PERMISSON_REQUEST=2;
    private static final int REQUEST_BLU = 3;
    private static final int BLUETOOTH_PERMISSION = 4;
    private static final int DISCOVER_DURATION = 120;
    TextView textOut;
    Button sendButton;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file_bt);
        textOut=(TextView)findViewById(R.id.textout);
        sendButton=(Button)findViewById(R.id.sendButton);
        sendButton.setVisibility(View.INVISIBLE);
        file=null;
        BluetoothAdapter btAdapter=BluetoothAdapter.getDefaultAdapter();
        if(btAdapter==null) {
            Toast.makeText(SendFileBT.this, "No Bluetooth adapter on device.", Toast.LENGTH_LONG).show();
        }
    }

    public void sendFile(View v){
        if(file==null){
            Toast.makeText(this, "No file chosen.", Toast.LENGTH_SHORT).show();
        } else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)==PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, BLUETOOTH_PERMISSION);
            }
            Intent intent=new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType(URLConnection.guessContentTypeFromName(file.getName()));
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            PackageManager pm=getPackageManager();
            List<ResolveInfo> appsList=pm.queryIntentActivities(intent, 0);
            if(appsList.size()>0){
                String packageName = null;
                String className = null;
                boolean found = false;
                for(ResolveInfo info : appsList){
                    packageName = info.activityInfo.packageName;
                    if( packageName.equals("com.android.bluetooth")){
                        className = info.activityInfo.name;
                        found = true;
                        break;
                    }
                }
                if(! found){
                    Toast.makeText(this, "Bluetooth adapter not found.", Toast.LENGTH_LONG).show();
                    System.exit(0);
                }
                intent.setClassName(packageName, className);
            }
            startActivity(intent);
        }
    }

//    public void enableBluetooth(){
//        Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION );
//        startActivityForResult(discoveryIntent, REQUEST_BLU);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    sendButton.setVisibility(View.VISIBLE);
                    Uri _uri = data.getData();
                    String FilePath=getPath(this.getApplicationContext(), _uri);
                    textOut.setText(FilePath);
                    file = new File(FilePath);
                }
                break;
//            case REQUEST_BLU:
//                if(resultCode == DISCOVER_DURATION){
//
//                }
//                break;
            case BLUETOOTH_PERMISSION:
                if(resultCode!=RESULT_OK){
                    Toast.makeText(this, "You must enable Bluetooth for Bluetooth file transfer", Toast.LENGTH_SHORT);
                }
                break;
        }
    }

    public void openFilePicker(View v){
        int readPermission= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
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


    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
