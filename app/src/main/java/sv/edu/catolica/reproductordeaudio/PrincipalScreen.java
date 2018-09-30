package sv.edu.catolica.reproductordeaudio;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class PrincipalScreen extends AppCompatActivity {
    private static final int UPDATE_FREQUENCY=500;
    private static final int STEP_VALUE=4000;

    ArrayList<String> arrayList;
    ArrayAdapter a;
    private ListView list;
    private ImageButton Play_pause, Prev, Next;
    private SeekBar skSong;
    private TextView tvTime, txt1;
    private Handler skHandler = new Handler();
    MediaPlayer mp;

    int posicion = 0, count=1;
    MediaPlayer mpLista[] = new MediaPlayer[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_screen);

        Play_pause=(ImageButton) findViewById(R.id.play_stop);
        Prev=(ImageButton)findViewById(R.id.anterior);
        Next=(ImageButton)findViewById(R.id.siguiente);
        skSong = (SeekBar) findViewById(R.id.skSong);
        tvTime = (TextView) findViewById(R.id.tvTime);
        txt1 = (TextView) findViewById(R.id.Txt1);
        list=findViewById(R.id.list);

        arrayList = new ArrayList<String>();
        Field[] songs = R.raw.class.getFields();
        for(int i=0; i<songs.length; i++){
            arrayList.add(songs[i].getName());
        }

        a=new ArrayAdapter(PrincipalScreen.this, android.R.layout.simple_list_item_1, arrayList);
        list.setAdapter(a);

        txt1.setText(count+ "/" +songs.length);

        registerForContextMenu(list);

        int id = getResources().getIdentifier(arrayList.get(posicion), "raw", getPackageName());
        mp= MediaPlayer.create(PrincipalScreen.this, id);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(mp != null){
                    mp.release();
                }
                posicion=i;

                count=i+1;
                Field[] songs = R.raw.class.getFields();
                txt1.setText(count+ "/" +songs.length);

                int id = getResources().getIdentifier(arrayList.get(i), "raw", getPackageName());

                mp= MediaPlayer.create(PrincipalScreen.this, id);
                mp.start();

                tvTime.setText( getHRM(mp.getDuration()) );
                skSong.setMax(mp.getDuration());
                skSong.setProgress(mp.getCurrentPosition());

                //cada segundo se actualiza el estado del seek bar
                skHandler.postDelayed(updateskSong, 1000);

                Play_pause.setBackgroundResource(R.drawable.pause64x64);
                Toast.makeText(PrincipalScreen.this,"Reproduciendo: " +arrayList.get(i), Toast.LENGTH_SHORT).show();
            }

        });
        skSong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
                    mp.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                skSong.setMax(mp.getDuration());

                SeekBar();
            }
        });

    }

    public void Anterior(View v){
        if(posicion>=1){
            if(mp.isPlaying()){
                mp.stop();
                posicion--;
                int id = getResources().getIdentifier(arrayList.get(posicion), "raw", getPackageName());
                mp= MediaPlayer.create(PrincipalScreen.this, id);
                mp.start();

                count=posicion+1;
                Field[] songs = R.raw.class.getFields();
                txt1.setText(count+ "/" +songs.length);
            }else{
                posicion--;
                count=posicion+1;
            }
        }else{
            Toast.makeText(PrincipalScreen.this,"No hay más canciones", Toast.LENGTH_SHORT).show();
        }
    }
    public void PlayStop(View v){
        if (mp.isPlaying()){
            mp.pause();
            Play_pause.setBackgroundResource(R.drawable.play_64x64);
            Toast.makeText(PrincipalScreen.this,"Pausa", Toast.LENGTH_SHORT).show();
        }else{
            try{
                mp.start();
                //--- Se coloca el tiempo de duracion y se inicia el seek bar
                tvTime.setText( getHRM(mp.getDuration()) );
                skSong.setMax(mp.getDuration());
                skSong.setProgress(mp.getCurrentPosition());

                //cada segundo se actualiza el estado del seek bar
                skHandler.postDelayed(updateskSong, 1000);



                Play_pause.setBackgroundResource(R.drawable.pause64x64);
                Toast.makeText(PrincipalScreen.this,"Reproduciendo", Toast.LENGTH_SHORT).show();
            }catch (IllegalStateException e){
                e.printStackTrace();
            }
        }
    }
    public void Siguiente(View v){
        Field[] songs = R.raw.class.getFields();
        if(posicion<songs.length-1){
            if(mp.isPlaying()){
                mp.stop();
                posicion++;
                int id = getResources().getIdentifier(arrayList.get(posicion), "raw", getPackageName());
                mp= MediaPlayer.create(PrincipalScreen.this, id);
                mp.start();

                count=posicion+1;
                txt1.setText(count+ "/" +songs.length);
            }else{
                posicion++;
                count=posicion+1;
            }
        }else{
            Toast.makeText(PrincipalScreen.this,"No hay más canciones", Toast.LENGTH_SHORT).show();
        }
    }

    private String getHRM(int milliseconds )
    {
        int seconds = (int) (milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000*60)) % 60);
        return ((minutes<10)?"0"+minutes:minutes) + ":" +
                ((seconds<10)?"0"+seconds:seconds);
    }

    Runnable updateskSong = new Runnable() {
        @Override
        public void run() {
            skSong.setProgress(mp.getCurrentPosition() );
            tvTime.setText(getHRM(mp.getCurrentPosition()) + " - " + getHRM(mp.getDuration()));
            skHandler.postDelayed(updateskSong, 1000);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater i = getMenuInflater();
        if(v.getId()==R.id.list){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

            menu.setHeaderTitle(list.getAdapter().getItem(info.position).toString());

            i.inflate(R.menu.menucontextual, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.agregar:

                return true;
            case R.id.salir:
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setCancelable(false);
                b.setTitle("Confirmación");
                b.setMessage("¿Realmente desea cerrar la aplicación?");
                b.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                b.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                b.create();
                b.show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo i = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()){
            case R.id.reproducir:
                if(mp != null){
                    mp.release();
                }
                posicion=i.position;

                int id = getResources().getIdentifier(arrayList.get(i.position), "raw", getPackageName());

                mp= MediaPlayer.create(PrincipalScreen.this, id);
                mp.start();

                tvTime.setText( getHRM(mp.getDuration()) );
                skSong.setMax(mp.getDuration());
                skSong.setProgress(mp.getCurrentPosition());

                //cada segundo se actualiza el estado del seek bar
                skHandler.postDelayed(updateskSong, 1000);

                Play_pause.setBackgroundResource(R.drawable.pause64x64);
                Toast.makeText(PrincipalScreen.this,"Reproduciendo: " +arrayList.get(i.position), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.pausar:
                if (mp.isPlaying()){
                    mp.pause();
                    Play_pause.setBackgroundResource(R.drawable.play_64x64);
                    Toast.makeText(PrincipalScreen.this,"Pausa", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.detener:
                if (mp.isPlaying()){
                    mp.stop();
                    Play_pause.setBackgroundResource(R.drawable.play_64x64);
                    Toast.makeText(PrincipalScreen.this,"Detener", Toast.LENGTH_SHORT).show();

                    int iD = getResources().getIdentifier(arrayList.get(i.position), "raw", getPackageName());
                    mp= MediaPlayer.create(PrincipalScreen.this, iD);
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    private void SeekBar(){
        skSong.setProgress(mp.getCurrentPosition());
        if(mp.isPlaying()){
            updateskSong = new Runnable() {
                @Override
                public void run() {
                    SeekBar();
                }
            };
            tvTime.setText(getHRM(mp.getCurrentPosition()) + " - " + getHRM(mp.getDuration()));
            skHandler.postDelayed(updateskSong, 1000);
            mp.start();
        }
    }
}
