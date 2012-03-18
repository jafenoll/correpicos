package es.fenoll.javier;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class ConfigDeportes extends Activity implements OnClickListener{

	
	private AlmacenDatos registroDB;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     
        setContentView(R.layout.configdeportes);
        
        // TODO: lista de deportes
        // por ahora solo tengo un deporte que es que tiene id = 0 
        // en el futuro esto vendr´´a desde una lista y lo que me llegar´´an 
        // ya aqui ser´´a el id ya pasado en el extra
      
        
        // Preparo el cursor para leer el deporte
        registroDB = new AlmacenDatos( (Context) this.getApplication() );   
        AlmacenDatos.Deporte elDeporte = registroDB.recuperaDeporte(0);
        
        
        ((TextView)findViewById(R.id.deporteNombre)).setText(elDeporte.nombre);
        ((TextView)findViewById(R.id.deporteGPSgapDIST)).setText( Integer.toString(elDeporte.gpsgapdist) );
        ((TextView)findViewById(R.id.deporteGPSgapTMP)).setText( Integer.toString(elDeporte.gpsgaptmp) );
        ((CheckBox)findViewById(R.id.umbralAutoPauseChK)).setChecked( elDeporte.autopause );
        ((TextView)findViewById(R.id.umbralAutoPause)).setText( Double.toString(elDeporte.umbralautopause) );
        
        
        
        
        
        //engancho los listener 
        Button button = (Button)findViewById(R.id.deportesOK);
        button.setOnClickListener(this);
        button = (Button)findViewById(R.id.deportesCancel);
        button.setOnClickListener(this);
        button = (Button)findViewById(R.id.deportesDefault);
        button.setOnClickListener(this);
        button = (Button)findViewById(R.id.cierra);
        button.setOnClickListener(this);
        
    }

	@Override
	public void onClick(View v) {

		if ( v.getId() == R.id.deportesOK ) {
			//salvo el deporte y me vuelvo
			AlmacenDatos.Deporte elDeporte = registroDB.new Deporte();
			
			elDeporte.nombre = ((TextView) findViewById(R.id.deporteNombre)).getText().toString();
			elDeporte.gpsgapdist = Integer.parseInt( ((TextView) findViewById(R.id.deporteGPSgapDIST)).getText().toString() );
			elDeporte.gpsgaptmp = Integer.parseInt( ((TextView) findViewById(R.id.deporteGPSgapTMP)).getText().toString() );
			elDeporte.autopause = ((CheckBox)findViewById(R.id.umbralAutoPauseChK)).isChecked();
			elDeporte.umbralautopause = Double.parseDouble( ((TextView) findViewById(R.id.umbralAutoPause)).getText().toString() );
			
			registroDB.actualizaDeporte(0,elDeporte);
		}
		else if( v.getId() == R.id.cierra) {
			finish();	
		}
		else if( v.getId() == R.id.deportesCancel) {
			finish();	
		}
		else if( v.getId() == R.id.deportesDefault) {

	        ((TextView)findViewById(R.id.deporteNombre)).setText("correr");
	        ((TextView)findViewById(R.id.deporteGPSgapDIST)).setText( Integer.toString(10) );
	        ((TextView)findViewById(R.id.deporteGPSgapTMP)).setText( Integer.toString(6) );
	        ((CheckBox)findViewById(R.id.umbralAutoPauseChK)).setChecked( true );
	        ((TextView)findViewById(R.id.umbralAutoPause)).setText( Double.toString(0.5) );
	        
	        	
		}
		
		
	}
	
}
