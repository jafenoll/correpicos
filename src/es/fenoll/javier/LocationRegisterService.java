package es.fenoll.javier;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

import es.fenoll.javier.AlmacenDatos.PuntoGPX;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.MediaPlayer;
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
		
		//para lso intervalos
		private ArrayList<Intervalo> intervalos;
		private int enIntervalo;
		private long tiempoComienzoIntervalo;
		private double distanciaComienzoIntervalo;
		private String flagGuardaIntervalo;
		
		private  Context context;

		
		//sesion
		private long sesionId;
		
		//guardo el enlace a la activity que se encargara de poner los valores en la UI
		private Registra UImanager;
		
		
		//variables para el tema del reloj
		private final int REFRESH_RATE = 1000;
		private Handler mHandler;
		private long tiempoTranscurrido;
		private long tiempoActual;
		// en ms cuando empece a registar
		private long tiempoInicial;
		
		//para notificar que el servicio esta ejecutandose
		private NotificationManager mNM;
		// Unique Identification Number for the Notification.
	    // We use it on Notification start, and to cancel it.
	    private int NOTIFICATION = R.string.CCServiceLocationListener;
		
	  //para que no se duerma el telefono
		private WakeLock wl;
		
		private void PlaySound(int resourceID) {
			
			
			
			MediaPlayer intervalosMediaPlayer = MediaPlayer.create( this, resourceID );
			
			intervalosMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
	            @Override
	            public void onCompletion(MediaPlayer mp) {
	                mp.stop();
	                mp.release();
	            }
	        });
			
			intervalosMediaPlayer.start(); // no need to call prepare(); create() does that for you
			
			
		}
		
		//para gestionar el cronometro
	 	private Runnable startTimer = new Runnable() {
	 	   public void run() {
	 		    
	 		   tiempoActual = System.currentTimeMillis();
	 		   tiempoTranscurrido = tiempoActual - tiempoInicial;
	 		   
	 		   // si he comenzado con los intervalos
	 		   if (enIntervalo >= 0) {
	 			   
	 			  //y  este intervalo esta fijado por el tiempo 
	 			  if (intervalos.get(enIntervalo).unidad.equals("ms") ) {
	 				  
	 				  
	 				  //comprueba si ha pasado el tiempo del intervalo desde que se iniciaron lso intervalos
	 				  // si es asi pasa al siguienet tramo y pone el flag de almacenar
	 				 
	 				 Long tiempoIntervalo = tiempoTranscurrido - tiempoComienzoIntervalo;
	 				 // a esto le sumo 1 s por que como luego hay redondeos se pierden coas y yo quiero que cuando queden 700ms diga que queda 1s y no 0
	 				 Long tiempoRestaIntervalo = intervalos.get(enIntervalo).duracion - tiempoIntervalo+1000;
	 				
	 				 updateTimer(tiempoRestaIntervalo,intervalos.get(enIntervalo).etiqueta);
	 				 
	 				 // como solo se llama a esta funcion cada segundo solo suano 1 vez por segundo
	 				 // suena 3 segundos antes de acabar
	 				 
	 				 // por algun motivo tengo que comprobar siempre con valores 1 segundo mayores
	 				 
	 				 
	 				 // acaba de terminar este intervalo
	 				 if (tiempoRestaIntervalo <= 1000) {
	 					
	 					//pongo la cedena para guardar 
	 					flagGuardaIntervalo = intervalos.get(enIntervalo).etiqueta;
	 					//pongo a cero el tiempo 
	 					tiempoComienzoIntervalo = tiempoTranscurrido;
	 					//paso al siguiente intervalo o termino
	 					if (enIntervalo < intervalos.size()-1 ) {
	 						enIntervalo +=1;
	 						//TODO: hacer que este sonido sea diferente
	 						
	 					}
	 					else {
	 						enIntervalo = -1;
	 						//TODO: hacer que este sonido sea diferente
	 						
	 					}
	 						
	 					
	 					
	 				 }
	 				//TODO: Hacer que el num de segundos antes sea configurable
	 				 else if (tiempoRestaIntervalo <= 4000) {
	 					//preparo para hecer ruido
	 					PlaySound(R.raw.tick);
	 				 }
	 				  
	 			  }
	 		   }
	 		   else {

	 			   updateTimer(tiempoTranscurrido, null);
	 		   }
	 		   
	 		   
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
	 		   
	 		   mHandler.postDelayed(this,REFRESH_RATE);
	 		   
	 		   
	 		   
	 		}
	 	};
		
	 	private void updateTimer(long tiempo, String accion){
	 		if (UImanager != null)
	 			UImanager.updateTimerUI(tiempo,accion);
	 		
	 	}
	 	
	 	private void initSesion() {
	 		tracking = false;
	 		distanciaTotal = 0;
	 		tiempoTranscurrido = 0;
	 		sesionId = -1;
	 		tiempoInicial = 0;
	 		numPuntos = 0;
	 		
	 		//inicializo la parte de intervalos
	    	intervalos = null;
	    	enIntervalo = -1;
	    	flagGuardaIntervalo = null;
	 		
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
			registroDB.terminaSesion(sesionId);
						
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
		
		
		public void startIntervalos( ArrayList<Intervalo> lintervalos) {
			
			intervalos = lintervalos;
	    	// lo pongo en 0m para que el timer sepa que debe empezar
			enIntervalo = 0;
			
			tiempoComienzoIntervalo = tiempoTranscurrido;
			distanciaComienzoIntervalo = distanciaTotal;
			
			
			
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
			 
			long tiempoPausado = 0;
			
			//si llega un punto es que estoy activo
			estado_gps = true;
			
			// si no estoy registrando me salgo
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
				
				tiempoPausado = System.currentTimeMillis() - tiempoInicial - tiempoTranscurrido;
				
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
			
			PuntoGPX elPuntoGPX = registroDB.new PuntoGPX();
					
			
			elPuntoGPX.posicion = new GeoPoint( location.getLatitude()  , location.getLongitude() );
			elPuntoGPX.index = (long) numPuntos;
			elPuntoGPX.distancia = (long) distanciaTotal;
			elPuntoGPX.sesionId = sesionId;
			if ( location.hasSpeed() ) {
				elPuntoGPX.velocidad =  (long) location.getSpeed() ;
			}
			if ( location.hasAltitude() ) {
				elPuntoGPX.altitud =  (long) location.getAltitude() ;
			}
			if ( location.hasAccuracy() ) {
				elPuntoGPX.precisionH =  (long) location.getAccuracy() ;
			}
			elPuntoGPX.timestamp =  tiempoActual ;
			elPuntoGPX.tiempopausado =  tiempoPausado ;
			
			//si llegael flag de que ha terminado el intervalo guardo 
			if( flagGuardaIntervalo != null) {
				elPuntoGPX.intervalo =  flagGuardaIntervalo ;
				// lo dejo a null para no volver a guardar hasta el siguiente
				flagGuardaIntervalo = null;
			}
			
			
			registroDB.insertaPunto(elPuntoGPX);
			
			
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
        
    	context = this;
    	
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




