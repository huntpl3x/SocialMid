package com.roichomsky.socialmid;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    ListView lv;
    ArrayList<Toy> toyList;
    ToyAdapter toyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Bitmap bitmapToy = BitmapFactory.decodeResource(getResources(), R.drawable.icon);

        Toy t1 = new Toy(50,"home",bitmapToy);
        Toy t2 = new Toy(70,"home",bitmapToy);
        Toy t3 = new Toy(90,"home",bitmapToy);
        Toy t4 = new Toy(29,"light",bitmapToy);
        Toy t5 = new Toy(37,"phone",bitmapToy);
        Toy t6 = new Toy(50,"light",bitmapToy);
        Toy t7 = new Toy(29,"light",bitmapToy);
        Toy t8 = new Toy(37,"phone",bitmapToy);
        Toy t9 = new Toy(50,"light",bitmapToy);

        toyList = new ArrayList<Toy>();
        toyList.add(t1);toyList.add(t2);toyList.add(t3);
        toyList.add(t4);toyList.add(t5);toyList.add(t6);
        toyList.add(t7);toyList.add(t8);toyList.add(t9);

        toyAdapter=new ToyAdapter(this,0,0,toyList);

        lv=(ListView)findViewById(R.id.lvList);
        lv.setAdapter(toyAdapter);
    }
}
