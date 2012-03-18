package es.fenoll.javier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import es.fenoll.javier.AlmacenDatos.PuntoGPX;

public class PuntosSesion extends Activity implements OnClickListener {

	private AlmacenDatos registroDB;
	private TableLayout tablaPuntos, tablaPuntosCabecera;
	
	private String loadedSesionDesc;
	private long sesionId;
	private GestureDetector gestureDetector;
	private MyGestureDetector gestureListener;
	
	private MapView map;
	private CorrepicosItemizedIconOverlay<OverlayItem> mPointsOverlay;
	private Paint estiloLinea, estiloLineaResaltado, estiloLineaPocoResaltado;
	
	// como cargo todo el cabecera pues ya preparo el nombre y asi no tengo que releer el cursor
	private String nomFicheroExport;
	private ProgressDialog dialog;
	
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
	 		
	@Override
	protected void onPause() {
		
		super.onPause();
		
		
		// si ha cambiado la descripcion lo salvo
		
		TextView tv = (TextView) findViewById(R.id.descPuntosSesion) ; 
		
		if ( tv.getText().toString().compareTo(loadedSesionDesc) != 0) {
			
			if ( !(loadedSesionDesc.length() == 0 && tv.getText().toString().length() == 0)  ) {
				salvaSesion();
			}
		}
		
	}


		 
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.puntossesion); 
        
        // engancho para detectar el swipe
     	gestureListener = new MyGestureDetector();
     	gestureDetector = new GestureDetector(gestureListener);
     	
     	//engancho los botones
     	Button button = (Button)findViewById(R.id.anteriorKm);
        button.setOnClickListener(this);
        button = (Button)findViewById(R.id.siguienteKm);
        button.setOnClickListener(this);
         
        button = (Button)findViewById(R.id.cierra);
        button.setOnClickListener(this);
        ImageButton imgbutton = (ImageButton)findViewById(R.id.swipenext);
        imgbutton.setOnClickListener(this);

       //añado el detector de swipe a todos lo controles que necesito para que funciona bien
      	View swipeViews[] = {null,null,null,null};
      	
        swipeViews[0] = findViewById(R.id.tablaPuntosScroll);
      	swipeViews[1] = findViewById(R.id.mapaSesion);
      	
      	
      	for (int i=0;i<2;i++){
      		
      		swipeViews[i].setOnTouchListener(new View.OnTouchListener() {

          		@Override
          		public boolean onTouch(View v, MotionEvent event) {
          		if (gestureDetector.onTouchEvent(event))
          			return true;
          		else
          			return false;
          		}
          		});
      	}
         
		// recupero el id que llega desde quien lo llama
		Bundle extras = getIntent().getExtras();
		if(extras !=null) {
			sesionId = extras.getLong("sesionId");
		}


	     // creo la referencia a la BBDD
	     registroDB = new AlmacenDatos( (Context) this.getApplication() );        
	       
		//CARGO LA TABLA DE PUNTOS
		
		// cargo algunos controles para luego referenciarlos mas rápido
        tablaPuntos = (TableLayout)findViewById(R.id.tablaPuntosSesion);
        tablaPuntosCabecera =  (TableLayout)findViewById(R.id.tablaPuntosSesionCabecera);
        
        
        // pongo el textode la cabecera
        cargaCebecera();
           
        
        //preparo el mapa
        preparaMapa();
        
        XmlPullParser parserPuntos =  registroDB.PreparaRecuperaPuntosSesionGpx(sesionId);
        
        //Al principio cargo por Km
        //cargaTodosLosPuntos(cPuntosSesion);
        
      //saco el dialogo
    	dialog = new ProgressDialog(this);
    	dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	dialog.setMessage( getText(R.string.cargando).toString() );
    	dialog.setCancelable(false);
    	dialog.show();
    	
    	
        CargaPuntosThread cargaParcialThread = new CargaPuntosThread(parserPuntos,1000);
        cargaParcialThread.start();     
        
    }
    
    @Override
    // engancho el detector del swipe
    public boolean onTouchEvent(MotionEvent event) {
    	if (gestureDetector.onTouchEvent(event))
    		return true;
    	else
    		return false;
    }
    

	
    
    
	//CARGA DATOS GENERALES DE LA SESION
	// TODO: este metodo es casi iguakl que el de ListaSesionesAdapter, mejorar con el rediseño de clases
	private void cargaCebecera(){
		
		//obtengo la sesion que necesito
		// TODO: esto tampoco es muy optimo, pero como este método igual cambia....
		Cursor cSesiones  = registroDB.recuperaSesiones(sesionId);
        startManagingCursor(cSesiones);
        
        if (cSesiones.getCount() != 1) {    
			return ;          
			}    
		
        cSesiones.moveToFirst();
        
		// Ponemos la fecha      
				TextView tvdiasem = (TextView) findViewById(R.id.puntoSesionDiaSem);
				TextView tvdianum = (TextView) findViewById(R.id.puntoSesionDiaNum);
				TextView tvdiames = (TextView) findViewById(R.id.puntoSesionDiaMes);
				
				Date lafecha;
				String fechaFormateadaDiaSem = "--error--";
				String fechaFormateadaDiaNum = "--error--";
				String fechaFormateadaDiaMes = "--error--";
				
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				try {
					//lafecha = DateFormat.getDateTimeInstance().parse(cSesiones.getString( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_FECHA)  ))  ;
					lafecha = df.parse(cSesiones.getString( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_FECHA)  ))  ;
					fechaFormateadaDiaSem = fechaFormateadaDiaNum = fechaFormateadaDiaMes = DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL).format (lafecha);
								
					df = new SimpleDateFormat("E");
					fechaFormateadaDiaSem = df.format(lafecha);
					df = new SimpleDateFormat("d");
					fechaFormateadaDiaNum = df.format(lafecha);
					df = new SimpleDateFormat("MMM");
					fechaFormateadaDiaMes = df.format(lafecha);
					df = new SimpleDateFormat("yyyy-M-dd_HH_mm");
					nomFicheroExport = df.format(lafecha);
														
					} catch (ParseException e) {
						e.printStackTrace();
									} 
				tvdiasem.setText( fechaFormateadaDiaSem );
				tvdianum.setText( fechaFormateadaDiaNum );
				tvdiames.setText( fechaFormateadaDiaMes );
				
				// obtenemos distancia y tiempo
				long distanciaTotal = cSesiones.getLong( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_DISTANCIA)  ) ;
				long tiempoTotal = cSesiones.getLong( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_DURACION)  );
				long desnivelAcumPos = cSesiones.getLong( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_ALTITUD_POS)  );
				long desnivelAcumNeg = cSesiones.getLong( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_ALTITUD_NEG)  );
				
				//termino el nombre del fichero
				nomFicheroExport += "_" + distanciaTotal + "m";
				
				//pongo la distancia en Km y un decimal
				TextView tv = (TextView) findViewById(R.id.puntoSesionDistancia);   
				NumberFormat formatter = new DecimalFormat("#.##");
				tv.setText(  formatter.format( (double) distanciaTotal/1000 ) );
					  
				// pongo el tiempo en h:mm
				tv = (TextView) findViewById(R.id.puntoSesionDuracion); 
				FormatosDisplay cambioFormatos = new FormatosDisplay();
				String textoFinal = cambioFormatos.desdeMStoHHMM( tiempoTotal );
				tv.setText( textoFinal  );
				
				//pongo al velocidad
				tv = (TextView) findViewById(R.id.puntoSesionVelocidad); 
				if ( distanciaTotal != 0 && tiempoTotal != 0 ) {	
					// calculo la velocidad media y la pongo en MxKm
					// recordar que la distancia llega en m y el tiempo en ms
					tv.setText(  cambioFormatos.desdeMsaMKM( (double) distanciaTotal/(tiempoTotal/1000) ) );
				}
				else {
					tv.setText("--");
				}
				
				//pongo el desnivel
				tv = (TextView) findViewById(R.id.puntoSesionDesnivelPos);   
				tv.setText(  "+" + desnivelAcumPos );
				tv = (TextView) findViewById(R.id.puntoSesionDesnivelNeg);   
				tv.setText(  "-" + desnivelAcumNeg );
				
				//pongo al descripcion
				tv = (TextView) findViewById(R.id.descPuntosSesion); 
				loadedSesionDesc = cSesiones.getString( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_DESC)  ) ;
				if (loadedSesionDesc != null)
					tv.setText( loadedSesionDesc );
				else
					loadedSesionDesc = "";
					
		
	}
	
	
	//CONTROLA EL MAPA
	
	private void preparaMapa() {
		

        //INICIALIZO EL MAPA
                
        // y creo el mapa y me centro en el
        map = (MapView)findViewById(R.id.mapaSesion);
        
        /*
        // esto hace que coja los tiles de google maps, quitar y coje los de OSM 
        // que es el de por defecto, implemantar otras cosas a ver que tal IGN?
        // Bases URL para google
        // 
        // http://mt3.google.com/vt/v=w2.97 para el mapa
        // TODO: ver que hay que poner para sacar el satelite y otros
        map.setTileSource(
        		new OnlineTileSourceBase("Google Maps", ResourceProxy.string.unknown, 1, 20, 256, ".png", "http://mt3.google.com/vt/lyrs=h@145&hl=en") {
			        @Override
			        public String getTileURLString(final MapTile aTile) {
				        
				        // GOOGLE MAPS URL looks like
				        // base url const x y zoom
				        // http://mt3.google.com/vt/v=w2.97&x=74327&y=50500&z=17
				        //
				        return getBaseUrl() + "&x=" + aTile.getX() + "&y=" + aTile.getY() + "&z=" + aTile.getZoomLevel();
				    }
        		});

        		*/
        
        //con 14 me caba mas o menos un km bien en la pantalla
    	map.getController().setZoom(14);
        
        
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        
        
        // TODO: por lo menos para debug poner la resolucion de cada punto
           

        // creo el overlay de puntos con el array que he generado
        // el icono que quiero para los marker
        //Drawable iconoPunto = this.getResources().getDrawable(R.drawable.pospnt);
        //ItemizedOverlay<OverlayItem> mMyLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,iconoPunto,null,mResourceProxy);
       
	}
	
	private void highliteLineaMapa() {
		
		
		int colorResaltado = 0xFFFF0000;
		int colorPocoResaltado = 0xffce0808;
		
		estiloLineaResaltado = new Paint();
		estiloLineaResaltado.setColor(colorResaltado);
		estiloLineaResaltado.setStrokeWidth(5);
		estiloLineaResaltado.setShadowLayer(1, 1, 1, 0xFF222222);
		estiloLineaResaltado.setStyle(Paint.Style.STROKE); // esto para que lo dibuje como linea

		estiloLineaPocoResaltado = new Paint();
		estiloLineaPocoResaltado.setColor(colorPocoResaltado);
		estiloLineaPocoResaltado.setStrokeWidth(3);
		estiloLineaPocoResaltado.setShadowLayer(1, 1, 1, 0xFF222222);
		estiloLineaPocoResaltado.setStyle(Paint.Style.STROKE); // esto para que lo dibuje como linea

		
		
        //recorro la lista de overlays y el primer path le cambio el color
        List<Overlay> listaOverlays = map.getOverlays();
        
        int i= 0;
        for ( Iterator<Overlay> iterOverlay = listaOverlays.iterator(); iterOverlay.hasNext() ;i++ ) {
            
        	Overlay cadaOverlay = iterOverlay.next();
        	
        	if ( cadaOverlay instanceof  PathOverlay) {
        		// lso de antes del punto selec los pongo resaltados
        		if ( i < mPointsOverlay.getFocusedItemIndex()  ) {
        			if ( i+1 == mPointsOverlay.getFocusedItemIndex() ) {
            			((PathOverlay)cadaOverlay).setPaint(estiloLineaResaltado);
            		}
        			else {
        				((PathOverlay)cadaOverlay).setPaint(estiloLineaPocoResaltado);
        			}
        		}
        		else {
        			((PathOverlay)cadaOverlay).setPaint(estiloLinea);
        		}
        		
        		
        	}
        	
        	        	
        }
        
        
	
	}
	
	private void addLineaMapa(PathOverlay linea, long AltitudAcumulada , long AltitudAcumuladaBajada  ) {
		
		int color = 0xff0b06c3;
		
		//TODO: mejorar el color del path segun la subida/bajada que tenga
		
		color = (int) (AltitudAcumulada - AltitudAcumuladaBajada);
		
		estiloLinea = new Paint();
        estiloLinea.setColor(color);
        estiloLinea.setStrokeWidth(3);
        estiloLinea.setShadowLayer(1, 1, 1, 0xFF222222);
        estiloLinea.setStyle(Paint.Style.STROKE); // esto para que lo dibuje como linea

        linea.setPaint(estiloLinea);
		
		map.getOverlays().add(linea);
	}
	
	private void addPuntosMapa( List<OverlayItem> items) {
		
        // este overlay son unas cajas de texto
        mPointsOverlay = new CorrepicosItemizedIconOverlay<OverlayItem>(this, items, 
                        new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                                @Override
                                public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                                        /*Toast.makeText(
                                        		PuntosSesion.this,
                                                        "Item '" + item.mTitle + "' (index=" + index
                                                                        + ") got single tapped up", Toast.LENGTH_LONG).show();
                                        */
                                        return true;
                                }

                                @Override
                                public boolean onItemLongPress(final int index, final OverlayItem item) {
                                        /*
                                		Toast.makeText(
                                        		PuntosSesion.this,
                                                        "Item '" + item.mTitle + "' (index=" + index
                                                                        + ") got long pressed", Toast.LENGTH_LONG).show();
                                        */
                                        return false;
                                }
                        });
        
        
        // añado los dos overlays, el de lineas y el de puntos al mapa
        
        map.getOverlays().add(mPointsOverlay);
       
        
	}
	
	
	
	private PathOverlay creaUnPath(){
		

        // para la linea creo el overlay del color que sea (el numero) y luego le voy dando los puntos
        PathOverlay lineaCarrera = new PathOverlay(Color.RED,this);

        return lineaCarrera;
        
	}
	
	
	
	// CONTROLA LA TABLA DE PUNTOS se lanza como un thread por q tarda un poco
	
	 private class CargaPuntosThread extends Thread {

	        private XmlPullParser m_parserPuntos;
	        private long m_cortesParcial;
	        
	        public CargaPuntosThread(XmlPullParser parserPuntos, long cortesParcial) {
	            this.m_parserPuntos = parserPuntos;
	            this.m_cortesParcial = cortesParcial;
	        }

	        @Override
	        public void run() {         
	        	cargaCadaParcial( m_parserPuntos, m_cortesParcial );
	            handler.sendEmptyMessage(0);
	        }

	        private Handler handler = new Handler() {

	            @Override
	            public void handleMessage(Message msg) {
	                
	            	dialog.dismiss();
	            	
	            }
	        };
	
	 }
    
    
    private void addValoresAltitud(List<String> valores, long subida, long bajada) {
    	
    	String texto = "";
    	String imagen = "";
    	
    	if ( subida > -1 * bajada) {
    		
    		texto = String.valueOf(subida);
    		
    		if (subida > 150) {
    			imagen = "subida_fuerte";
    		}
    		else if (subida > -1* bajada + 30 ) {
    			imagen = "subida_media";
    		}
    		else {
    			imagen = "subida_llano";
    		}
    		
    		
    	}
    	else {
    		texto = String.valueOf(bajada);
    		
    		if (-1 * bajada > 150) {
    			imagen = "bajada_fuerte";
    		}
    		else if (-1 * bajada > subida + 30 ) {
    			imagen = "bajada_media";
    		}
    		else {
    			imagen = "bajada_llano";
    		}
    	}
    	
    	
    	valores.add(texto);
    	valores.add(imagen);
    	
    }
	
    
    /* cargaTodosLosPuntos
    // esto realmente no tiene sentido, b´´asicamente solo vale para depurar 
    // y de hecho con muchos puntos la tabla durectamente no puede con ello
    private void cargaTodosLosPuntos(Cursor c) {

    	
        //long AltitudAcumulada  = 0;
        //long AltitudAnterior = 0;
        long precision =0;
        //long AltitudActual;
        FormatosDisplay cambioFormatos = new FormatosDisplay();
        
      //borro si hay algo en las tablas
        tablaPuntosCabecera.removeAllViews();
        tablaPuntos.removeAllViews();
        
        //pongo las cabeceras
        List<String> valores = new ArrayList<String>();
        valores.add("orden");
        valores.add("tiempo");
        valores.add("distancia");
        //valores.add("Km/h");
        //valores.add("mmKm");
        //valores.add("desnivelAcum");
        valores.add("precision");
        rellenaCabecer(valores);
   
        if (c.getCount() > 0) {
        	c.moveToFirst();
	        
	        do {
	        	
	        	valores.clear();
	        	
	        	//orden
	        	String valor  = String.valueOf( c.getInt( c.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_SECUENCIA))  );
	        	valores.add(valor);
	        	
	        	//tiempo
	        	valor  = cambioFormatos.desdeMStoHHMM( c.getLong( c.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_TIEMPOTRANS))  );
	        	valor = valor  + cambioFormatos.desdeMSobtenSS( c.getLong( c.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_TIEMPOTRANS))  );
	        	valores.add(valor);
	        	
	        	//distancia
	        	NumberFormat formatter = new DecimalFormat("#.#");
	        	valor  = formatter.format( c.getLong( c.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_DISTANCIA))  );
	        	valores.add(valor);
	        	
	        	//velocidades
	        	valor  = cambioFormatos.desdeMsaKh( c.getLong( c.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_VELOCIDAD))  );
	        	valores.add(valor);
	        	valor  = cambioFormatos.desdeMsaMKM( c.getLong( c.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_VELOCIDAD))  );
	        	valores.add(valor);
	        	
	        	// calculo la altitud acumulada
	        	AltitudActual = c.getLong( c.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_ALTITUD)) ;
	        	// si ya tengo una altitud anterior, y he subido, sumo
	        	if (AltitudAnterior < AltitudActual && AltitudAnterior != 0){
	        		AltitudAcumulada += AltitudActual - AltitudAnterior;
	        	}
	        	AltitudAnterior = AltitudActual ;
	        	valores.add(String.valueOf( AltitudAcumulada ));
				
	        	//precision
	        	precision = c.getLong( c.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_PRECISION));
	        	valores.add(String.valueOf( precision ));
	        	
	        	rellenaPuntos(valores);
	        } while (c.moveToNext());
        }

    	
    }
    */
    

    // todo lo pongo como final para poder correlo en la UI thread
    private void creaFilaPuntos(List<String> valores, TableLayout tabla, boolean cabecera, boolean oculta) {
    	
    	//Create a new row to be added.
    	//TableRow tr = new TableRow(this);
    	
    	//Tenemos que lanzar esto en la thread de la UI si no da problemas
    	
    	
    	final LayoutInflater inflater = LayoutInflater.from(this);
    	
    	final boolean fcabecera = cabecera;
    	final String[] fvalores = {valores.get(0),valores.get(1),valores.get(2),valores.get(3)};
    	final TableLayout ftabla = tabla;
    	final boolean foculta = oculta;
    	
    	runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
            	TableRow tr ;
            	if (foculta) 
            		tr = (TableRow) inflater.inflate(R.layout.filatablapuntos_oculta, ftabla, false);
            	else
            		tr = (TableRow) inflater.inflate(R.layout.filatablapuntos, tablaPuntos, false);


            	TextView tv[] = {null,null,null};
            	tv[0] = (TextView) tr.findViewById(R.id.km);
            	tv[1] = (TextView) tr.findViewById(R.id.ritmo);
            	tv[2] = (TextView) tr.findViewById(R.id.altitud);
                
            	for(int i=0;i<3;i++) {
            		tv[i].setText(fvalores[i]);
                	if (fcabecera) {
                		tv[i].setTextSize(15);
                		tv[i].setBackgroundColor(0xFFfba824);
                	}
                	
            	}
            	
            	  	
        	    ImageView iv = (ImageView) tr.findViewById(R.id.altitud_img);
        	    // obtengo el id del recurso conociendo su nombre y tipo
        	    int id = getResources().getIdentifier(fvalores[3] , "drawable", getPackageName());
        	    iv.setImageResource(id);
        	    if (fcabecera) {
        	    	iv.setBackgroundColor(0xFFfba824);
        	    }
            		

            	//Add the new row to our tableLayout 
            	ftabla.addView(tr);

            	
            	
            	
            	
            }
        } );
    	
    	    	
    }
    
    

    // Parcial es cada cuantos metros agrupo
    private void cargaCadaParcial(XmlPullParser parserPuntos, long cortesParcial){

        long AltitudAcumulada  = 0;
        long AltitudAcumuladaBajada = 0;
        long AltitudAnterior = 0;
        long AltitudActual;
        
        
        long distAcmulada = 0;
        long distParcial = 0;
        long distActual = 0;
        
        long tiempoAcmulado = 0;
        long tiempoActual = 0;
        
        double velocidadTramo = 0;
        
        FormatosDisplay cambioFormatos = new FormatosDisplay();
        NumberFormat formatter = new DecimalFormat("#.##");
        
        //borro si hay algo en las tablas
        tablaPuntosCabecera.removeAllViews();
        tablaPuntos.removeAllViews();
        
        
        //pongo las cabeceras, en la tabla normal oculta
        List<String> valores = new ArrayList<String>();
        valores.add(  getText(R.string.displayDistanciaLbl).toString() );
        valores.add(  getText(R.string.displayVelocidadLblMms).toString() );
        valores.add( getText(R.string.altAcumLbl).toString() );
        valores.add("subida_vacio");
        creaFilaPuntos(valores,tablaPuntosCabecera,true, false);
        creaFilaPuntos(valores,tablaPuntos,true, true);
       
        valores.clear();
        valores.add("0,00");
		valores.add("10:10");
		valores.add("-100");
		valores.add("subida_vacio");
		creaFilaPuntos(valores,tablaPuntosCabecera,true, true);
		valores.clear();
        
		if (parserPuntos == null) {
			return;
		}
		
		PuntoGPX elPuntoGPX =  registroDB.RecuperaPuntoSesionGpx ( parserPuntos);
		
        if (elPuntoGPX != null) {
        	        	
        	long tramo  = 1;
        	
        	//creo el path 
        	GeoPoint elpunto;
        	PathOverlay lineaCarrera = creaUnPath();
        	//para los puntos, creo el array de puntos y luego se los paso al overlay de puntos todos del tiron
            ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
            
	        
	        do {
	        	
	        	//añado el punto a path
	        	elpunto = elPuntoGPX.posicion;
	            
	        	lineaCarrera.addPoint(elpunto);
	            
	            if ( elPuntoGPX.index == 1) {
	            	//añado el punto
	            	items.add(new OverlayItem("Inicio"  , "", elpunto));
	            	// y centro el mapa en ese punto
	            	map.getController().setCenter( elpunto );
	            }
	            
	        	// es la distancia desde el principio
	        	distActual =  elPuntoGPX.distancia;
	        	// sumo la distancia del tramo a la parcial, hago esto por que en la tabla me vallegando los acumulados
	        	distParcial = distActual - distAcmulada;
	        	
        		// calculo la altitud acumulada
	        	AltitudActual = elPuntoGPX.altitud;
	        	// si ya tengo una altitud anterior, y he subido, sumo
	        	if (AltitudAnterior < AltitudActual && AltitudAnterior != 0){
	        		AltitudAcumulada += AltitudActual - AltitudAnterior;
	        	}
	        	// tambien lo que bajo en el tramo
	        	if (AltitudAnterior > AltitudActual && AltitudAnterior != 0){
	        		AltitudAcumuladaBajada += AltitudActual - AltitudAnterior;
	        	}
	        	AltitudAnterior = AltitudActual ;
	        	
	        	tiempoActual = elPuntoGPX.tiempo;
	        	
	        	// Si ya llevo la cantidadque me dice el parametro
	        	if (  distActual >= tramo * cortesParcial ) {
	        		
	        		// acumulo distancia 
	        		distAcmulada = distActual;
	        		//valores.add(formatter.format(distAcmulada));
	        		valores.add(String.valueOf(tramo));
	        		
	        		// calculo el tiempo desde el último acumulado y acumulo
	        		// no tengo que acumular en cada tramo por que ya viene así en la tabla 
	        		//valores.add( cambioFormatos.desdeMStoHHMM( tiempoActual - tiempoAcmulado ) + cambioFormatos.desdeMSobtenSS(tiempoActual - tiempoAcmulado) );
	        		
	        		// calculo las velocidades medias con la distancia y tiempo transcurrido
	        		// en m/s
	        		velocidadTramo = (double) distParcial/( (tiempoActual - tiempoAcmulado)/1000 );
	        		//valores.add(cambioFormatos.desdeMsaKh(velocidadTramo) );
	        		valores.add(cambioFormatos.desdeMsaMKM(velocidadTramo) );
	        		
	        		// el tiempo lo acumulo despues de calcular velocidad
	        		tiempoAcmulado = tiempoActual;
	        		
	        		// pngo al altitud acumulada y lo vuelvo a dejar a cero para el siguiente acumulado
		        	addValoresAltitud(valores,AltitudAcumulada,AltitudAcumuladaBajada);
		        	creaFilaPuntos(valores,tablaPuntos,false, false);
	        		valores.clear();
	        		
	        		//cierro el tramo y lo añado al mapa
	        		addLineaMapa(lineaCarrera,AltitudAcumulada,AltitudAcumuladaBajada);
	        		lineaCarrera = creaUnPath();  //creo el siguiente
	        		
	        		//añado el punto
	        		items.add(new OverlayItem("Km: " + tramo, cambioFormatos.desdeMsaMKM(velocidadTramo), elpunto));
	        		
		        	AltitudAcumulada = 0;
		        	AltitudAcumuladaBajada = 0;
	        		
	        		tramo++;
	        	}
	        
	        	elPuntoGPX =  registroDB.RecuperaPuntoSesionGpx ( parserPuntos) ;
	        	
	        } while ( elPuntoGPX != null   );
	        
	        
	        
	        // al final añado lo poco que me queda que no llega al intervalo fijado
	        valores.add(formatter.format( (double)(distParcial)/1000 ));
	        //valores.add( cambioFormatos.desdeMStoHHMM( tiempoActual - tiempoAcmulado ) + cambioFormatos.desdeMSobtenSS(tiempoActual - tiempoAcmulado) );
    		velocidadTramo = (double) distParcial/( (tiempoActual - tiempoAcmulado)/1000 );
    		//valores.add(cambioFormatos.desdeMsaKh(velocidadTramo) );
    		valores.add(cambioFormatos.desdeMsaMKM(velocidadTramo) );
    		addValoresAltitud(valores,AltitudAcumulada,AltitudAcumuladaBajada);
        	
        	
    		creaFilaPuntos(valores,tablaPuntos,false, false);
    		valores.clear();
    		
    		//pongo elúltimo tramo
    		addLineaMapa(lineaCarrera,AltitudAcumulada,AltitudAcumuladaBajada);
    		//añado el punto
    		items.add(new OverlayItem("FIN", cambioFormatos.desdeMsaMKM(velocidadTramo), elpunto));
    		
    		
    		// y añado los puntos al mapa
    		addPuntosMapa(items);
        }
        

    }  // termina cargaCadaParcial

   

    
    // Para el menu de la activity
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.puntossesionmenu, menu);
        return true;
    }
    
    private void salvaSesion() {
	    TextView tv = (TextView) findViewById(R.id.descPuntosSesion); 
		
		registroDB.actualizaDescSesion( sesionId, tv.getText().toString() );
		
		// almaceno en la variable lo ya guardado para que on pause no quiera guardar otra vez
		loadedSesionDesc =  tv.getText().toString();
	
		findViewById(R.id.main).requestFocus();
		
		String text =  getText(R.string.SesionSalvada).toString();
		Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
    	toast.show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.salvaSesionKML:
            
        	//saco el dialogo
        	dialog = new ProgressDialog(this);
        	dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        	dialog.setMessage( getText(R.string.exportando).toString() );
        	dialog.setCancelable(false);
        	dialog.setProgress(0);
        	dialog.show();
        	
        	
        	// TODO_meter en export en otra thread y enseñar el dialogo
        	String nomficheroExportKml = nomFicheroExport + ".kml";
        	
            ExportThread exportThread = new ExportThread(nomficheroExportKml);
            exportThread.start();

        	        	
            return true;
            
        case R.id.salvaSesion:
        	
        	salvaSesion();
        	
        	return true;
            
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private class ExportThread extends Thread {

        private String nomfichero;
        private boolean resultado;

        public ExportThread(String nomfichero) {
            this.nomfichero = nomfichero;
        }

        @Override
        public void run() {         
        	salvaKML( nomfichero);
            handler.sendEmptyMessage(0);
        }

        private Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                
            	String text = "";
            	// TODO_meter en export en otra thread y enseñar el dialogo
            	String nomficheroExportKml = nomFicheroExport + ".kml";
            	
            	
            	if ( resultado ) {
            		text = getText(R.string.archexportadotxt).toString() + nomficheroExportKml;
            	} else {
            		text = getText(R.string.errorarchexportadotxt).toString() + getText(R.string.archexportadotxt).toString();
            	}
            	
            	dialog.dismiss();
            	Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            	toast.show();
            }
        };
        
        private void salvaKML(String nomfichero) {
            
        	//compruebo si la SD esta bien
        	String state = Environment.getExternalStorageState();
        	if (! Environment.MEDIA_MOUNTED.equals(state)) {
        	    // MEDIA_MOUNTED means we can read and write the media
        		resultado = false;
        		return;
        	} 
        	
        	//cojo handler al fichero
        	/*String pathToExternalStorage = Environment.getExternalStorageDirectory().toString();
        	File appDirectory = new File(pathToExternalStorage + "/" + "AppName");   
        	// have the object build the directory structure, if needed.
        	appDirectory.mkdirs();
    */
        	
        	
        	File path = new File(Environment.getExternalStorageDirectory()+ File.separator  + "correpicos");
        	File file = new File(path, nomfichero);

        	//recorro todos los puntos y los meto en un string
        	// recorro y escribo los puntos
        	String coords = "";

            XmlPullParser parserPuntos =  registroDB.PreparaRecuperaPuntosSesionGpx(sesionId);
            
        	
            //float total = (float)100 / cPuntosSesion.getCount();

			PuntoGPX elPuntoGPX =  registroDB.RecuperaPuntoSesionGpx ( parserPuntos);
			
            if (elPuntoGPX.puntosSesion > 0)
            	dialog.setMax( elPuntoGPX.puntosSesion.intValue() );
            
            try {
            	
            	path.mkdirs();
    	    	FileWriter gpxwriter = new FileWriter(file);
    	        BufferedWriter out = new BufferedWriter(gpxwriter);
    	        
    	        //escribo las cabeceras del kml
    	        
    	        out.write("<?xml version='1.0' encoding='UTF-8'?>\n");
    	        out.write("<kml xmlns='http://www.opengis.net/kml/2.2' xmlns:gx='http://www.google.com/kml/ext/2.2' xmlns:kml='http://www.opengis.net/kml/2.2' xmlns:atom='http://www.w3.org/2005/Atom'>\n");
    	        out.write("<Document>\n");
    	        out.write("\t <name>ruta de ejemplo</name>\n");
    	        
    	        
    	        out.write("\t <Placemark>\n");
    	        out.write("\t\t <name>Ruta sin tÃ­tulo</name>\n");
    			
    	        out.write("\t\t <LineString>\n");
    	        out.write("\t\t\t <tessellate>1</tessellate>\n");
    	        out.write("\t\t\t <coordinates>\n");
            
    	        out.write("\t\t\t\t ");
    	        
	        	do {
	        		
	        		dialog.setProgress( elPuntoGPX.index.intValue() );
	        		     		
	        		coords = Double.toString( elPuntoGPX.posicion.getLongitudeE6()/1E6 );
	        		coords = coords + "," + Double.toString( elPuntoGPX.posicion.getLatitudeE6()/1E6 );
	        		coords = coords + "," +  Double.toString( elPuntoGPX.altitud );
	        		coords = coords + " "	;
	        		
	        		out.write( coords );
	        		
		        	elPuntoGPX =  registroDB.RecuperaPuntoSesionGpx ( parserPuntos) ;
		        	
	             } while ( elPuntoGPX != null );
	             	
	        	out.write("\n");
	        	
    			//		-3.908026479611976,40.61936190368323,0 -3.908449529672289,40.61930266352579,0 -3.90853864355761,40.61946631188167,0 -3.907507299478171,40.61986960863751,0 -3.907408342236161,40.61952847272852,0 -3.907940205371154,40.6193797270107,0 
    			out.write("\t\t\t </coordinates>\n");
    			out.write("\t\t </LineString>\n");
    			out.write("\t </Placemark>\n");
    			out.write("</Document>\n");
    			out.write("</kml>");
    	        
    	        out.close();
        	
        	}catch (IOException e) {
        	    Log.e("salvaKML", "Could not write file " + e.getMessage());
        	    resultado = false;
        	    return;
        	}
            
        	resultado = true;
        	return;
        
        }
    }

    
    
    

	@Override
	public void onClick(View v) {
		if ( v.getId() == R.id.anteriorKm ) {
		
			mPointsOverlay.setFocusPrev();
			highliteLineaMapa();
			map.getController().setCenter( mPointsOverlay.getFocusedGeoPoint()  );
			
		}
		else if ( v.getId() == R.id.siguienteKm ) {
			
			mPointsOverlay.setFocusNext();
			highliteLineaMapa();
			map.getController().setCenter( mPointsOverlay.getFocusedGeoPoint() );
		}
		else if( v.getId() == R.id.cierra) {
			finish();	
		}
		else if( v.getId() == R.id.swipenext) {
			
			gestureListener.Mueve(true);
			
		}
		
	}
    
}
