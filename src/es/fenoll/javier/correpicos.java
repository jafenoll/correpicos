package es.fenoll.javier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;




public class correpicos extends Activity  implements OnClickListener {
    
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
           
        setContentView(R.layout.main);
        
        Button button = (Button)findViewById(R.id.startRegistra);
        button.setOnClickListener(this);
        button = (Button)findViewById(R.id.listaSesiones);
        button.setOnClickListener(this);
        button = (Button)findViewById(R.id.confDeportes);
        button.setOnClickListener(this);
        
        //actualizo la DB si fuese necesario
        AlmacenDatos registroDB = new AlmacenDatos( (Context) this.getApplication() );   
        registroDB.ForceUpgradeDB();
        // paso sesiones a la SD si tengo alguna todabia en la memoria interna (por un cuelgue etc...)
        registroDB.PasaSesionesSD();
        
        
    }

    @Override
	public void onClick(View v) {
		
		if ( v.getId() == R.id.startRegistra ) {
			Intent intent = new Intent(this, Registra.class);
			startActivity(intent);
		}
		else if ( v.getId() == R.id.listaSesiones ) {
			Intent intent = new Intent(this, ListaSesiones.class);
			startActivity(intent);
		}
		else if ( v.getId() == R.id.confDeportes ) {
			Intent intent = new Intent(this, ConfigDeportes.class);
			startActivity(intent);
		}
		
		
		// TODO: Montar un boton para configuracion de deportes
		
		
	}

	

	
	

	

	
	
	
}