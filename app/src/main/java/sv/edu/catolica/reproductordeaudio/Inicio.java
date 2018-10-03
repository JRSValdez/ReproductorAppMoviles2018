package sv.edu.catolica.reproductordeaudio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class Inicio extends Activity {


    private Boolean botonBackPresionado=false;
    private static final int DURACION_SPLASH=5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        Handler manejador= new Handler();
        manejador.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                if(!botonBackPresionado){
                    Intent intento= new Intent(Inicio.this, PrincipalScreen.class);
                    startActivity(intento);
                }
            }
        },DURACION_SPLASH);
    }

    public void onBackPressed(){
        botonBackPresionado=true;
        super.onBackPressed();
    }

}
