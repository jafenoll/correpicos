package es.fenoll.javier;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListaSesionesAdapter extends BaseAdapter  {

	// store the context (as an inflated layout)  
	private LayoutInflater inflater;      
	// store the resource (typically list_item.xml)    
	private int resource;
	// Aqui guardo los datos
	private Cursor cSesiones;
	
	/**         * Default constructor. Creates the new Adaptor object to 
	 *         * provide a ListView with data.         
	 *         * @param context       
	 *           * @param resource        
	 *            * @param data         */      
	public ListaSesionesAdapter(Context context, int resource, Cursor cData) { 
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		this.resource = resource; 
		this.cSesiones = cData;  
		}
	
	@Override
	public int getCount() {
		return cSesiones.getCount();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (!cSesiones.moveToPosition(position)) {    
			return -1;          
			} 
		
		return cSesiones.getLong(cSesiones.getColumnIndex(EstructuraDB.Sesion._ID) );
		
	}
	
	// devuelve un nombre para la sesion
	// por elmomento es sólo la fecha
	public String getItemName(int position) {
		if (!cSesiones.moveToPosition(position)) {    
			return "";          
			} 
		
		return cSesiones.getString( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_FECHA)  ) ;
		
	}

	

	/**         * Return a generated view for a position.         */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// reuse a given view, or inflate a new one from the xml  
		View view;                              
		if (convertView == null) {                  
			view = this.inflater.inflate(resource, parent, false);           
			} 
		else {     
			view = convertView;  
			}                 
		
		
		// bind the data to the view object 
		return this.bindData(view, position);  
		}
		
	
	/**         * Bind the provided data to the view.       
	 *   * This is the only method not required by base adapter.   
	 *         */    
	
	public View bindData(View view, int position) {  
		// si estoy fuera del cursor me voy 
		if (!cSesiones.moveToPosition(position)) {    
			return view;          
			}                        
		
		// Ponemos la fecha      
		TextView tvdiasem = (TextView) view.findViewById(R.id.filaSesionDiaSem);
		TextView tvdiames = (TextView) view.findViewById(R.id.filaSesionDiaMes);
		View tvfila =  view.findViewById(R.id.linearLayoutfilasesion);
		Date lafecha;
		String fechaFormateadaDiaSem = "--error--";
		String fechaFormateadaDiaMes = "--error--";
		
		int semanaNum = 0;
		
		int colorTexto = 0;
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try {
			//lafecha = DateFormat.getDateTimeInstance().parse(cSesiones.getString( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_FECHA)  ))  ;
			lafecha = df.parse(cSesiones.getString( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_FECHA)  ))  ;
			fechaFormateadaDiaSem = fechaFormateadaDiaMes = DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL).format (lafecha);
						
			df = new SimpleDateFormat("E");
			fechaFormateadaDiaSem = df.format(lafecha).toUpperCase() ;
			df = new SimpleDateFormat("d");
			fechaFormateadaDiaMes = df.format(lafecha);
			df = new SimpleDateFormat("MMM");
			fechaFormateadaDiaMes = fechaFormateadaDiaMes + df.format(lafecha);
			
			//calculo la semana del año para poner el fondo de la fila
			df = new SimpleDateFormat("w");
			semanaNum = Integer.parseInt(df.format(lafecha));
			if(semanaNum%2 == 0) {
				//fondo claro
				tvfila.setBackgroundColor(0xFFfed595 );
				colorTexto = 0xFF000000  ;
				}
			else {
				//fondo oscuro
				tvfila.setBackgroundColor(0xFFfba824 );
				colorTexto = 0xFF000000 ;
			}
			
			} catch (ParseException e) {
				e.printStackTrace();
							} 
		tvdiasem.setText( fechaFormateadaDiaSem );
		tvdiasem.setTextColor(colorTexto);
		tvdiames.setText( fechaFormateadaDiaMes );
		tvdiames.setTextColor(colorTexto);
		
		
		//cambio los colores de las label
		TextView tv = (TextView) view.findViewById(R.id.displayDistanciaLbl);
		tv.setTextColor(colorTexto);
		
		tv = (TextView) view.findViewById(R.id.displayTiempoLbl);
		tv.setTextColor(colorTexto);
		
		tv = (TextView) view.findViewById(R.id.displayVelocidadLblMms);
		tv.setTextColor(colorTexto);
		
		tv = (TextView) view.findViewById(R.id.displayDesnivelLbl);
		tv.setTextColor(colorTexto);
		
		// obtenemos distancia y tiempo
		long distanciaTotal = cSesiones.getLong( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_DISTANCIA)  ) ;
		long tiempoTotal = cSesiones.getLong( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_DURACION)  );
		long desnivelAcum = cSesiones.getLong( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_ALTITUD_POS)  );
		
		
		
		//pongo la distancia en Km y un decimal
		tv = (TextView) view.findViewById(R.id.filaSesionDistancia);   
		NumberFormat formatter = new DecimalFormat("#.##");
		tv.setText(  formatter.format( (double) distanciaTotal/1000 ) );
		tv.setTextColor(colorTexto);
		
		// pongo el tiempo en h:mm
		tv = (TextView) view.findViewById(R.id.filaSesionDuracion); 
		FormatosDisplay cambioFormatos = new FormatosDisplay();
		String textoFinal = cambioFormatos.desdeMStoHHMM( tiempoTotal );
		tv.setText( textoFinal  );
		tv.setTextColor(colorTexto);
		
		//pongo la velocidad
		tv = (TextView) view.findViewById(R.id.filaSesionVelocidad); 
		if ( distanciaTotal != 0 && tiempoTotal != 0 ) {	
			// calculo la velocidad media y la pongo en MxKm
			// recordar que la distancia llega en m y el tiempo en ms
			tv.setText(  cambioFormatos.desdeMsaMKM( (double) distanciaTotal/(tiempoTotal/1000) ) );
		}
		else {
			tv.setText("--");
		}
		tv.setTextColor(colorTexto);
		
		//pongo la altitud acumulada
		tv = (TextView) view.findViewById(R.id.filaSesionDesnivel);   
		tv.setText(  "+" + desnivelAcum );
		tv.setTextColor(colorTexto);
		
		// return the final view object   
		return view;      
		} 
	
	

	
	
	}

