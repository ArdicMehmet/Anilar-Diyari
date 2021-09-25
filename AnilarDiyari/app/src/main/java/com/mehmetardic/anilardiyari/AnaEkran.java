package com.mehmetardic.anilardiyari;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class AnaEkran extends AppCompatActivity {
    ListView listView;
    ArrayList<String> names;
    ArrayList<Integer> idArray;
    ArrayAdapter arrayAdapter;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ana_ekran);
        listView=findViewById(R.id.listView);
        names=new ArrayList<>();
        idArray= new ArrayList<>();
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,names);
        listView.setAdapter(arrayAdapter);
        getir();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                intent = new Intent(AnaEkran.this,AniyiGoster.class);
                intent.putExtra("id",idArray.get(position));
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu1,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.ani_ekle_item){

            Intent intent = new Intent(this,MainActivity.class);
            intent.putExtra("status","new");
            startActivity(intent);
        }
        else if(item.getItemId()==R.id.ayarlar_item){
            Toast.makeText(this,"Şuan Bu Servis Kullanılamıyor",Toast.LENGTH_LONG).show();

            /*    Suan Kullanım Dışı
            Intent intent = new Intent(this,AyarlarPage.class);
            startActivity(intent);*/
        }
        return super.onOptionsItemSelected(item);
    }
    public void getir(){

        try {

            SQLiteDatabase database = this.openOrCreateDatabase("AniDefteri",MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM anilar",null);
            int baslikIx= cursor.getColumnIndex("anibasligi");
            int idIx=cursor.getColumnIndex("id");

            while (cursor.moveToNext()){
                
                names.add(cursor.getString(baslikIx));
                idArray.add(cursor.getInt(idIx));



            }
            // Yeni veri ekledim bunu göster
            cursor.close();
            arrayAdapter.notifyDataSetChanged();




        }catch (Exception e){
            e.printStackTrace();

        }


    }
}