package es.fenoll.javier;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ListActivity;
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
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class ListaSesiones extends ListActivity  {

	private AlmacenDatos registroDB;
	private GestureDetector gestureDetector;
	

	// para controlar lso gestos y en un swipe pasar a la otra pantalla
	 private class MyGestureDetector extends SimpleOnGestureListener {
	 		
	 		private static final int SWIPE_MIN_DISTANCE = 120;
	 		//private static final int SWIPE_MAX_OFF_PATH = 250;
	 		private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	 		
	 		@Override
	 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	 			
	 			ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.pantallasegistra);
	 			
	 			if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	 				viewFlipper.setInAnimation( AnimationUtils.loadAnimation(viewFlipper.getContext(), R.anim.push_left_in) );
	 				viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(viewFlipper.getContext(), R.anim.push_left_out) );
	 				viewFlipper.showNext();
	 				return true;
	 				}
	 			else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	 				viewFlipper.setInAnimation( AnimationUtils.loadAnimation(viewFlipper.getContext(), R.anim.push_right_in) );
	 				viewFlipper.setOutAnimation( AnimationUtils.loadAnimation(viewFlipper.getContext(), R.anim.push_right_out) );
	 				viewFlipper.showPrevious();
	 				return true;
	 				}
	 		
	 			return false;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listasesiones);
       
        // engancho para detectar el swipe
     	MyGestureDetector gestureListener = new MyGestureDetector();
     	gestureDetector = new GestureDetector(gestureListener);
     	
     	ListView lista = (ListView) findViewById(android.R.id.list);
     	lista.setOnTouchListener(new View.OnTouchListener() {

     		@Override
     		public boolean onTouch(View v, MotionEvent event) {
     		if (gestureDetector.onTouchEvent(event))
     		return true;
     		else
     		return false;
     		}
     		});
     	
     	
        
        // me quedo al tanto de long press para menus
        registerForContextMenu(getListView());
        getListView().setOnCreateContextMenuListener(this); 
		

     
     // creo la referencia a la BBDD
     	registroDB = new AlmacenDatos( (Context) this.getApplication() );        
       
     	rellenaEstadisticas();
     	
     	fillListData();
        
    
        //registerForContextMenu(getListView());
    }	
	
		
	private void rellenaEstadisticas() {
		

		List<String> valores = new ArrayList<String>();
		
		//Las tablas para los acumulados
		TableLayout tablaPuntos[] = {null,null,null};
		
		// Pensar si poner o no cabeceras y temas de primera fila fija pero no se
		// alinean bien las columnas
		tablaPuntos[0] = (TableLayout)findViewById(R.id.tablaAcumuladoSemanaCabecera);
		tablaPuntos[1] = (TableLayout)findViewById(R.id.tablaAcumuladoMesCabecera);
		tablaPuntos[2] = (TableLayout)findViewById(R.id.tablaAcumuladoAnoCabecera);
		
		tablaPuntos[0] = (TableLayout)findViewById(R.id.tablaAcumuladoSemana);
		tablaPuntos[1] = (TableLayout)findViewById(R.id.tablaAcumuladoMes);
		tablaPuntos[2] = (TableLayout)findViewById(R.id.tablaAcumuladoAno);
		
		
		//añado las cabeceras a las tablas
		for (int i = 0; i<3; i++) {
			valores.clear();
			valores.add("");
			valores.add(  getText(R.string.numero).toString()  );
			valores.add(  getText(R.string.displayDistanciaLbl).toString()  );
			valores.add(  getText(R.string.displayTiempoLbl).toString()  );
			valores.add(  getText(R.string.altAcumLbl).toString()  );
			creaFilaEstadisticas(valores,tablaPuntos[i]);
		}
		
		tablaPuntos[0] = (TableLayout)findViewById(R.id.tablaAcumuladoSemana);
		tablaPuntos[1] = (TableLayout)findViewById(R.id.tablaAcumuladoMes);
		tablaPuntos[2] = (TableLayout)findViewById(R.id.tablaAcumuladoAno);
		
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
        			
        			creaFilaEstadisticas(valores,tablaPuntos[0]);
        			
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
        			
        			creaFilaEstadisticas(valores,tablaPuntos[1]);
        			
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
        			
        			creaFilaEstadisticas(valores,tablaPuntos[2]);
        			
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
			creaFilaEstadisticas(valores,tablaPuntos[0]);
			
			valores.clear();
			valores.add(mesNumAnt + "/" + anoNum);
			valores.add(numSesionesAcum[1] + "" );
			valores.add(formatter.format( (double) distanciAcum[1]/1000 ));
			valores.add(cambioFormatos.desdeMStoHHMM(tiempoAcum[1]));
			valores.add( altitudAcum[1] + "" );
			creaFilaEstadisticas(valores,tablaPuntos[1]);
			
			valores.clear();
			valores.add(anoNumAnt + "" );
			valores.add(numSesionesAcum[2] + "" );
			valores.add(formatter.format( (double) distanciAcum[2]/1000 ));
			valores.add(cambioFormatos.desdeMStoHHMM(tiempoAcum[2]));
			valores.add( altitudAcum[2] + "" );
			creaFilaEstadisticas(valores,tablaPuntos[2]);
        	
        }
        
        c.close();
        
		
		
	}
	

    private void creaFilaEstadisticas(List<String> valores, TableLayout tabla) {
    	
    	//Create a new row to be added.
    	//TableRow tr = new TableRow(this);
    	final LayoutInflater inflater = LayoutInflater.from(this);
    	TableRow tr = (TableRow) inflater.inflate(R.layout.filatablaestadisticassesiones, tabla, false);
    	
    	TextView tv = (TextView) tr.findViewById(R.id.nombre);
    	tv.setText(valores.get(0));
    	
    	tv = (TextView) tr.findViewById(R.id.num);
    	tv.setText(valores.get(1));
    	
    	tv = (TextView) tr.findViewById(R.id.km);
    	tv.setText(valores.get(2));
    	
    	tv = (TextView) tr.findViewById(R.id.dur);
    	tv.setText(valores.get(3));
    	 
    	tv = (TextView) tr.findViewById(R.id.alt);
    	tv.setText(valores.get(4));
    	
    	
    	
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

	
}
