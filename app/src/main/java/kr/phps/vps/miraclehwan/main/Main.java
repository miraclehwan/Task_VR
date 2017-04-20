package kr.phps.vps.miraclehwan.main;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

public class Main extends AppCompatActivity {

    private VrPanoramaView vrPanoramaView;
    private VrPanoramaView.Options panoOptions;

    InputStream inputStream = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button assets = (Button) findViewById(R.id.assets);
        Button uri = (Button) findViewById(R.id.uri);
        Button pick = (Button) findViewById(R.id.pick);
        vrPanoramaView = (VrPanoramaView) findViewById(R.id.pano_view);
        panoOptions = new VrPanoramaView.Options();
        panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO;


        assets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assets_Image_Load();
            }
        });

        uri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                URI_Image_Load();
            }
        });

        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(Main.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (permissionCheck == PackageManager.PERMISSION_GRANTED){
                    Intent Go_Album = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(Go_Album, 1);
                }else{
                    ActivityCompat.requestPermissions(Main.this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            switch (requestCode){
                case 1:
                    Uri fileUri = Uri.fromFile(new File(getPath(data.getData())));
                    try {
                        inputStream = getContentResolver().openInputStream(fileUri);
                        vrPanoramaView.loadImageFromBitmap(BitmapFactory.decodeStream(inputStream), panoOptions);
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void URI_Image_Load(){
        Uri fileUri = Uri.parse("android.resource://" + getPackageName() +"/drawable/vr_image2" );
        try {
            inputStream = getContentResolver().openInputStream(fileUri);
            vrPanoramaView.loadImageFromBitmap(BitmapFactory.decodeStream(inputStream), panoOptions);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void assets_Image_Load(){
        AssetManager assetManager = getAssets();
        try {
            inputStream = assetManager.open("vr_image1.png");
            vrPanoramaView.loadImageFromBitmap(BitmapFactory.decodeStream(inputStream), panoOptions);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        vrPanoramaView.resumeRendering();
    }

    @Override
    protected void onPause() {
        super.onPause();
        vrPanoramaView.pauseRendering();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vrPanoramaView.shutdown();
    }

    private String getPath(Uri uri){
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent move_album = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(move_album, 1);
                }else{
                    Toast.makeText(Main.this, "어플의 기능이 제한될 수 있습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
