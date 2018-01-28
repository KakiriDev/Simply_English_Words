package com.kakiridev.simpleenglishwords;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class WordListView extends AppCompatActivity {

    private ArrayList<Word> words = new ArrayList<Word>();
    WordListViewAdapter adapter;
    Word word;
    ListView listview;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list_view);

        Log.v("DTAG", "Class name: " + Thread.currentThread().getStackTrace()[2].getClassName() + " Method name: " + Thread.currentThread().getStackTrace()[2].getMethodName() + " Words count: " + words.size());

        words = getWords();

        adapter = new WordListViewAdapter(this, R.layout.word_listview_row,words);
        listview = findViewById(R.id.listview);
        listview.setAdapter(adapter);
        initListViewOnItemLongClick();
        registerForContextMenu(listview);
    }


    // Add Context Menu
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){

        //menu.setHeaderTitle(taskDesc);

        menu.add(0, v.getId(), 0, "Edit");
        menu.add(0, v.getId(), 0, "Delete");
    }


    /** wykonuje się po wybraniu opcji z menu knteksowego **/
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo menuinfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        long selectid = menuinfo.id; //_id from database in this case
        int selectpos = menuinfo.position; //position in the adapter
        word = words.get(selectpos); // wskazany task

        if(item.getTitle()=="Edit") {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(); //pobranie referencji do bazy
            mDatabase.child("Words").child(word.getmId()); // zapis do bazy do childrena "grupa"

            intent = new Intent(getApplicationContext(), AddWordView.class);
            Toast.makeText(this, "isE " + word.getmNazwaPl(), Toast.LENGTH_LONG).show();
            intent.putExtra("isEdit", true);
            intent.putExtra("id", word.getmId());
            intent.putExtra("nazwaPl", word.getmNazwaPl());
            intent.putExtra("nazwaEn", word.getmNazwaEn());
            intent.putExtra("category", word.getmCategory());
            startActivity(intent);

            //startActivityForResult(intent, 1);
/**
            HashMap<String, String> dataMap = new HashMap<String, String>(); //kolejność wczytania do bazy odwrotna niz zapisu
            dataMap.put("id", word.getmId());
            dataMap.put("nazwaPl", data.getStringExtra("nazwaPl"));
            dataMap.put("nazwaEn", data.getStringExtra("nazwaEn"));
            dataMap.put("category", data.getStringExtra("category"));

            //sprawdzenie poprawnosci zapisu do bazy
            mDatabase.setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(WordListView.this, "Stored.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(WordListView.this, "Error.", Toast.LENGTH_LONG).show();
                    }
                }
            });
 **/
        }
        if(item.getTitle()=="Delete") {

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            Query applesQuery = ref.child("Words").orderByChild("id").equalTo(word.getmId());

            applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                        appleSnapshot.getRef().removeValue();
                        words.clear();
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("DTAG", "onCancelled", databaseError.toException());
                }
            });
        }
        return super.onContextItemSelected(item);
    }


    // add longClickListener
    private void initListViewOnItemLongClick() {
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {

                Word task = words.get(position);
                String wordId = task.getmId();
                Log.v("DTAG","pos: " + position);
                return false;
            }
        });
    }



    // Get all words for firebase and add it to "words" list
    private ArrayList<Word> getWords() {
        Log.v("DTAG", "Class name: " + Thread.currentThread().getStackTrace()[2].getClassName() + " Method name: " + Thread.currentThread().getStackTrace()[2].getMethodName() + " Words count: " + words.size());

        DatabaseReference mDatabaseWords = FirebaseDatabase.getInstance().getReference().child("Words");
        final ArrayList<Word> fbWords = new ArrayList<Word>();
        words.clear();
//        mDatabaseWords.addListenerForSingleValueEvent(new ValueEventListener() {
        mDatabaseWords.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.v("DTAG", "Class name: " + Thread.currentThread().getStackTrace()[2].getClassName() + " Method name: " + Thread.currentThread().getStackTrace()[2].getMethodName() + " Words count: " + words.size());

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                    String id = dsp.child("id").getValue().toString();
                    String pl = dsp.child("nazwaPl").getValue().toString();
                    String en = dsp.child("nazwaEn").getValue().toString();
                    String category = dsp.child("category").getValue().toString();
                    Word rekord = new Word(id, pl, en, category);

                    fbWords.add(rekord);

                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("DTAG", "Class name: " + Thread.currentThread().getStackTrace()[2].getClassName() + " Method name: " + Thread.currentThread().getStackTrace()[2].getMethodName() + " Words count: " + words.size());
            }

        });

        return fbWords;
    }



}
