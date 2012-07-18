package es.fenoll.javier;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class gestionaIntervalos extends Activity implements OnClickListener{

	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     
        setContentView(R.layout.intervalos);
        
       
        
        
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
			
			
			int rondas = Integer.parseInt( ((TextView) findViewById(R.id.rondas)).getText().toString() );
			int prepara = Integer.parseInt( ((TextView) findViewById(R.id.prepara)).getText().toString() );
			int trota = Integer.parseInt( ((TextView) findViewById(R.id.trota)).getText().toString() );
			int corre = Integer.parseInt( ((TextView) findViewById(R.id.corre)).getText().toString() );

			
			ArrayList<Intervalo> intervalos = new ArrayList<Intervalo>();
			intervalos.add( new Intervalo("preparacion",(long) prepara * 1000,"ms") );
			for (int i=0; i<rondas; i++ ) {
				intervalos.add( new Intervalo("trota", (long) trota * 1000 ,"ms") );
				intervalos.add(new Intervalo("corre",(long) corre * 1000,"ms") );
			}
			
					
			
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
