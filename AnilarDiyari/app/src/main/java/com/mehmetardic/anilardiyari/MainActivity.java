package com.mehmetardic.anilardiyari;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    Bitmap selectedImage,newImage;
    ImageView aniImageView;
    EditText aniBasligiEditText,aniEditText;
    Intent intent;
    SQLiteDatabase database;
    String sqlString,status;
    Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aniImageView=findViewById(R.id.imageView);
        aniBasligiEditText=findViewById(R.id.aniBasligiEditText);
        aniEditText=findViewById(R.id.aniEditText);
        deleteButton=findViewById(R.id.deleteButton);
        intent=getIntent();
        status=intent.getStringExtra("status");
        database = this.openOrCreateDatabase("AniDefteri",MODE_PRIVATE,null);
        if(status.matches("old")){

            verileriGetir(intent.getIntExtra("id",0));
            deleteButton.setVisibility(View.VISIBLE);
        }
        else{
            deleteButton.setVisibility(View.INVISIBLE);
        }

    }

    public void verileriGetir(int id){

        try {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0,0 , 0);
            aniImageView.setLayoutParams(lp);
            //Soru işareti yerine yanındaki stringdeki elemanı koyuyor
            Cursor cursor = database.rawQuery("SELECT * FROM anilar WHERE id = ?",new String[]{String.valueOf(id)});
            int idIx=cursor.getColumnIndex("id");
            int aniBasligiIx=cursor.getColumnIndex("anibasligi");
            int aniIx=cursor.getColumnIndex("ani");
            int imageIx=cursor.getColumnIndex("image");

            while (cursor.moveToNext()){

                aniBasligiEditText.setText(cursor.getString(aniBasligiIx));
                aniEditText.setText(cursor.getString(aniIx));
                byte[] bytes=cursor.getBlob(imageIx);
                Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                aniImageView.setImageBitmap(bitmap);
                selectedImage=bitmap;

            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void selectImage(View view){

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            //izin isteme
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

        } else {
            //izin verilmişse tekrar selectImage'e tıklayınca galeriye götürme
            Intent intentToGallery= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentToGallery,2);
        }


    }

    @Override // izin verildiği an otomatik galeriye götürme
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if(requestCode == 1){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Intent intentToGallery= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2);
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override //seçilen veriyi alma
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {

        if(requestCode==2 && resultCode==RESULT_OK && data!= null){

            Uri imagedata=data.getData();


            try {
                // SDK 28 VE ÜZERİNE IMAGEDECODER GETİRDİLER
                if(Build.VERSION.SDK_INT>=28){

                    ImageDecoder.Source source=ImageDecoder.createSource(this.getContentResolver(),imagedata);
                    selectedImage=ImageDecoder.decodeBitmap(source);
                    newImage=makeSmallerImage(selectedImage,1920);
                    aniImageView.setImageBitmap(newImage);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 0,0 , 0);
                    aniImageView.setLayoutParams(lp);

                }// SDK 28 VE ALTINDA BU İŞİ GETBİTMAP YAPIYOR
                else{
                    selectedImage=MediaStore.Images.Media.getBitmap(this.getContentResolver(),imagedata);
                    aniImageView.setImageBitmap(selectedImage);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 0,0 , 0);
                    aniImageView.setLayoutParams(lp);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void Save(View view){

        String ani= aniEditText.getText().toString();
        String aniBasligi=aniBasligiEditText.getText().toString();
        //Bitmap yeni= makeSmallerImage(selectedImage,300);
        //Kaydetmek için bitmap'i veriye çevirmemiz lazım
        newImage= makeSmallerImage(selectedImage,1920);
        ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
        newImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        if(status.matches("new")){

            try {


                database.execSQL("CREATE TABLE IF NOT EXISTS anilar (id INTEGER PRIMARY KEY,anibasligi VARCHAR, ani VARCHAR, image BLOB )");
                sqlString ="INSERT INTO anilar (anibasligi,ani,image) VALUES (?,?,?)";
                SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
                sqLiteStatement.bindString(1,aniBasligi);
                sqLiteStatement.bindString(2,ani);
                sqLiteStatement.bindBlob(3,byteArray);
                sqLiteStatement.execute();
                System.out.println("new icinde");


            }catch (Exception e){

                e.printStackTrace();

            }

        }
        else if(status.matches("old")){
                try {

                    sqlString = "UPDATE anilar SET anibasligi=?, ani=?, image=? WHERE id=?";
                    SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
                    sqLiteStatement.bindString(1,aniBasligi);
                    sqLiteStatement.bindString(2,ani);
                    sqLiteStatement.bindBlob(3,byteArray);
                    sqLiteStatement.bindString(4,String.valueOf(intent.getIntExtra("id",0)));
                    sqLiteStatement.execute();

                }catch (Exception e){
                    e.printStackTrace();
                }
        }

        //Aktiviteyi Komple Bitiriyor
        mainActivityToAnaEkran();

    }
    public void mainActivityToAnaEkran(){
        intent= new Intent(MainActivity.this,AnaEkran.class);
        // öteki activity e geçerken diğer tüm activiteleri kapatıyor
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }


    public void delete(View view){

        AlertDialog.Builder uyariMesaji= new AlertDialog.Builder(this);
        uyariMesaji.setTitle("Anı Silme");
        uyariMesaji.setMessage("Anıyı silmek istediğinize emin misiniz ?");
        uyariMesaji.setNegativeButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sqlString="DELETE FROM anilar WHERE id=?";
                SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
                sqLiteStatement.bindString(1,String.valueOf(intent.getIntExtra("id",0)));
                sqLiteStatement.execute();
                mainActivityToAnaEkran();
            }
        });
        uyariMesaji.setPositiveButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        uyariMesaji.show();

    }

    // Resim Küçültme İşlemi (Sqlite 1mb ve üzeri resimlerde çöküyor, O yüzden ya küçültcez yada FireBase'de saklıcaz)
     public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image,width,height,true);
    }
}