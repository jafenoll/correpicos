package es.fenoll.javier;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class gestionaIntervalos extends Activity implements OnClickListener{

	private AlmacenDatos registroDB;
   
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     
        setContentView(R.layout.intervalos);
        
        registroDB = new AlmacenDatos( (Context) this.getApplication() );
        
        
        //engancho los listener 
        Button button = (Button)findViewById(R.id.OK);
        button.setOnClickListener(this);
        button = (Button)findViewById(R.id.Cancel);
        button.setOnClickListener(this);
       
        button = (Button)findViewById(R.id.cierra);
        button.setOnClickListener(this);
        
        
        
    }
    
    
    
   
    
    
	@Override
	public void onClick(View v) {

		if ( v.getId() == R.id.OK ) {

			ArrayList<Intervalo> intervalos = new ArrayList<Intervalo>();
			intervalos.add( new Intervalo("preparacion",(long)3000,"ms") );
			intervalos.add( new Intervalo("corre", (long)5000, "ms") );
			intervalos.add( new Intervalo("trota", (long)4000,"ms") );
			intervalos.add(new Intervalo("corre",(long)5000,"ms") );
			intervalos.add( new Intervalo("trota",(long)4000,"ms") );
					
			
			//pongo que hay sesion salvada en el resuktado
			Intent intent = new Intent(this, Registra.class);
			intent.putParcelableArrayListExtra("intervalos", intervalos);
			setResult(RESULT_OK,intent);
			
			// vuelvo a la actividad principal
			finish();	
			
		}
		else if( v.getId() == R.id.cierra) {
			finish();	
		}
		else if( v.getId() == R.id.Cancel) {
			finish();	
		}
		
		
		
	}
	
}
