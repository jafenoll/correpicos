package es.fenoll.javier;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class ListaSesiones extends ListActivity implements OnClickListener  {

	private AlmacenDatos registroDB;
	private GestureDetector gestureDetector;
	private MyGestureDetector gestureListener;
	
	

	// para controlar lso gestos y en un swipe pasar a la otra pantalla
	 private class MyGestureDetector extends SimpleOnGestureListener {
	 		
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
	// engancho el detector del swipe
	public boolean onTouchEvent(MotionEvent event) {
	  	if (gestureDetector.onTouchEvent(event))
	   		return true;
	   	else
	   		return false;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        
		 ProgressDialog dialog = ProgressDialog.show(this, "", 
	                "", true);
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.listasesiones);
              
        // engancho para detectar el swipe
     	gestureListener = new MyGestureDetector();
     	gestureDetector = new GestureDetector(gestureListener);
     	
     	//añado el detector de swipe a todos lo controles que necesito para que funciona bien
     	View swipeViews[] = {null,null,null,null};
     	
     	swipeViews[0] = findViewById(android.R.id.list);
     	swipeViews[1] = findViewById(R.id.tablaAcumuladoSemanaScroll);
     	swipeViews[2] = findViewById(R.id.tablaAcumuladoMesScroll);
     	swipeViews[3] = findViewById(R.id.tablaAcumuladoAnoScroll);
     	
     	for (int i=0;i<4;i++){
     		
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
     	
     	
     	
        // me quedo al tanto de long press para menus
        registerForContextMenu(getListView());
        getListView().setOnCreateContextMenuListener(this); 
		

        Button button = (Button)findViewById(R.id.cierra);
        button.setOnClickListener(this);
        ImageButton imgbutton = (ImageButton)findViewById(R.id.swipenext);
        imgbutton.setOnClickListener(this);

     
     // creo la referencia a la BBDD
     	registroDB = new AlmacenDatos( (Context) this.getApplication() );        
       
     	rellenaEstadisticas();
     	
     	fillListData();
        
     	dialog.dismiss();
     	
        //registerForContextMenu(getListView());
    }	
	
		
	private void rellenaEstadisticas() {
		

		List<String> valores = new ArrayList<String>();
		
		//Las tablas para los acumulados
		TableLayout tablaPuntos[] = {null,null,null};
		TableLayout tablaPuntosCabecera[] = {null,null,null};
		
		// Pensar si poner o no cabeceras y temas de primera fila fija pero no se
		// alinean bien las columnas
		
		tablaPuntos[0] = (TableLayout)findViewById(R.id.tablaAcumuladoSemana);
		tablaPuntos[1] = (TableLayout)findViewById(R.id.tablaAcumuladoMes);
		tablaPuntos[2] = (TableLayout)findViewById(R.id.tablaAcumuladoAno);
		
		tablaPuntosCabecera[0] = (TableLayout)findViewById(R.id.tablaAcumuladoSemanaCab);
		tablaPuntosCabecera[1] = (TableLayout)findViewById(R.id.tablaAcumuladoMesCab);
		tablaPuntosCabecera[2] = (TableLayout)findViewById(R.id.tablaAcumuladoAnoCab);
		
		
		//añado las cabeceras a las tablas
		for (int i = 0; i<3; i++) {
			valores.clear();
			valores.add("");
			valores.add(  getText(R.string.numero).toString()  );
			valores.add(  getText(R.string.displayDistanciaLbl).toString()  );
			valores.add(  getText(R.string.displayTiempoLbl).toString()  );
			valores.add(  getText(R.string.altAcumLbl).toString()  );
			//creaFilaEstadisticas(valores,tablaPuntos[i], true);
			creaFilaEstadisticas(valores,tablaPuntosCabecera[i], true);		
			//creo la linea oculta en la tabla normal para que se alineen las celdas
			creaFilaEstadisticas(valores,tablaPuntos[i], true, true);
			
			// lo mismo para la tabla cabecera y normal con un tamaño maximo tipico de la tabla normal
			valores.clear();
			valores.add("01/2012");
			valores.add("10");
			valores.add("10,10");
			valores.add("10:10");
			valores.add("11111");
			creaFilaEstadisticas(valores,tablaPuntosCabecera[i], false, true);
			creaFilaEstadisticas(valores,tablaPuntos[i], false, true);
		}
		
		
		
		Cursor c  = registroDB.recuperaSesiones(-1);
        startManagingCursor(c);
        
        // el cursor devuelve de mas moderno a menos, por eso lo tengo que 
        // recorer al reves
        if (c.getCount() > 0) {
        	c.moveToFirst();
        	

        	int semanaNum= 0, mesNum = 0, anoNum = 0;
        	int semanaNumAnt = 0, mesNumAnt = 0, anoNumAnt = 0;
        	
        	int numSesionesAcum[] = {0,0,0};
        	long distanciAcum[] = {0,0,0} , distanciaSesion = -1, tiempoAcum[] = {0,0,0}, tiempoSesion = -1 , altitudAcum[] = {0,0,0}, altitudSesion= -1;
        	
        	
    		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    		Date fechaSesion;
    		
    		NumberFormat formatter = new DecimalFormat("#.##");
    		FormatosDisplay cambioFormatos = new FormatosDisplay();
    		  
    		
        	
        	do {
        		 
        		try {
        			// cojo la fecha
        			df = new SimpleDateFormat("yyyy-MM-dd");
           		 	fechaSesion = df.parse(c.getString( c.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_FECHA)  ))  ;
           		
           		 	//calculo la semana
           		 	df = new SimpleDateFormat("w");
     				semanaNum = Integer.parseInt(df.format(fechaSesion));           		 
           		 	//el mes
     				df = new SimpleDateFormat("M");
     				mesNum = Integer.parseInt(df.format(fechaSesion));           		 
           		 	// y el año
     				df = new SimpleDateFormat("y");
     				anoNum = Integer.parseInt(df.format(fechaSesion));
           		 
        		} catch (ParseException e) {
    				e.printStackTrace();
				} 
        		 
        		 
        		 //acumulo la distancia, el tiempo y el desnivel pos 
        		distanciaSesion = c.getLong( c.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_DISTANCIA)  ) ;
        		tiempoSesion = c.getLong( c.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_DURACION)  );
        		altitudSesion = c.getLong( c.getColumnIndex(EstructuraDB.Sesion.COLUMN_ALTITUD_POS)  );
        		 
        		// si cambio de semana escribo una linea en la tabla
        		if ( semanaNum != semanaNumAnt && semanaNumAnt != 0){
        			
        			valores.clear();
        			valores.add(semanaNumAnt + "/" + anoNum);
        			valores.add(numSesionesAcum[0] + "" );
        			valores.add(formatter.format( (double) distanciAcum[0]/1000 ));
        			valores.add(cambioFormatos.desdeMStoHHMM(tiempoAcum[0]));
        			valores.add( altitudAcum[0] + "" );
        			
        			creaFilaEstadisticas(valores,tablaPuntos[0], false);
        			
        			numSesionesAcum[0] = 0;
        			distanciAcum[0] = 0;
        			tiempoAcum[0] = 0;
        			altitudAcum[0] = 0;
        		}
        		// si cambio de semana escribo una linea en la tabla
        		if ( mesNum != mesNumAnt && mesNumAnt != 0){
        			     			
        			valores.clear();
        			valores.add(mesNumAnt + "/" + anoNum);
        			valores.add(numSesionesAcum[1] + "" );
        			valores.add(formatter.format( (double) distanciAcum[1]/1000 ));
        			valores.add(cambioFormatos.desdeMStoHHMM(tiempoAcum[1]));
        			valores.add( altitudAcum[1] + "" );
        			
        			creaFilaEstadisticas(valores,tablaPuntos[1], false);
        			
        			numSesionesAcum[1] = 0;
        			distanciAcum[1] = 0;
        			tiempoAcum[1] = 0;
        			altitudAcum[1] = 0;
        		}
        		// si cambio de semana escribo una linea en la tabla
        		if ( anoNum != anoNumAnt && anoNumAnt != 0){
        			
        			        			
        			valores.clear();
        			valores.add(anoNumAnt + "" );
        			valores.add(numSesionesAcum[2] + "" );
        			valores.add(formatter.format( (double) distanciAcum[2]/1000 ));
        			valores.add(cambioFormatos.desdeMStoHHMM(tiempoAcum[2]));
        			valores.add( altitudAcum[2] + "" );
        			
        			creaFilaEstadisticas(valores,tablaPuntos[2], false);
        			
        			numSesionesAcum[2] = 0;
        			distanciAcum[2] = 0;
        			tiempoAcum[2] = 0;
        			altitudAcum[2] = 0;
        		}
        		
        		semanaNumAnt = semanaNum;
        		mesNumAnt = mesNum;
        		anoNumAnt = anoNum;
        		
        		
        		// y acumulo
        		for(int i = 0; i<3;i++){
        			numSesionesAcum[i] += 1;
        			distanciAcum[i] += distanciaSesion;
        			tiempoAcum[i] += tiempoSesion;
        			altitudAcum[i] += altitudSesion;
        		}
        		
        		
        		 
        	 } while (c.moveToNext());
        	
        	
        	
        	//y añado lo que me queda sin poner
        	valores.clear();
			valores.add(semanaNumAnt + "/" + anoNum);
			valores.add(numSesionesAcum[0] + "" );
			valores.add(formatter.format( (double) distanciAcum[0]/1000 ));
			valores.add(cambioFormatos.desdeMStoHHMM(tiempoAcum[0]));
			valores.add( altitudAcum[0] + "" );
			creaFilaEstadisticas(valores,tablaPuntos[0], false);
			
			valores.clear();
			valores.add(mesNumAnt + "/" + anoNum);
			valores.add(numSesionesAcum[1] + "" );
			valores.add(formatter.format( (double) distanciAcum[1]/1000 ));
			valores.add(cambioFormatos.desdeMStoHHMM(tiempoAcum[1]));
			valores.add( altitudAcum[1] + "" );
			creaFilaEstadisticas(valores,tablaPuntos[1], false);
			
			valores.clear();
			valores.add(anoNumAnt + "" );
			valores.add(numSesionesAcum[2] + "" );
			valores.add(formatter.format( (double) distanciAcum[2]/1000 ));
			valores.add(cambioFormatos.desdeMStoHHMM(tiempoAcum[2]));
			valores.add( altitudAcum[2] + "" );
			creaFilaEstadisticas(valores,tablaPuntos[2], false);
        	
        	
        }
        
        c.close();
        
        
	}
	
	
	private void creaFilaEstadisticas(List<String> valores, TableLayout tabla, boolean cabecera) {
		
		creaFilaEstadisticas(valores, tabla, cabecera, false); 
	}

    private void creaFilaEstadisticas(List<String> valores, TableLayout tabla, boolean cabecera, boolean oculta) {
    	
    	//Create a new row to be added.
    	//TableRow tr = new TableRow(this);
    	final LayoutInflater inflater = LayoutInflater.from(this);
    	TableRow tr;
    	
    	if (oculta) 
    		tr = (TableRow) inflater.inflate(R.layout.filatablaestadisticassesiones_oculta, tabla, false);
    	else
    		tr = (TableRow) inflater.inflate(R.layout.filatablaestadisticassesiones, tabla, false);

    	TextView tv[] = {null,null,null,null,null};
    	tv[0] = (TextView) tr.findViewById(R.id.nombre);
    	tv[1] = (TextView) tr.findViewById(R.id.num);
    	tv[2] = (TextView) tr.findViewById(R.id.km);
    	tv[3] = (TextView) tr.findViewById(R.id.dur);
    	tv[4] = (TextView) tr.findViewById(R.id.alt);
    	
    	for(int i=0;i<5;i++) {
    		tv[i].setText(valores.get(i));
        	if (cabecera) {
        		tv[i].setTextSize(15);
        		tv[i].setBackgroundColor(0xFFfba824);
        	}
        	
    	}
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	//Add the new row to our tableLayout 
    	tabla.addView(tr);
    	
    }
	
	 private void fillListData() {
	        // Get all of the notes from the database and create the item list
		 	Cursor cSesiones  = registroDB.recuperaSesiones(-1);
	        startManagingCursor(cSesiones);
	        
	        ListaSesionesAdapter adptrSesiones = new ListaSesionesAdapter(this, R.layout.filasesiones, cSesiones);
	         
	        setListAdapter(adptrSesiones);
	        
	    }
	 
	 
	 
	 
	 @Override
	 protected void onListItemClick(ListView l, View v, int position, long id) {
		// vuelvo a la actividad principal
		Intent intent = new Intent(this, PuntosSesion.class);
		
		intent.putExtra("sesionId", getListAdapter().getItemId(position));
		
		startActivity(intent);
		
		//String item = String.valueOf ( getListAdapter().getItemId(position) );
		//Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
		 
		 
		} 
	
	 @Override
	 public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	   
	     AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	     menu.setHeaderTitle( ((ListaSesionesAdapter) getListAdapter()).getItemName(info.position) );
	     String[] menuItems = getResources().getStringArray(R.array.listasesionesmenu);
	     for (int i = 0; i<menuItems.length; i++) {
	       menu.add(Menu.NONE, i, i, menuItems[i]);
	     }
	   
	 }
	 
	 @Override
	 public boolean onContextItemSelected(MenuItem item) {
	   AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	   int menuItemIndex = item.getItemId();
	   
	   // lo primero en el menu es el borrar
	   if (menuItemIndex == 0) {
		   registroDB.borraSesion( getListAdapter().getItemId(info.position) );
		   
		   // requery esta deprecado lo que tengo que hacer es cargar todo de nuevo
		   fillListData();
		   
		   
	   }

	   return true;
	 }

	@Override
	public void onClick(View v) {
		if( v.getId() == R.id.cierra) {
			finish();	
		}
		else if( v.getId() == R.id.swipenext) {
			
			gestureListener.Mueve(true);
			
		}
		
	}

	
}
