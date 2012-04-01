package es.fenoll.javier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;


// para que reciba los cambios en la posicion
// lo hago servicio para que no se me pare si la app esta en backgroun
public class LocationRegisterService extends Service implements LocationListener {
		
		//para enlazar con la BBDD donde guardar los puntos
		private AlmacenDatos registroDB;
	
		//variables para la gestion de la posición
		private LocationManager locationManager;
		//para saver si quiero guardar los puntos o no
		private boolean tracking;
		// umbral de autopause en m/s  -1 para desactivar
		private double autopause;
		//para saber si estoy o no en autopause
		private boolean estado_autopause;
		//para saber si el GPS esta bien enganchado
		private boolean estado_gps;
		//cada cuanto tiempo debe recibir una posicion en ms
		private long tiempoCadaPos ;
		//para saber por que punto voy guardando
		private int numPuntos ;
		// distancia total que llevo, para poner en el UI
		private double distanciaTotal ;
		private int altitudAcumPos;
		private int altitudAcumNeg;
		private Location posicionAnterior;
		
		//sesion
		private long sesionId;
		
		//guardo el enlace a la activity que se encargara de poner los valores en la UI
		private Registra UImanager;
		
		
		//variables para el tema del reloj
		private final int REFRESH_RATE = 1000;
		private Handler mHandler;
		private long tiempoTranscurrido;
		// en ms cuando empece a registar
		private long tiempoInicial;
		
		//para notificar que el servicio esta ejecutandose
		private NotificationManager mNM;
		// Unique Identification Number for the Notification.
	    // We use it on Notification start, and to cancel it.
	    private int NOTIFICATION = R.string.CCServiceLocationListener;
		
	  //para que no se duerma el telefono
		private WakeLock wl;
		
		//para gestionar el cronometro
	 	private Runnable startTimer = new Runnable() {
	 	   public void run() {
	 		   tiempoTranscurrido = System.currentTimeMillis() - tiempoInicial;
	 		   updateTimer(tiempoTranscurrido);
	 		   
	 		   
	 		   // si el GPS esta activado, pero no recibo puntos desde por lo 
	 		   // menos el doble del tiempo en que debería, es que estoy parado, hago autopause
	 		   if (estado_gps && autopause != -1 ) {
	 			 
	 			  long tiempoTrans = 0;
	 			   
	 			  if (posicionAnterior != null)
	 				 tiempoTrans = System.currentTimeMillis() - posicionAnterior.getTime() ;
	 			  
	 			  // si el tiempo que ha trancurrido es largo o estaba en autopause y todabía no he empezado a recibir puntos
	 			  if ( tiempoTrans > 2*tiempoCadaPos || estado_autopause   )   {
	 		   
	 				   autopause(posicionAnterior);
	 				   return;
	 			   }
	 		   }
	 		   
	 		   /*
	 		   //para debug por que se me para el registro hago un log
	 		   try {
		 		   File path = new File(Environment.getExternalStorageDirectory()+ File.separator  + "correcaminos");
		 		   File file = new File(path, "sesion.log");
		 		   FileWriter gpxwriter = new FileWriter(file);
			       BufferedWriter out = new BufferedWriter(gpxwriter);
			       out.write("tiempo :" + tiempoTranscurrido + "\n" );
			       out.close();
	 		   }catch (IOException e) {
		    	    Log.e("salvaKML", "Could not write file " + e.getMessage());
	 		   }
	 		   */
	 		   
	 		   mHandler.postDelayed(this,REFRESH_RATE);
	 		   
	 		   
	 		   
	 		}
	 	};
		
	 	private void updateTimer(long tiempo){
	 		if (UImanager != null)
	 			UImanager.updateTimerUI(tiempo);
	 		
	 	}
	 	
	 	private void initSesion() {
	 		tracking = false;
	 		distanciaTotal = 0;
	 		tiempoTranscurrido = 0;
	 		sesionId = -1;
	 		tiempoInicial = 0;
	 		numPuntos = 0;
	 		
	 	}
	 	
	 	// devuelve si ya esta registrando o no
	 	public boolean isTracking() {
	 		
	 		return tracking;
	 	}
	 	
	 	public void setUIManager(Registra activity) {
	 		UImanager = activity;
	 	}
	 	
		// lo llamo para pausar el registro y que luego al reanudar siga contando desde donde estaba
		public void pauseRecording() {
			mHandler.removeCallbacks(startTimer);
			// no dejo de recibir, auqnue como quito el wake lock si pasa un tiempo se duerme
			//locationManager.removeUpdates(this);
			tracking = false;
			posicionAnterior = null;
			if ( wl.isHeld() ) {
				wl.release();	
			}
		}
		
		public void startRecording() {
			//no me duermo
			wl.acquire();
			
			if ( sesionId == -1) {
				sesionId = registroDB.insertaSesion(null);
			}
			
			
			
			// si no autopause el reloj empieza a corre desde el principio		
			if ( autopause == -1 ) {
				// asigno el reloj
				// resto el tiempo trascurrido por si vengo de un pause que no empiece en 0 otra vez
				tiempoInicial = System.currentTimeMillis() - tiempoTranscurrido;
				mHandler.removeCallbacks(startTimer);
				mHandler.postDelayed(startTimer, 0);
			}
			// si quiero autopause solo empieza a contar tiempo cuando llegue la 
			// primera posicion
			else {
				autopause(null);
			}
	        
	        tracking = true;
	        
	     // Display a notification about us starting.  We put an icon in the status bar.
	        showNotification();
	        
	        //y si tiene el au
			
		}
		
		public void endRecording(){

			pauseRecording();
			
			mNM.cancel(NOTIFICATION);
			
			// actualizo la sesion con los datos globales
			registroDB.terminaSesion(sesionId, distanciaTotal,tiempoTranscurrido, altitudAcumPos, altitudAcumNeg);
						
			//  e inicializo la sesion por si vuelve a arrancar
			initSesion();
			
		}
		
		private void autopause(Location loc) {
			
			//pongo elflag de que paso a autopause
			estado_autopause = true;
			// para que el reloj no siga corriendo
			mHandler.removeCallbacks(startTimer);
			
			if (loc != null) {
				//pongo lo valores en el display
				//actualiza los displais en pantalla
				
				// como estoy en autopause dejo la velocidad a 0
				loc.setSpeed(0);
				
				if (UImanager != null)
		 			UImanager.updateLocUI(loc, distanciaTotal, altitudAcumPos, altitudAcumNeg);
				
			}
			
			UImanager.updateAutoPauseUI(true);

		}
		
		// ininia el que el servicio se enganche a los GPS
		// recibe el automause en m/s
		public void startListeningLoc(int timeRegLoc,int distRegLoc, double autopause_entrada) {
			autopause = autopause_entrada;
			
			tiempoCadaPos = timeRegLoc*1000;
			
			// los parametros dicen que recibe nueva posicion cada distRegLoc metros pero que si el 
			// telefono lo decide puede dormirse dutante timeRegLoc seguntos
			// Por eso para el autopause tengo que tener cuidado pues si no me muevo y distRegLoc es gande no me entra un nuevo punto
			//por eso en el reloj, si el GPS esta activo, si pasan mas del doble de segundos de timeRegLoc sin que llegue un punto hago autopause
	     	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, tiempoCadaPos , distRegLoc, this);
	     	
	     	
	     	
		}
		
		// Called when a new location is found by the network location provider.
		public void onLocationChanged(Location location) {
			  
			//si llega un punto es que estoy activo
			estado_gps = true;
			
			// si no estoy registrando, me salgo
			if (tracking == false)
				return;
			
			//autopause
			if (location.getSpeed() < autopause) {
				
				autopause(location);
				
				//hago return y por tanto no grabo este punto
				return;
			}
			
			// si estaba en autpause lo quito , pues como ha llegado hasta aqui es que hay movimiento
			if (estado_autopause == true) {
				estado_autopause = false;
				
				tiempoInicial = System.currentTimeMillis() - tiempoTranscurrido;
				mHandler.removeCallbacks(startTimer);
		        mHandler.postDelayed(startTimer, 0);
		        
		        UImanager.updateAutoPauseUI(false);
			
			}
			
			// TODO: hacer que si la precision es menor que la distancia desde el
			// ultimo punto, no haga nada
			
			
			numPuntos ++ ;
						
			if (posicionAnterior != null) {
				double distanciaTramo = location.distanceTo(posicionAnterior);	
				distanciaTotal += distanciaTramo;
				
				// si los puntos tiene altitud voi acumulando su variacion
				//TODO:la precision de la altitud es muy mala, ver como hacer para que el acumulado valga para algo
				if ( location.hasAltitude() && posicionAnterior.hasAltitude())  {
					double cambioAltid = location.getAltitude() - posicionAnterior.getAltitude();
					if ( cambioAltid > 0  ) {
						altitudAcumPos += cambioAltid; 
					} else {
						altitudAcumNeg += -1 * cambioAltid; 
					}
				}
				
				
			}
			posicionAnterior = location;
			
			// ahora almaceno los datos en la BBDD
			ContentValues valores = new ContentValues();
			valores.put(EstructuraDB.Punto.COLUMN_NAME_SECUENCIA, numPuntos);
			valores.put(EstructuraDB.Punto.COLUMN_NAME_LAT,location.getLatitude() );
			valores.put(EstructuraDB.Punto.COLUMN_NAME_LONG,location.getLongitude() );
			valores.put(EstructuraDB.Punto.COLUMN_NAME_DISTANCIA, distanciaTotal );
			valores.put(EstructuraDB.Punto.COLUMN_NAME_SESION, sesionId );
			if ( location.hasSpeed() ) {
				valores.put(EstructuraDB.Punto.COLUMN_NAME_VELOCIDAD, location.getSpeed() );
			}
			if ( location.hasAltitude() ) {
				valores.put(EstructuraDB.Punto.COLUMN_NAME_ALTITUD, location.getAltitude() );
			}
			if ( location.hasAccuracy() ) {
				valores.put(EstructuraDB.Punto.COLUMN_NAME_PRECISION, location.getAccuracy() );
			}
			
			valores.put(EstructuraDB.Punto.COLUMN_NAME_TIEMPOTRANS, tiempoTranscurrido );
			registroDB.insertaPunto(valores);
			
			
			//actualiza los displais en pantalla
			if (UImanager != null)
	 			UImanager.updateLocUI(location, distanciaTotal, altitudAcumPos, altitudAcumNeg);
			
			}
	
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
			if (status != LocationProvider.AVAILABLE) {
				estado_gps = true;
			} else {
				estado_gps = false;
			}
			
		}
	
		public void onProviderEnabled(String provider) {}
	
		public void onProviderDisabled(String provider) {}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
		}
	
	/**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
    	LocationRegisterService getService() {
            return LocationRegisterService.this;
        }
    }
    
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();


	
    @Override
    public void onCreate() {
        
    	//inicializo a desactivado por si acaso
    	autopause = -1;
    	estado_autopause = false;
    	
    	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
        
        //abro la BBDD
        registroDB = new AlmacenDatos( (Context) this.getApplication() ); 
        
        //cojo referencia al servicio de posicionameinto
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        //cojo el Handler para el reloj
        mHandler = new Handler();
        
        // inicializo la sesion
        initSesion();
        
        //preparao lo del wakelock para que no de duerma el telefono
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "correcaminosRegistra");

        
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);
    }

    
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
        
    }
	
	// pone icono y todo ese en la barra de notificacion para que se
	// vea que el servicio esta corriendo
    private void showNotification() {
    		
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.CCServiceLocationListener);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.cclocationlistenernotification, text,
                System.currentTimeMillis());
     
        
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Registra.class), PendingIntent.FLAG_UPDATE_CURRENT );

        notification.contentIntent = contentIntent;

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.CCServiceLocationListenerLabel),
                       text, contentIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }



}




