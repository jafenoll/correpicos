package es.fenoll.javier;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class Registra extends Activity implements OnClickListener{
    
	// true o falsi si estoy registrasndo o estoy en pause
	private boolean tracking;
	
	private LocationRegisterService locationListener;
	private GestureDetector gestureDetector;
	private MyGestureDetector gestureListener;
	private FormatosDisplay cambioFormatos;
	// cada cuantos ms llamo para actualizar el reloj
	
	//para el mapa
	private MapView map;
	
	private AlmacenDatos.Deporte elDeporte;
	
 	// para controlar lso gestos y en un swipe pasar a la otra pantalla
 	private	class MyGestureDetector extends SimpleOnGestureListener {
 		
 		private static final int SWIPE_MIN_DISTANCE = 120;
 		//private static final int SWIPE_MAX_OFF_PATH = 250;
 		private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 		
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 			
 			if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 				Mueve(true);
 				return true;
 				}
 			else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 				Mueve(false);
 				return true;
 				}
 		
 			return false;
 			}
 		
 		public void Mueve(boolean izquierda) {
 		
 			ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.pantallasegistra);
 			
 			
 			if (izquierda) {
 				viewFlipper.setInAnimation( AnimationUtils.loadAnimation(viewFlipper.getContext(), R.anim.push_left_in) );
 				viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(viewFlipper.getContext(), R.anim.push_left_out) );
 				viewFlipper.showNext();
 			}
 			else {
 				viewFlipper.setInAnimation( AnimationUtils.loadAnimation(viewFlipper.getContext(), R.anim.push_right_in) );
 				viewFlipper.setOutAnimation( AnimationUtils.loadAnimation(viewFlipper.getContext(), R.anim.push_right_out) );
 				viewFlipper.showPrevious();
 			}
 			
 			
 			
 		
 		}
 		
 		
 		
 	}
 	
 	//oculta y pone visible al etiqueta de autopause
 	public void updateAutoPauseUI(boolean estado) {
 		
 		if (estado) {
 			((TextView)findViewById(R.id.displayautopause)).setVisibility(View.VISIBLE);
 			((TextView)findViewById(R.id.displayvelocidadMms)).setText( "0.00"  );
 		}
 		else
 			((TextView)findViewById(R.id.displayautopause)).setVisibility(View.INVISIBLE);
 			
 		
 	}
 	
 	//se llama cuando hay que actualizar el UI
 	public void updateTimerUI(long tiempo) {
		
			((TextView)findViewById(R.id.displayTiempo)).setText( cambioFormatos.desdeMStoHHMM(tiempo)  );
			((TextView)findViewById(R.id.displayTiempoSS)).setText( cambioFormatos.desdeMSobtenSS(tiempo)  );
			
			
 	}
 	
 	public void updateLocUI(Location location, double distanciaTotal, int altitudAcumPos, int altitudAcumNeg) {
 		
 		if ( location.hasSpeed() ) {
			// velocidad, de m/s en que llega lo paso a km/h
			//TODO: valorar si la que da el GPS varia mucho por los errores y eso, y si seria conveninete poner la de los ´´ultimos 100m o algo similar
			((TextView)findViewById(R.id.displayvelocidadMms)).setText( cambioFormatos.desdeMsaMKM(location.getSpeed())  );
		}
 		
 		int Km = (int) distanciaTotal/1000;
		double Dm = (distanciaTotal/1000-Km)*10;
		
		NumberFormat formatter = new DecimalFormat("#");
		((TextView)findViewById(R.id.displayDistancia)).setText( formatter.format(Km)  );
		((TextView)findViewById(R.id.displayDistanciaM)).setText( "." + formatter.format(Dm)  );
		
		((TextView)findViewById(R.id.displayAltitudAcumPos)).setText( "+" + altitudAcumPos  );
		((TextView)findViewById(R.id.displayAltitudAcumNeg)).setText( "-" + altitudAcumNeg  );
		
		
		
 	}
 	

	// esta serviceconection es para poder coger una conexion (referencia)
	// al servicio que voy a arrancar para el tema del GPS
	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	    	locationListener = ((LocationRegisterService.LocalBinder)service).getService();
	    	
	    	//una vez hecho el bind, me engancho para recibir las notif de UI
			locationListener.setUIManager( Registra.this);
			
			if ( ! locationListener.isTracking()) {
				// si no esta registrando ya, puede ser que sea el primer bind
				// del activity asi que
				
				// lanzo un  startService 
		     	// si solo hago el bind, cuando el activity se cierra por sleep el servicio se para
				startService(new Intent(Registra.this,LocationRegisterService.class));
				
				
				// paso param del deporte  al servicio para que los tenga en cuenta
				//por umbralpaso -1 para off del autopause y si no el umbral en m/s
				double umbral = -1;
				if (elDeporte.autopause) {
					umbral = (elDeporte.umbralautopause) * 1000/3600;
				}
		     	locationListener.startListeningLoc(elDeporte.gpsgaptmp,elDeporte.gpsgapdist, umbral);

				
			}
			else {
				
				changestartTrackingButton("pause");
			}
			
			
			
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	    	locationListener = null;
	        
	    }
	};
	
	private void changestartTrackingButton(String estado){
		
		
		TextView tv = ((TextView)findViewById(R.id.startTracking));
		
		if (estado == "pause") {
			tv.setText(R.string.PauseTrackingLbl);
			tv.setTextColor(0xffbe8703);
		}
		else if( estado == "start" ) {
			tv.setText(R.string.startTrakingLbl);
			tv.setTextColor(0xff03be10);
		}
		else if( estado == "resume" ) {
			tv.setText(R.string.ResumeTrackingLbl);
			tv.setTextColor(0xff03be10);
		}
		
	}
 	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
		cambioFormatos = new  FormatosDisplay();
		
		//busco los parametros de tiempo y distancia para el deporte
		AlmacenDatos registroDB = new AlmacenDatos( (Context) this.getApplication() ); 
     	elDeporte = registroDB.recuperaDeporte(0);
		
		// hago bind al servicio
		bindService(new Intent(this,LocationRegisterService.class)
					, mConnection, Context.BIND_AUTO_CREATE);

     	// engancho para detectar el swipe
     	gestureListener = new MyGestureDetector();
     	gestureDetector = new GestureDetector(gestureListener);
     	
     	
        setContentView(R.layout.registra);  
        
        Button button = (Button)findViewById(R.id.startTracking);
        button.setOnClickListener(this);
        button = (Button)findViewById(R.id.paraRegistro);
        button.setOnClickListener(this);
       
        button = (Button)findViewById(R.id.cierra);
        button.setOnClickListener(this);
        ImageButton imgbutton = (ImageButton)findViewById(R.id.swipenext);
        imgbutton.setOnClickListener(this);

        
        updateAutoPauseUI(false);
        
        //creo el mapa
        map = (MapView)findViewById(R.id.mapaRegistra);
        //map.getController().setZoom(16);
        
        
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(16);
        
        ResourceProxy  mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
        MyLocationOverlay mLocationOverlay;
        mLocationOverlay = new MyLocationOverlay(this.getBaseContext(), map, mResourceProxy);
        map.getOverlays().add(mLocationOverlay);
        mLocationOverlay.enableFollowLocation();
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableCompass();
        
        
        //algunos metodos de mLocationOverlay
        //realmente mLocationOverlay esta escuchando tambien la posición
        // asi que tengo dos clases haciendo esto para lo mismo, muy optimo
        // no es pero como metodo de rusar codigo puede no estar mal
        // cada clase hace una cosa diferente
        // sino tendía que o exterder mLocationOverlay  para hacer lo que yo quiero
        // o coger su codigo fuente y modificarlo.
        //
        mLocationOverlay.setLocationUpdateMinDistance(elDeporte.gpsgapdist); 
        mLocationOverlay.setLocationUpdateMinTime( elDeporte.gpsgaptmp * 1000);
          
        
        
    }

    @Override
    // engancho el detector del swipe
    public boolean onTouchEvent(MotionEvent event) {
    	if (gestureDetector.onTouchEvent(event))
    		return true;
    	else
    		return false;
    }
    
	@Override
	public void onClick(View v) {
		
		// si pulsa a salir vuelvo a pantalal principal
		if ( v.getId() == R.id.paraRegistro ) {
			
			//cuanto antes paro de registrar
			tracking = false;
			
			// y cierro el servicio
			locationListener.endRecording();
			//hago el unbind y el stop por que hice el bind y el start, por los temas de sleep
			unbindService(mConnection);
			locationListener.stopSelf();
			//locationListener = null;
					
			
			changestartTrackingButton("start");
			
			// vuelvo a la actividad principal
			Intent intent = new Intent(this, correpicos.class);
			startActivity(intent);
			
			return;
		}
		else if (v.getId() == R.id.startTracking ) {
			// este boton hace dos cosas, depende de si ya estoy registrando o no
			// que lo se por el valor de tracking
			

			// estoy registrando, paro
			if (tracking) {
				
				locationListener.pauseRecording();
				
				tracking = false;
				changestartTrackingButton("resume");
				  
			}
			else {
				
				locationListener.startRecording();
				
				tracking = true;
				// coloco los controles
				changestartTrackingButton("pause");
	
			}
			
		}
		else if( v.getId() == R.id.cierra) {
			finish();	
		}
		else if( v.getId() == R.id.swipenext) {
			
			gestureListener.Mueve(true);
			
		}
		
		
		
		}

	
}