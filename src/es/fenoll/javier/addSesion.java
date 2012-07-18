package es.fenoll.javier;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.osmdroid.util.GeoPoint;

import es.fenoll.javier.AlmacenDatos.PuntoGPX;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class addSesion extends Activity implements OnClickListener{

	
	private AlmacenDatos registroDB;
	
	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
    private int mMinute;
    private Date mDate; 
    
    private TextView mTimeDisplay;
	private TextView mDateDisplay;
	
	static final int DATE_DIALOG_ID = 0;
	static final int TIME_DIALOG_ID = 1;

	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     
        setContentView(R.layout.addsesion);
        
        mDateDisplay = (TextView) findViewById(R.id.sesionDate);   
        mDateDisplay.setOnTouchListener(new View.OnTouchListener() {
           
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
			
				showDialog(DATE_DIALOG_ID);
				// we controlado el evento, asi que devuelvo tru ey asi ya no hace lo de
				// por defecto que es enseñar el softKeyboard
				return true;
			}
        });
        
        mTimeDisplay = (TextView) findViewById(R.id.sesionTime);
        mTimeDisplay.setOnTouchListener(new View.OnTouchListener() {
            
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
			
				showDialog(TIME_DIALOG_ID);
				// we controlado el evento, asi que devuelvo tru ey asi ya no hace lo de
				// por defecto que es enseñar el softKeyboard
				return true;
			}
        });

        
        // get the current date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);


        // display the current date
        updateDisplay();


        
        //engancho los listener 
        Button button = (Button)findViewById(R.id.OK);
        button.setOnClickListener(this);
        button = (Button)findViewById(R.id.Cancel);
        button.setOnClickListener(this);
       
        button = (Button)findViewById(R.id.cierra);
        button.setOnClickListener(this);
        
        registroDB = new AlmacenDatos( (Context) this.getApplication() ); 
        
        
    }
    
    private void updateDisplay() {
        
    	String txtFecha= "", txtHora="";
    	
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    	
	
    	
		try {
			NumberFormat formatter = new DecimalFormat("00");
			
			mDate = df.parse( mYear + "-" + (mMonth + 1) + "-" + mDay + " " + mHour + ":" + formatter.format(mMinute) );
			
			df = new SimpleDateFormat("E");
	    	
	    	DateFormat dtf = DateFormat.getDateInstance(DateFormat.MEDIUM  );
	    	
	    	txtFecha = df.format(mDate) + " " + dtf.format(mDate);
	    	
	    	
	    	txtHora = mHour + ":" + formatter.format(mMinute);
			
		} catch (ParseException e) {
			
			txtFecha = "-";
		}
    	
    	
    	this.mDateDisplay.setText( txtFecha );
    	this.mTimeDisplay.setText( txtHora );
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
        
	    case TIME_DIALOG_ID:
	        return new TimePickerDialog(this,
	                mTimeSetListener, mHour, mMinute, true);
	    }

        return null;
    }
    
    private DatePickerDialog.OnDateSetListener mDateSetListener =
    	    new DatePickerDialog.OnDateSetListener() {
    	        public void onDateSet(DatePicker view, int year, 
    	                              int monthOfYear, int dayOfMonth) {
    	            mYear = year;
    	            mMonth = monthOfYear;
    	            mDay = dayOfMonth;
    	            updateDisplay();
    	        }
    	    };
    	    
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
    	    	    new TimePickerDialog.OnTimeSetListener() {
    	    	        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    	    	            mHour = hourOfDay;
    	    	            mMinute = minute;
    	    	            updateDisplay();
    	    	        }
    	    	    };



	@Override
	public void onClick(View v) {

		if ( v.getId() == R.id.OK ) {
			//salvo el deporte y me vuelvo
			
			String valor = ((TextView)findViewById(R.id.sesionKm)).getText().toString();
			if (valor.length() == 0) {
				
				String text =  getText(R.string.sesionKmError).toString();
				Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
		    	toast.show();
				
				return;
			}
			Long distanciaTotal = Long.valueOf ( valor ); 
			// lo pongo en m
			distanciaTotal = distanciaTotal * 1000;
		
			//pasi los hh:mm a ms
			valor = ((TextView)findViewById(R.id.sesionDur)).getText().toString();
			if (valor.length() == 0) {
				
				String text =  getText(R.string.sesionDurError).toString();
				Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
		    	toast.show();
				
				return;
			}
			String duracion =  valor;
			int posColom = duracion.indexOf(":");
			if (posColom == -1 ) {
				// no encuentra los :, decir que esta mal el campo
				
				String text =  getText(R.string.sesionDurError).toString();
				Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
		    	toast.show();
				
				return;
			}
			
			int Horas = 0;
			if (posColom != 0) {	
				Horas = Integer.valueOf( duracion.substring(0,posColom) );
			}
			int Minutos = 0;
			if (posColom != duracion.length()-1 ) {
				Minutos = Integer.valueOf( duracion.substring(posColom+1, duracion.length() ) );
			}
			Double tiempoTranscurrido = ((double)Minutos + ((double)Horas * 60) ) * 60 * 1000;
			
			valor = ((TextView)findViewById(R.id.sesionDesnivelPos)).getText().toString();
			if (valor.length() == 0) valor = "0";
			int altitudAcumPos = Integer.valueOf ( valor );
			valor = ((TextView)findViewById(R.id.sesionDesnivelNeg)).getText().toString();
			if (valor.length() == 0) valor = "0";
			int altitudAcumNeg = Integer.valueOf ( valor );
			
			long sesionId =  registroDB.insertaSesion(mDate);
			
			//en las sesiones de este tipo meto tres puntos falsos en el 0,0
			// para que luego calcule todo bien

			PuntoGPX elPuntoGPX = registroDB.new PuntoGPX();
			elPuntoGPX.posicion = new GeoPoint( 0  , 0 );
			elPuntoGPX.index = (long) 1;
			elPuntoGPX.distancia = (long) 0;
			elPuntoGPX.sesionId = sesionId;
			elPuntoGPX.altitud = (long) 0;
			elPuntoGPX.timestamp = mDate.getTime();		
			registroDB.insertaPunto(elPuntoGPX);
			elPuntoGPX.index = (long) 2;
			elPuntoGPX.distancia =  (long) distanciaTotal/2;
			elPuntoGPX.altitud = (long) altitudAcumPos;
			elPuntoGPX.timestamp = (long) (mDate.getTime() + tiempoTranscurrido/2);	
			registroDB.insertaPunto(elPuntoGPX);
			elPuntoGPX.index = (long) 3;
			elPuntoGPX.distancia =  distanciaTotal;
			elPuntoGPX.altitud = (long) (altitudAcumPos - altitudAcumNeg);
			elPuntoGPX.timestamp = (long) (mDate.getTime() + tiempoTranscurrido );	
			registroDB.insertaPunto(elPuntoGPX);
			
			// actualizo la sesion con los datos globales
			registroDB.terminaSesion(sesionId);
			
			valor = ((TextView)findViewById(R.id.sesionDesc)).getText().toString();
			if (valor.length() != 0) {
				registroDB.actualizaDescSesion(sesionId, valor);
			}
			
			RatingBar miRB = (RatingBar) findViewById(R.id.ratingPuntosSesion);
			int intvalor = (int) miRB.getRating();
			registroDB.actualizaRatingSesion( sesionId, intvalor );
			
			//pongo que hay sesion salvada en el resuktado
			Intent intent = new Intent(this, addSesion.class);
			intent.putExtra("salvado", true);
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
