package sv.edu.catolica.reproductordeaudio;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuWrapperFactory;
import android.util.Log;
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;

public class PrincipalScreen extends AppCompatActivity {
    private static final int UPDATE_FREQUENCY = 500;
    private static final int STEP_VALUE = 4000;
    ArrayList<String> arrayList;
    ArrayAdapter a;
    private ListView list;
    private ImageButton Play_pause, Prev, Next;
    private SeekBar skSong;
    private TextView tvTime, txt1;
    private Handler skHandler = new Handler();
    private int VALOR_RETORNO = 1;
    MediaPlayer mp;
    String ruta=Environment.getExternalStorageDirectory().getAbsolutePath();

    int posicion = 0, count = 1;
    MediaPlayer mpLista[] = new MediaPlayer[2];

    Uri[] cancionesExternas = new Uri[10];
    int conExt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_screen);

        Play_pause = (ImageButton) findViewById(R.id.play_stop);
        Prev = (ImageButton) findViewById(R.id.anterior);
        Next = (ImageButton) findViewById(R.id.siguiente);
        skSong = (SeekBar) findViewById(R.id.skSong);
        tvTime = (TextView) findViewById(R.id.tvTime);
        txt1 = (TextView) findViewById(R.id.Txt1);
        list = findViewById(R.id.list);

        arrayList = new ArrayList<String>();
        Field[] songs = R.raw.class.getFields();
        for (int i = 0; i < songs.length; i++) {
            arrayList.add(songs[i].getName());
        }
        a = new ArrayAdapter(PrincipalScreen.this, android.R.layout.simple_list_item_1, arrayList);
        list.setAdapter(a);

        txt1.setText(count + "/" + songs.length);

        registerForContextMenu(list);

        int id = getResources().getIdentifier(arrayList.get(posicion), "raw", getPackageName());

        mp = MediaPlayer.create(PrincipalScreen.this, id);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mp != null) {
                    mp.release();
                }
                posicion = i;

                count = i + 1;
                Field[] songs = R.raw.class.getFields();
                txt1.setText(count + "/" + songs.length);

                if(posicion <= 2){
                    int id = getResources().getIdentifier(arrayList.get(i), "raw", getPackageName());

                    mp = MediaPlayer.create(PrincipalScreen.this, id);
                    mp.start();
                } else{
                    mp = new MediaPlayer();
                    mp = MediaPlayer.create(PrincipalScreen.this,cancionesExternas[posicion-3]);
                    mp.start();
                }

                tvTime.setText(getHRM(mp.getDuration()));
                skSong.setMax(mp.getDuration());
                skSong.setProgress(mp.getCurrentPosition());

                //cada segundo se actualiza el estado del seek bar
                skHandler.postDelayed(updateskSong, 1000);

                Play_pause.setBackgroundResource(R.drawable.pause64x64);
                Toast.makeText(PrincipalScreen.this, "Reproduciendo: " + arrayList.get(i), Toast.LENGTH_SHORT).show();
            }

        });
        skSong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
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

    public void Anterior(View v) {
        if (posicion >= 1 ) {
            if(posicion>2){
                if (mp.isPlaying()) {
                    mp.stop();
                    posicion--;
                    mp = new MediaPlayer();
                    if(posicion<=2){
                        int id = getResources().getIdentifier(arrayList.get(posicion), "raw", getPackageName());
                        mp = MediaPlayer.create(PrincipalScreen.this, id);
                        mp.start();
                    } else{
                        mp = MediaPlayer.create(PrincipalScreen.this, cancionesExternas[posicion-3]);
                        mp.start();
                    }


                    count = posicion + 1;
                    Field[] songs = R.raw.class.getFields();
                    txt1.setText(count + "/" + songs.length);
                } else {
                    posicion--;
                    count = posicion + 1;
                }
            }else {
                if (mp.isPlaying()) {
                    mp.stop();
                    posicion--;
                    int id = getResources().getIdentifier(arrayList.get(posicion), "raw", getPackageName());
                    mp = MediaPlayer.create(PrincipalScreen.this, id);
                    mp.start();

                    count = posicion + 1;
                    Field[] songs = R.raw.class.getFields();
                    txt1.setText(count + "/" + songs.length);
                } else {
                    posicion--;
                    count = posicion + 1;
                }
            }
        } else {
            Toast.makeText(PrincipalScreen.this, "No hay más canciones", Toast.LENGTH_SHORT).show();
        }
    }

    public void PlayStop(View v) {
        if (mp.isPlaying()) {
            mp.pause();
            Play_pause.setBackgroundResource(R.drawable.play_64x64);
            Toast.makeText(PrincipalScreen.this, "Pausa", Toast.LENGTH_SHORT).show();
        } else {
            try {
                mp.start();
                //--- Se coloca el tiempo de duracion y se inicia el seek bar
                tvTime.setText(getHRM(mp.getDuration()));
                skSong.setMax(mp.getDuration());
                skSong.setProgress(mp.getCurrentPosition());

                //cada segundo se actualiza el estado del seek bar
                skHandler.postDelayed(updateskSong, 1000);


                Play_pause.setBackgroundResource(R.drawable.pause64x64);
                Toast.makeText(PrincipalScreen.this, "Reproduciendo", Toast.LENGTH_SHORT).show();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    public void Siguiente(View v) {
        Field[] songs = R.raw.class.getFields();
        if (posicion < conExt + 2) {
            if (mp.isPlaying()) {
                mp.stop();
                posicion++;
                if(posicion>2){
                    mp.stop();
                    mp = MediaPlayer.create(PrincipalScreen.this, cancionesExternas[posicion-3]);
                    mp.start();
                } else {
                    int id = getResources().getIdentifier(arrayList.get(posicion), "raw", getPackageName());
                    mp = MediaPlayer.create(PrincipalScreen.this, id);
                    mp.start();
                }
                count = posicion + 1;
                txt1.setText(count + "/" + songs.length);
            } else {
                //posicion++;
                //count = posicion + 1;
            }
        } else {
            Toast.makeText(PrincipalScreen.this, "No hay más canciones", Toast.LENGTH_SHORT).show();
        }
    }

    private String getHRM(int milliseconds) {
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        return ((minutes < 10) ? "0" + minutes : minutes) + ":" +
                ((seconds < 10) ? "0" + seconds : seconds);
    }

    Runnable updateskSong = new Runnable() {
        @Override
        public void run() {
            skSong.setProgress(mp.getCurrentPosition());
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
        if (v.getId() == R.id.list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            menu.setHeaderTitle(list.getAdapter().getItem(info.position).toString());

            i.inflate(R.menu.menucontextual, menu);
        }
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.agregar:

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath());
                intent.setDataAndType(uri, "audio/*");
                startActivityForResult(Intent.createChooser(intent, "Open folder"),VALOR_RETORNO);

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
        switch (item.getItemId()) {
            case R.id.reproducir:
                if (mp != null) {
                    mp.release();
                }
                posicion = i.position;

                int id = getResources().getIdentifier(arrayList.get(i.position), "raw", getPackageName());

                mp = MediaPlayer.create(PrincipalScreen.this, id);
                mp.start();

                tvTime.setText(getHRM(mp.getDuration()));
                skSong.setMax(mp.getDuration());
                skSong.setProgress(mp.getCurrentPosition());

                //cada segundo se actualiza el estado del seek bar
                skHandler.postDelayed(updateskSong, 1000);

                Play_pause.setBackgroundResource(R.drawable.pause64x64);
                Toast.makeText(PrincipalScreen.this, "Reproduciendo: " + arrayList.get(i.position), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.pausar:
                if (mp.isPlaying()) {
                    mp.pause();
                    Play_pause.setBackgroundResource(R.drawable.play_64x64);
                    Toast.makeText(PrincipalScreen.this, "Pausa", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.detener:
                if (mp.isPlaying()) {
                    mp.stop();
                    Play_pause.setBackgroundResource(R.drawable.play_64x64);
                    Toast.makeText(PrincipalScreen.this, "Detener", Toast.LENGTH_SHORT).show();

                    int iD = getResources().getIdentifier(arrayList.get(i.position), "raw", getPackageName());
                    mp = MediaPlayer.create(PrincipalScreen.this, iD);
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void SeekBar() {
        skSong.setProgress(mp.getCurrentPosition());
        if (mp.isPlaying()) {
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



//A este metodo se devuelve el resultado obtenido
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            //Cancelado por el usuario
            Toast.makeText(PrincipalScreen.this,"Importacion cancelada",LENGTH_LONG).show();
        }
        if ((resultCode == RESULT_OK) && (requestCode == VALOR_RETORNO)) {
            //Procesar el resultado
            Uri uri = data.getData(); //obtener el uri content
            String rutaEsp="";
            String nombreCancion="";

            rutaEsp=getRealPath(PrincipalScreen.this,uri);
            nombreCancion=getNombre(rutaEsp);
            arrayList.add(nombreCancion);
            a = new ArrayAdapter(PrincipalScreen.this, android.R.layout.simple_list_item_1, arrayList);
            list.setAdapter(a);

            try {

                   mp= MediaPlayer.create(PrincipalScreen.this, uri);
                   this.cancionesExternas[conExt] = uri;
                   conExt++;
                   mp.prepare();
                   //mp.start();

                Toast.makeText(PrincipalScreen.this,nombreCancion,LENGTH_LONG).show();
            }catch (Exception e){
               // Toast.makeText(PrincipalScreen.this,"error trycatch",LENGTH_LONG).show();

            }


    }
    }


    public static String getRealPath(final Context context, final Uri uri) {

        if (uri.getScheme().equals("file")) {
            return uri.toString();

        } else if (uri.getScheme().equals("content")) {
            // DocumentProvider
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (DocumentsContract.isDocumentUri(context, uri)) {

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
                        final String[] selectionArgs = new String[]{
                                split[1]
                        };

                        return getDataColumn(context, contentUri, selection, selectionArgs);
                    }
                }
            }
        }

        return null;
    }


    public static String getDataColumn(Context context, Uri uri, String selection,
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


    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    public static String getNombre(String uri) {
        String fileName = uri.substring( uri.lastIndexOf('/')+1, uri.length() );
        String fileNameWithoutExtn = fileName.substring(0, fileName.lastIndexOf('.'));
        return fileNameWithoutExtn;
    }


}