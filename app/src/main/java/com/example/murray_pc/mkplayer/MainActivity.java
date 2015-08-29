package com.example.murray_pc.mkplayer;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.TreeMap;


public class MainActivity extends ListActivity {
    MediaMetadataRetriever metadataRetriever; // For meta-Data
    AlertDialog.Builder details;               // For Displaying meta-Data
    File f;
    MediaPlayer mp;
    ArrayList<String> al;
    TreeMap<String,String> songList ;
    ArrayAdapter<String> adapter;
    String artist,title,genre;
    double size;
    ListView lv ;
    int i =0;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_main);
        initializeNotification();
        initializeList();
    }   //end of OnCreate

    private void initializeNotification() {
        builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("MK Player");
        builder.setSmallIcon(R.drawable.notify);
        builder.setOngoing(true);   // to add notification in ongoing cateogory, you will know it better when you run the app


        int id =1;
        Intent intent = new Intent(this,MainActivity.class);    // making intent to open MainACtivity from notification
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP); // to simply jump to previous state of app

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(intent);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1,builder.build());

    }
    // populating ListView with .mp3 file
    private void initializeList() {
        songList = new TreeMap<String,String>();
        registerForContextMenu(getListView());
        f = Environment.getExternalStorageDirectory();
        String p1 = f.getParent();

        while (!p1.equals("/")){
            f = new File(p1);
            p1 = f.getParent();
        }

        if(songList != null) {
            add(f);          // helperFunction to add files to map
        }
        al = new ArrayList<>(songList.keySet());
        adapter =  new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, al);
        setListAdapter(adapter);

    }
    // helperFunction to add files to map
    public void add(File f)
    {
       File files[];
       if((f.isDirectory())&&(files = f.listFiles())!=null)
        {
            for(File ser : files)
            {
                add(ser);
            }
        }  else {
           if(f.getName().endsWith(".mp3")) {
             songList.put(f.getName().toLowerCase().replace(".mp3",""),f.getAbsolutePath());

           }
       }
//     add(fil);
// }
        lv = getListView();
        lv.setTextFilterEnabled(true);
}
   //end of helperFunction to add files to map
    @Override
    protected void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(1);
    }

     //@Override
     protected void onListItemClick(ListView l ,View v, int position, long id)
    {   super.onListItemClick(l,v,position,id);

        if(mp!=null) {
            if (mp.isPlaying()) {
                mp.stop();
            }
        }
        // previousIndex = position;  (No Use)

        // to start Music
        startMusic(position);
    }

    // to start Music
    private void startMusic( final int position) {
        mp = new MediaPlayer();
        try {

            mp.setDataSource(this, Uri.parse(songList.get(al.get(position))));
            mp.prepare();
            mp.start();
            builder.setContentText(al.get(position));
            notificationManager.notify(1,builder.build());
        } catch (IOException e) {
            e.printStackTrace();
        }

            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                   if(ran == 1)
                   {
                    Random random = new Random();
                    int newRan = random.nextInt(al.size()-1);
                    startMusic(newRan);}
                    else if(ran == 0)
                    {
                        if(position == al.size()-1) {
                            startMusic(0);
                        }else startMusic(position+1);
                    }
                }
            });



    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Options");
        menu.add(0, v.getId(), 0, "Pause");  // to Pause Music
        menu.add(0,v.getId(),0,"Details");   // to get Details
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals("Pause"))  //Pause
        {
            if(mp!=null && mp.isPlaying()){
                mp.pause();
            }
        }
        else if(item.getTitle().equals("Details"))
        {
            AdapterView.AdapterContextMenuInfo info
                    = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            int o = (int) info.id;
            metadataRetriever = new MediaMetadataRetriever();

            metadataRetriever.setDataSource(songList.get(al.get(o)));
            if(metadataRetriever != null)
               init(o);
            else
              Toast.makeText(MainActivity.this,"could not be retrieved",Toast.LENGTH_LONG).show();

            details = new AlertDialog.Builder(this);
            details.setTitle(title);
            details.setMessage("Artist : " + artist+"\nGenre : "+genre+"\nSize  : "+String.valueOf(size)+" mb");
            details.show();
        }


        return true;
    }

    private void init(int o) {
        artist =  new String();
        title  =  new String();
        genre  =  new String();
        artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);  // Artist
        title  = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);   // Title
        genre  = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);   // Genre
        File v = new File(songList.get(al.get(o)));
        size = v.length()/1024;
        size/=1024;
        size = Double.parseDouble(new DecimalFormat("##.##").format(size));

        if(title == null || title.equals("")) {
            title = al.get(o);
            title = title.replace(".mp3", "");

        }
        if(artist == null || artist.equals(""))
             artist = "Unknown Artist";
        if(genre == null || genre.equals(""))
            genre = "Not Available";



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
       // adapter = (ArrayAdapter<String>) getListAdapter();
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return  true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return  true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings) {
            options();
        }
        else if (id == R.id.shuffle) {

                Collections.shuffle(al);
                adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, al);
                setListAdapter(adapter);

            } else if (id == R.id.sort && songList != null) {

                Collections.sort(al);
                adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, al);
                setListAdapter(adapter);
            }


        return super.onOptionsItemSelected(item);
    }

    static int ran = 0;
    private void options() {

        AlertDialog.Builder settings = new AlertDialog.Builder(this);
        final CharSequence[] mode ={"order play","random play"};
        int selection = 0 ;
        settings.setSingleChoiceItems(mode,ran, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 1) {
                    ran = 1;
                    dialog.dismiss();

                } else if (which == 0) {
                    ran = 0;
                    dialog.dismiss();

                }

            }

        });


            AlertDialog  m = settings.create();
            m.show();

    }

    private static long back_pressed;

    @Override
    public void onBackPressed() {
        if(back_pressed + 2000 > System.currentTimeMillis()) {
            if(mp!=null)
                mp.stop();
            notificationManager.cancel(1);
            super.onBackPressed();
        }
        else
            Toast.makeText(MainActivity.this,"Press once again to exit",Toast.LENGTH_SHORT
            ).show();
        back_pressed = System.currentTimeMillis();
    }


}
