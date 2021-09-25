package com.mehmetardic.anilardiyari;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AniyiGoster extends AppCompatActivity {
    ImageView imageView;
    TextView aniBasligiTextView,aniTextView;
    SQLiteDatabase database;
    Intent intent;
    Integer id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aniyi_goster);
        imageView=findViewById(R.id.imageView2);
        aniBasligiTextView=findViewById(R.id.aniBasligiGosterTextView);
        aniTextView=findViewById(R.id.aniGosterTextView);
        intent=getIntent();
        id=intent.getIntExtra("id",1);
        goster(id);
        System.out.println("id = "+id);
    }
    public void goster(int id){

        try {
            database=this.openOrCreateDatabase("AniDefteri",MODE_PRIVATE,null);
            //Soru işareti yerine yanındaki stringdeki elemanı koyuyor
            Cursor cursor = database.rawQuery("SELECT * FROM anilar WHERE id = ?",new String[]{String.valueOf(id)});
            int idIx=cursor.getColumnIndex("id");
            int aniBasligiIx=cursor.getColumnIndex("anibasligi");
            int aniIx=cursor.getColumnIndex("ani");
            int imageIx=cursor.getColumnIndex("image");

            while (cursor.moveToNext()){

                aniBasligiTextView.setText(cursor.getString(aniBasligiIx));
                aniTextView.setText(cursor.getString(aniIx));
                byte[] bytes=cursor.getBlob(imageIx);
                Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                imageView.setImageBitmap(bitmap);
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }



    }
    public void duzenle(View view){
        Intent intent2= new Intent(AniyiGoster.this,MainActivity.class);
        intent2.putExtra("status","old");
        intent2.putExtra("id",id);
        startActivity(intent2);

    }
}