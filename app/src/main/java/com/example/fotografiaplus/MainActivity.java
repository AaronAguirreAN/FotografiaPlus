package com.example.fotografiaplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.GnssAntennaInfo;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton btn_camera;
    ImageView img;
    Bitmap bitmap;
    Button btn_girar,btn_galeria;

    private int grados=0;

    private static final int REQUEST_PERMISSION_CAMERA = 22;
    private static final int FER_FOTO = 24;

    private static final int REQUEST_PERMISSION_WRITE_STORAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI
        initUI();

        btn_camera.setOnClickListener(this);
        btn_galeria.setOnClickListener(this);

        btn_girar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if (grados>=360){
                        img.setRotation(360);
                    } else {
                        grados++;
                        img.setRotation(grados);
                    }
            }
        });

    }

    private void initUI(){
        btn_camera = findViewById(R.id.btn_camera);
        btn_galeria = findViewById(R.id.btn_galeria);
        btn_girar = findViewById(R.id.btn_girar);
        img = findViewById(R.id.img);
    }

    /**Override**/
    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.btn_camera) {
            checkPermissionCamera();
        } else if(id==R.id.btn_galeria){
            checkPermissionStorage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == FER_FOTO){
            if(resultCode == Activity.RESULT_OK && data!=null){
                bitmap = (Bitmap) data.getExtras().get("data");
                img.setImageBitmap(bitmap);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_PERMISSION_CAMERA){
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                ferFoto();
            }
        } else if (requestCode == REQUEST_PERMISSION_WRITE_STORAGE){
            if(permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                saveImage();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void checkPermissionCamera(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
                ferFoto();
            } else {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
            }
        } else {

        }
    }

    private void checkPermissionStorage(){
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    saveImage();
                }else {
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION_WRITE_STORAGE);
                }
            }else {
                saveImage();
            }
        } else {
            saveImage();
        }
    }

    private void ferFoto(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,FER_FOTO);
        }
    }

    private void saveImage(){
        OutputStream fos = null;
        File file = null;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            ContentResolver resolver = getContentResolver();
            ContentValues values = new ContentValues();

            String filename = System.currentTimeMillis() + "image_exemple";

            values.put(MediaStore.Images.Media.DISPLAY_NAME,filename);
            values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/MyApp");
            values.put(MediaStore.Images.Media.IS_PENDING,1);

            Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri imageUri = resolver.insert(collection,values);

            try {
                fos = resolver.openOutputStream(imageUri);
            } catch (FileNotFoundException e){
                e.printStackTrace();
            }

            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING,0);
            resolver.update(imageUri,values,null,null);
        } else {
            String imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

            String fileName = System.currentTimeMillis() + ".jpg";

            file = new File(imageDir,fileName);

            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        boolean saved = bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
        if(saved){
            Toast.makeText(this,"Imatge guardada perfectament.",Toast.LENGTH_SHORT).show();
        }

        if(fos!=null){
            try {
                fos.flush();
                fos.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        if(file!=null){ //API < 29
            MediaScannerConnection.scanFile(this,new String[]{file.toString()},null,null);
        }

    }

}