package es.fenoll.javier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.osmdroid.util.GeoPoint;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;


public class AlmacenDatos  {

	// Used for debugging and logging
    private static final String TAG = "AlmacenDatos";
    private static final String FormatoFechaFichGPX = "yyyy-MM-dd HH:mm:ss.SSSZ";
	
    
    private static final String pathSesiones = "correpicos" + File.separator + "sesiones" ;
    
	
	/**
     * The database that the provider uses as its underlying data store
     */
    private static final String DATABASE_NAME = "correcaminos.db";

    /**
     * The database version
     */
    private static final int DATABASE_VERSION = 16;
	
    // Handle to a new DatabaseHelper.
    private DatabaseHelper mOpenHelper;

    
    
	public AlmacenDatos(Context context) {
		
		// Creates a new helper object. Note that the database itself isn't opened until
	    // something tries to access it, and it's only created if it doesn't already exist.
	    mOpenHelper = new DatabaseHelper(context);
	    
	}
	
	// Para crear  la bbdd
	static class DatabaseHelper extends SQLiteOpenHelper {

	       DatabaseHelper(Context context) {
	           // calls the super constructor, requesting the default cursor factory
	    	   super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    	
	       }

	       /**
	        *
	        * Creates the underlying database with table name and column names taken from the
	        * NotePad class.
	        */
	       @Override
	       public void onCreate(SQLiteDatabase db) {
	           db.execSQL("CREATE TABLE " + EstructuraDB.Punto.TABLE_NAME + " ("
	                   + EstructuraDB.Punto._ID + " INTEGER PRIMARY KEY,"
	                   + EstructuraDB.Punto.COLUMN_NAME_SECUENCIA + " INTEGER,"
	                   + EstructuraDB.Punto.COLUMN_NAME_LAT + " DOUBLE,"
	                   + EstructuraDB.Punto.COLUMN_NAME_LONG + " DOUBLE,"
	                   + EstructuraDB.Punto.COLUMN_NAME_DISTANCIA + " DOUBLE,"
	                   + EstructuraDB.Punto.COLUMN_NAME_VELOCIDAD + " DOUBLE,"
	                   + EstructuraDB.Punto.COLUMN_NAME_ALTITUD + " DOUBLE,"
	                   + EstructuraDB.Punto.COLUMN_NAME_TIMESTAMP + " DOUBLE,"
	                   + EstructuraDB.Punto.COLUMN_NAME_TIEMPOPAUSADO + " DOUBLE,"
	                   + EstructuraDB.Punto.COLUMN_NAME_SESION + " INTEGER,"
	                   + EstructuraDB.Punto.COLUMN_NAME_PRECISIONH + " LONG,"
	                   + EstructuraDB.Punto.COLUMN_NAME_INTERVALO + " TEXT"
	                   + ");");
	           
	           db.execSQL("CREATE TABLE " + EstructuraDB.Sesion.TABLE_NAME + " ("
	                   + EstructuraDB.Sesion._ID + " INTEGER PRIMARY KEY," 
	                   + EstructuraDB.Sesion.COLUMN_NAME_FECHA + " TEXT,"
	                   + EstructuraDB.Sesion.COLUMN_NAME_DISTANCIA + " LONG,"
	                   + EstructuraDB.Sesion.COLUMN_NAME_DURACION + " DOUBLE,"
	                   + EstructuraDB.Sesion.COLUMN_ALTITUD_POS + " INTEGER,"
	                   + EstructuraDB.Sesion.COLUMN_ALTITUD_NEG + " INTEGER,"
	                   + EstructuraDB.Sesion.COLUMN_NAME_FICHERO + " TEXT,"
	                   + EstructuraDB.Sesion.COLUMN_NAME_DESC + " TEXT,"
	                   + EstructuraDB.Sesion.COLUMN_NAME_RATING + " INTEGER"
	                   + ");");
	           
	           db.execSQL("CREATE TABLE " + EstructuraDB.Deportes.TABLE_NAME + " ("
	                   + EstructuraDB.Deportes._ID + " INTEGER PRIMARY KEY," 
	                   + EstructuraDB.Deportes.COLUMN_NAME_NOMBRE + " TEXT,"
	                   + EstructuraDB.Deportes.COLUMN_NAME_GPS_GAP_DIST + " INTEGER,"
	                   + EstructuraDB.Deportes.COLUMN_NAME_GPS_GAP_TIEMPO + " INTEGER,"
	                   + EstructuraDB.Deportes.COLUMN_NAME_AUTOPAUSE + " BOOLEAN,"
	                   + EstructuraDB.Deportes.COLUMN_NAME_UMBRAL_AUTOPAUSE + " DOUBLE"
	                   
	                   + ");");
	           db.execSQL("INSERT INTO " + EstructuraDB.Deportes.TABLE_NAME 
	        		   + " VALUES (0,'correr',10,6, 1, 0.5);");
	        		   
	           
	           
	       }

	       /**
	        *
	        * Demonstrates that the provider must consider what happens when the
	        * underlying datastore is changed. In this sample, the database is upgraded the database
	        * by destroying the existing data.
	        * A real application should upgrade the database in place.
	        */
	       @Override
	       public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	    	   
	           // Logs that the database is being upgraded
	           Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
	                   + newVersion + ", which will destroy all old data");

	           
	           
               db.execSQL("ALTER TABLE " + EstructuraDB.Punto.TABLE_NAME
       		   		+ " ADD COLUMN " + EstructuraDB.Punto.COLUMN_NAME_INTERVALO + " TEXT" 
       		   		+ ";"
       		   );
               /*
               db.execSQL("UPDATE " + EstructuraDB.Sesion.TABLE_NAME
            		   + " SET " +  EstructuraDB.Sesion.COLUMN_NAME_RATING + "= 0;"
            		   );
               /*
               db.execSQL("ALTER TABLE " + EstructuraDB.Sesion.TABLE_NAME
          		   		+ " ADD COLUMN " + EstructuraDB.Sesion.COLUMN_ALTITUD_NEG + " INTEGER"
          		   		+ ";"
          		   );
               */
	           /*
	           db.execSQL("ALTER TABLE " + EstructuraDB.Deportes.TABLE_NAME
	        		   		+ " ADD COLUMN " + EstructuraDB.Deportes.COLUMN_NAME_UMBRAL_AUTOPAUSE + " DOUBLE"
	        		   		+ ";"
	                   
	        		   );
	           */
	           /*
	           
	           // Kills the table and existing data
	           db.execSQL("DROP TABLE IF EXISTS " + EstructuraDB.Punto.TABLE_NAME );
	           db.execSQL("DROP TABLE IF EXISTS " + EstructuraDB.Sesion.TABLE_NAME );
	           db.execSQL("DROP TABLE IF EXISTS " + EstructuraDB.Deportes.TABLE_NAME );

	           // Recreates the database with a new version
	           onCreate(db);
	           
	           */
	           
	       }
	   }

	public long insertaPunto(PuntoGPX elPuntoGPX) {
		
		
		ContentValues valores = new ContentValues();
		valores.put(EstructuraDB.Punto.COLUMN_NAME_SECUENCIA, elPuntoGPX.index);
		// en la BBDD lo guardo en grados, no en milesimas de grado como es el formato E6
		valores.put(EstructuraDB.Punto.COLUMN_NAME_LAT,elPuntoGPX.posicion.getLatitudeE6()/1E6 );
		valores.put(EstructuraDB.Punto.COLUMN_NAME_LONG,elPuntoGPX.posicion.getLongitudeE6()/1E6 );
		valores.put(EstructuraDB.Punto.COLUMN_NAME_DISTANCIA, elPuntoGPX.distancia );
		valores.put(EstructuraDB.Punto.COLUMN_NAME_SESION, elPuntoGPX.sesionId );
		valores.put(EstructuraDB.Punto.COLUMN_NAME_VELOCIDAD, elPuntoGPX.velocidad );
		valores.put(EstructuraDB.Punto.COLUMN_NAME_ALTITUD, elPuntoGPX.altitud );
		valores.put(EstructuraDB.Punto.COLUMN_NAME_PRECISIONH, elPuntoGPX.precisionH );
		valores.put(EstructuraDB.Punto.COLUMN_NAME_TIMESTAMP, elPuntoGPX.timestamp );
		valores.put(EstructuraDB.Punto.COLUMN_NAME_TIEMPOPAUSADO, elPuntoGPX.tiempopausado );
		valores.put(EstructuraDB.Punto.COLUMN_NAME_INTERVALO, elPuntoGPX.intervalo );
		
		
		// Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
        // Performs the insert and returns the ID of the new note.
        long rowId = db.insert(
        	EstructuraDB.Punto.TABLE_NAME,        // The table to insert into.
        	null,
        	valores                           // A map of column names, and the values to insert
                                             // into the columns.
        );

		return rowId;
		}
	
	//actualiza la sesion con la distancia y tiempototales al terminar
	// distancia en metros
	// duracion en ms
	// desniveles en metros
	// tambie mueva ya los puntos al GPX
	public void terminaSesion(long sesionId) {
		
		//Al tyerminar la sesion la guerdo en el gpx
		Sesion lasesion = guardaSesionGPX(sesionId);
		//String nomFicheroSesion = guardaSesionGPX(sesionId).nomFicheroSesion;
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		ContentValues  valores = new ContentValues();
        valores.put(EstructuraDB.Sesion.COLUMN_NAME_DISTANCIA, lasesion.distancia );
        valores.put(EstructuraDB.Sesion.COLUMN_NAME_DURACION, lasesion.duracion );
        valores.put(EstructuraDB.Sesion.COLUMN_ALTITUD_POS, lasesion.altitudPos );
        valores.put(EstructuraDB.Sesion.COLUMN_ALTITUD_NEG, lasesion.altitudNeg );
        valores.put(EstructuraDB.Sesion.COLUMN_NAME_FICHERO,lasesion.nomFicheroSesion);
        valores.put(EstructuraDB.Sesion.COLUMN_NAME_RATING,0);
        
		db.update(EstructuraDB.Sesion.TABLE_NAME,valores,EstructuraDB.Sesion._ID + "=" + sesionId, null);
		
		db.close();
		
		
	}
	
	//actualiza la sesion con la distancia y tiempototales al terminar
	public void actualizaDescSesion(long rowid, String desc) {
			
			
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			
			ContentValues  valores = new ContentValues();
	        valores.put(EstructuraDB.Sesion.COLUMN_NAME_DESC, desc );
	       
	        
			db.update(EstructuraDB.Sesion.TABLE_NAME,valores,EstructuraDB.Sesion._ID + "=" + rowid, null);
			
			db.close();
			
			//TODO: Tambien actualizar fichero gpx
			
		}
	
	
	public void actualizaRatingSesion(long rowid, int rating) {
		
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		ContentValues  valores = new ContentValues();
        valores.put(EstructuraDB.Sesion.COLUMN_NAME_RATING, rating );
       
        
		db.update(EstructuraDB.Sesion.TABLE_NAME,valores,EstructuraDB.Sesion._ID + "=" + rowid, null);
		
		db.close();
		
		//TODO: Tambien actualizar fichero gpx
		
	}
	
	// crea una sesion nueva y devuelve su rowid, automatimente pone la fecha de inicio
	public long insertaSesion(Date laFecha) {
		
		// Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
          
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        if (laFecha == null) {
        	Calendar c = Calendar.getInstance();
        	laFecha = c.getTime();
        }
        
        String formattedDate = df.format(laFecha);

        ContentValues  valores = new ContentValues();
        valores.put(EstructuraDB.Sesion.COLUMN_NAME_FECHA, formattedDate );
        
        // Performs the insert and returns the ID of the new note.
        long rowId = db.insert(
        	EstructuraDB.Sesion.TABLE_NAME,        // The table to insert into.
        	null,
        	valores                           // A map of column names, and the values to insert
                                             // into the columns.
        );


		return rowId;
		}
	
	public Cursor recuperaSesionesDestacadas() {
		
		return recuperaSesiones(-2);
	}
	
	/*
	 * Return a Cursor over the list of all sesiones in the database
     * 
     * @return Cursor over sesiones, si se quieren todas las sesiones, pasar un -1
     */
	public Cursor recuperaSesiones(long sesionId) {
		
		// Opens the database object in "read" mode.
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
          
		String strQuery = null;
		String strOrder = null;
		if (sesionId > 1) {
			
			strQuery = EstructuraDB.Sesion._ID + "=" + sesionId;
		}
		else if (sesionId == -2) {
			
			strQuery = EstructuraDB.Sesion.COLUMN_NAME_RATING + "> 0 ";
		}
		else {
			// if multiple records order by 
			strOrder =  EstructuraDB.Sesion.COLUMN_NAME_FECHA + " DESC" ;
		}
		
        
		 return db.query(
				 EstructuraDB.Sesion.TABLE_NAME, 
				 new String[] {	EstructuraDB.Sesion._ID,
						 		EstructuraDB.Sesion.COLUMN_NAME_FECHA,
								EstructuraDB.Sesion.COLUMN_NAME_DISTANCIA ,
								EstructuraDB.Sesion.COLUMN_NAME_DURACION,
								EstructuraDB.Sesion.COLUMN_ALTITUD_POS,
								EstructuraDB.Sesion.COLUMN_ALTITUD_NEG,
								EstructuraDB.Sesion.COLUMN_NAME_DESC,
								EstructuraDB.Sesion.COLUMN_NAME_RATING}
		 		, strQuery, null, null, null, strOrder
		 		);
		
	}
	
	
	// dado un id de sesion, devuelve el parser para emprezar a leer los puntos
	public XmlPullParser  PreparaRecuperaPuntosSesionGpx(long sesionId) {
		
		
        String NomFichero = getNomFicheroPuntosSesion(sesionId);
    	File path = new File(Environment.getExternalStorageDirectory()+ File.separator  + pathSesiones);
    	File file = new File(path, NomFichero);
        //compruebo si la SD esta bien
    	String state = Environment.getExternalStorageState();
    	if (! Environment.MEDIA_MOUNTED.equals(state)) {
    	    // MEDIA_MOUNTED means we can read and write the media
    		return null;
    	}
    	
    	InputStream gpxreader;
    	XmlPullParser parser = Xml.newPullParser();
		
    	try {
    		
			gpxreader = new FileInputStream(file);
			parser.setInput(gpxreader, null);
			
    	} catch (FileNotFoundException e) {
			//e.printStackTrace();
			return null;
		} catch (XmlPullParserException e) {
			//e.printStackTrace();
			return null;
		}
    	
    	return parser;
    	
	}
			
	
	public String  RecuperaComentarioSesionGpx ( XmlPullParser parser) {
		
		int evento;
		String etiqueta = null;
		String valor="";
		
		try {
			
			evento = parser.next();
			
			
			while ( evento != XmlPullParser.END_DOCUMENT ) {
				
				switch (evento) {
				 
                case XmlPullParser.TEXT:
                	
                	valor += parser.getText();
                	break;
                    
                case XmlPullParser.END_TAG:
                	
                	etiqueta = parser.getName();
               	 
                    if (etiqueta.equals("cmt"))  {
                    	// si ha terminado el punto pues lo devuelvo
                    	return valor;
                    	}
                    break;
				}
				evento = parser.next();
			}
			
		} catch (XmlPullParserException e) {
			//e.printStackTrace();
			return null;
		} catch (IOException e) {
			//e.printStackTrace();
			return null;
		}

		return null;
		
		
	}
	
	
			
	public PuntoGPX  RecuperaPuntoSesionGpx ( XmlPullParser parser) {
			
			int evento;
			String etiqueta = null;
			PuntoGPX elPunto = null;
			String valor="";
			long puntosSesion=0;
			
			try {
				
				
				
				
				evento = parser.next();
				
				
				while ( evento != XmlPullParser.END_DOCUMENT ) {
					

					switch (evento) {
					
					case XmlPullParser.END_DOCUMENT:
						
						return null;
						
					case XmlPullParser.START_DOCUMENT:
						 
	                    break;
	 
	                case XmlPullParser.START_TAG:
	 
	                	valor = "";
	                    etiqueta = parser.getName();
	 
	                    if (etiqueta.equals("trkpt"))  {
	                    	
	                    	elPunto = new PuntoGPX();
	                    	elPunto.puntosSesion = puntosSesion;
	                    	elPunto.posicion = new GeoPoint( Double.valueOf( parser.getAttributeValue(0))  , Double.valueOf( parser.getAttributeValue(1) ) );
	                    	
	                        }
	                    
	                    break;
	                    
	                case XmlPullParser.TEXT:
	                	
	                	valor += parser.getText();
	                	break;
	                    
	                case XmlPullParser.END_TAG:
	                	
	                	etiqueta = parser.getName();
	               	 
	                    if (etiqueta.equals("trkpt"))  {
	                    	// si ha terminado el punto pues lo devuelvo
	                    	return elPunto;
	                    	
	                        }
	                    else if(etiqueta.equals("ele")) {
	                    	elPunto.altitud = Double.valueOf(valor).longValue();
	                    }
	                    else if(etiqueta.equals("dist")) {
	                    	elPunto.distancia = Double.valueOf(valor).longValue();
	                    }
	                    else if(etiqueta.equals("time")) {
	                    	// aqui lo devo pasa a ms, 
							try {
								SimpleDateFormat df = new SimpleDateFormat( FormatoFechaFichGPX );
		            			Date lafecha;
								lafecha = df.parse( valor );
								elPunto.timestamp = lafecha.getTime();
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	                    	
	                    }
	                    else if(etiqueta.equals("tmpt")) {
	                    	elPunto.timestamp = Double.valueOf(valor).longValue();
	                    }
	                    else if(etiqueta.equals("tmpPausado")) {
	                    	elPunto.tiempopausado = Double.valueOf(valor).longValue();
	                    }
	                    else if(etiqueta.equals("seq")) {
	                    	elPunto.index = Double.valueOf(valor).longValue();
	                    }
	                    else if(etiqueta.equals("numpuntos")) {
	                    	puntosSesion = Double.valueOf(valor).longValue();
	                    }
	                    else if(etiqueta.equals("intervalo")) {
	                    	elPunto.intervalo = valor;
	                    }
	                    else if(etiqueta.equals("vel")) {
	                    	elPunto.velocidad = Double.valueOf(valor).longValue();
	                    }
	                    else if(etiqueta.equals("hdop")) {
	                    	elPunto.precisionH = Double.valueOf(valor).longValue();
	                    }
	                    
	                    
	                    
	                    valor = "";
	                	
	                	break;
					
					}
					
					evento = parser.next();
				
				}
				
	
			} catch (XmlPullParserException e) {
				//e.printStackTrace();
				return null;
			} catch (IOException e) {
				//e.printStackTrace();
				return null;
			}
			
			
			return elPunto;
	
	}
	
	/**
	 * Return a Cursor over the list of all puntos of a known sesion
     * 
     * @return Cursor over all puntos de una sesion
     */
	
	public Cursor recuperaPuntosSesion(long sesionId) {
		
		// Opens the database object in "read" mode.
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
          
		 return db.query(
				 EstructuraDB.Punto.TABLE_NAME, 
				 new String[] {	EstructuraDB.Punto.COLUMN_NAME_SECUENCIA,
						 		EstructuraDB.Punto.COLUMN_NAME_LAT, 
						        EstructuraDB.Punto.COLUMN_NAME_LONG,
						        EstructuraDB.Punto.COLUMN_NAME_DISTANCIA,
						        EstructuraDB.Punto.COLUMN_NAME_VELOCIDAD,
						        EstructuraDB.Punto.COLUMN_NAME_ALTITUD,
						        EstructuraDB.Punto.COLUMN_NAME_TIMESTAMP,
						        EstructuraDB.Punto.COLUMN_NAME_TIEMPOPAUSADO,
						        EstructuraDB.Punto.COLUMN_NAME_PRECISIONH,
						        EstructuraDB.Punto.COLUMN_NAME_INTERVALO}
		 		, EstructuraDB.Punto.COLUMN_NAME_SESION + "=" + sesionId , null, null, null, null
		 		);
		
	}
	
	public String getNomFicheroPuntosSesion(long sesionId) {
		
		// Opens the database object in "read" mode.
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
          
        Cursor c=  db.query(
			 EstructuraDB.Sesion.TABLE_NAME, 
			 new String[] {	EstructuraDB.Sesion.COLUMN_NAME_FICHERO}
	 		, EstructuraDB.Sesion._ID + "=" + sesionId, null, null, null, null
	 		);
		
        c.moveToFirst();
        
        String resultado = c.getString( c.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_FICHERO));
        
        c.close();
        
		return resultado ;
	
	}
	
	
	// borra una sesion y toos sus puntos dado du id
	public boolean borraSesion(long sesionId) {
			
		// Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
        // borro el fichero GPX 
        String NomFichero = getNomFicheroPuntosSesion(sesionId);
    	File path = new File(Environment.getExternalStorageDirectory()+ File.separator  + pathSesiones);
    	File file = new File(path, NomFichero);
        //compruebo si la SD esta bien
    	String state = Environment.getExternalStorageState();
    	if (! Environment.MEDIA_MOUNTED.equals(state)) {
    	    // MEDIA_MOUNTED means we can read and write the media
    		return false;
    	}
    	file.delete();

        // Borro los puntos por si los hubiese
        db.delete(
        	EstructuraDB.Punto.TABLE_NAME,        
        	EstructuraDB.Punto.COLUMN_NAME_SESION + "=" + sesionId,
        	null                           
        );
        
     // Borro la sesion
        db.delete(
        	EstructuraDB.Sesion.TABLE_NAME,        
        	EstructuraDB.Sesion._ID + "=" + sesionId,
        	null                           
        );
        
		
        db.close();
        
		return true;
	}

	public class Deporte{
		
		public String nombre;
		public int gpsgapdist;
		public int gpsgaptmp;
		public boolean autopause;
		public Double umbralautopause;
		
	}
	
	public class PuntoGPX{
		
		public PuntoGPX() {
			sesionId = null;
			posicion = null;
			distancia = null;
			altitud = null;
			index = null;
			timestamp = null;
			tiempopausado = null;
			puntosSesion = null;
			velocidad = null;
			precisionH = null;
			intervalo = null;
			
		}
		
		public Long sesionId;
		public GeoPoint posicion;
		public Long distancia;
		public Long altitud;
		public Long index;
		public Long timestamp;
		public Long tiempopausado;
		public Long puntosSesion;
		public Long velocidad;
		public Long precisionH;
		public String intervalo;
		
	}
	
	public class Sesion {
		
		public  Long sesionId;
		public Long distancia;
		public Long duracion;
		public Long altitudPos;
		public Long altitudNeg;
		public String nomFicheroSesion;
		public String descripcion;
		public String rating;
		
	}
	
	
	
	
	//solo abro y cierro la DB en modo escritura para que el metodo onUpgrade se lance con permisos si lo necesita
	// este metodo se llamara nada mas arrancar la app y solo en ese momento
	public void  ForceUpgradeDB(){
		
		// Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.close();
		
	}
	
	
	public Deporte recuperaDeporte(long deporteId) {
		// Opens the database object in "read" mode.
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Deporte resultado = new Deporte();  
        
		Cursor c = db.query(
				 EstructuraDB.Deportes.TABLE_NAME, 
				 new String[] {	EstructuraDB.Deportes.COLUMN_NAME_NOMBRE,
						 		EstructuraDB.Deportes.COLUMN_NAME_GPS_GAP_DIST, 
						        EstructuraDB.Deportes.COLUMN_NAME_GPS_GAP_TIEMPO,
						        EstructuraDB.Deportes.COLUMN_NAME_AUTOPAUSE,
						        EstructuraDB.Deportes.COLUMN_NAME_UMBRAL_AUTOPAUSE}
		 		, EstructuraDB.Deportes._ID + "=" + deporteId , null, null, null, null
		 		);
		        
        c.moveToFirst();
        
        resultado.nombre = c.getString( c.getColumnIndex(EstructuraDB.Deportes.COLUMN_NAME_NOMBRE));
        resultado.gpsgapdist = c.getInt( c.getColumnIndex(EstructuraDB.Deportes.COLUMN_NAME_GPS_GAP_DIST));
        resultado.gpsgaptmp = c.getInt( c.getColumnIndex(EstructuraDB.Deportes.COLUMN_NAME_GPS_GAP_TIEMPO));
        
        if ( c.getInt( c.getColumnIndex(EstructuraDB.Deportes.COLUMN_NAME_AUTOPAUSE)) == 1) {
        	resultado.autopause = true;
        }
        else {
        	resultado.autopause = false;
        }
        	
        resultado.umbralautopause = c.getDouble( c.getColumnIndex(EstructuraDB.Deportes.COLUMN_NAME_UMBRAL_AUTOPAUSE));
        
        
        return resultado;
	}
	
	public void actualizaDeporte(long rowid, Deporte elDeporte) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		ContentValues  valores = new ContentValues();
        valores.put(EstructuraDB.Deportes.COLUMN_NAME_NOMBRE, elDeporte.nombre );
        valores.put(EstructuraDB.Deportes.COLUMN_NAME_GPS_GAP_DIST, elDeporte.gpsgapdist );
        valores.put(EstructuraDB.Deportes.COLUMN_NAME_GPS_GAP_TIEMPO, elDeporte.gpsgaptmp );
        valores.put(EstructuraDB.Deportes.COLUMN_NAME_AUTOPAUSE, elDeporte.autopause );
        valores.put(EstructuraDB.Deportes.COLUMN_NAME_UMBRAL_AUTOPAUSE, elDeporte.umbralautopause );
        
		db.update(EstructuraDB.Deportes.TABLE_NAME,valores,EstructuraDB.Deportes._ID + "=" + rowid, null);
		
		db.close();
	}
	
	// guarda la sesion en GPX y devuelve el nombre del fichero (sin ruta)
	// como esto se hce para pasar los puntos de la BBDD a la SD y se hace siempre
	// al terminar la sesion, realmenet nunca tiene ni rating ni descripcion
	// pero aun asi las gestiono por si acaso
	public Sesion guardaSesionGPX(long sesionID ) {
		
		//compruebo si la SD esta bien
    	String state = Environment.getExternalStorageState();
    	if (! Environment.MEDIA_MOUNTED.equals(state)) {
    	    // MEDIA_MOUNTED means we can read and write the media
    		return null;
    	} 
    	
    	// obtengo lo que necesito de la sesion
    	Cursor cSesiones  = recuperaSesiones(sesionID);
                
        if (cSesiones.getCount() != 1) {    
			return null;          
			}    
		
        cSesiones.moveToFirst();
        
        String nomFicheroExport="";
        String nomSesion="";
       
        
        nomSesion = "" + sesionID;
        
        String estrellasSesion = cSesiones.getString( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_RATING)  );
        String descSesion = cSesiones.getString( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_DESC)  );
        
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        Long timestampPunto = (long) 0, tiempoPausadoTotal = (long) 0, timestampPuntoInicio = (long) 0;
        Date lafecha = null;
        
        
        try {
	    	
	    	lafecha = df.parse(cSesiones.getString( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_FECHA)  ))  ;
	    	df = new SimpleDateFormat("yyyy-M-dd_HH_mm");
			nomFicheroExport = df.format(lafecha);
			
			
			df = new SimpleDateFormat( FormatoFechaFichGPX );
			
			
			nomFicheroExport += ".gpx";
			
        }
        catch (ParseException e) {
			//e.printStackTrace();
						} 
        
    	File path = new File(Environment.getExternalStorageDirectory()+ File.separator  + pathSesiones);
    	File file = new File(path, nomFicheroExport);
    	
    	Cursor cPuntos = recuperaPuntosSesion(sesionID);
    	cPuntos.moveToFirst();
    	
    	

         String lat = "", lon="", alt="", hdop="", seq="", vel="", tmpPausa = "", intervalo = "";
         Double altitudPos=0.0, altitudNeg=0.0, altitud=null, altitudAnt=null, dist=null;
        

    	 try {
    		 
         	 path.mkdirs();
 	    	 FileWriter gpxwriter = new FileWriter(file);
 	         BufferedWriter out = new BufferedWriter(gpxwriter);
 	         
 	        
 	         //escribo las cabeceras del gpx
 	        
 	         out.write("<?xml version='1.0' encoding='UTF-8'?>\n");
 	        
 	         out.write("<gpx\n");
 	         out.write("version='1.1'\n");
 	         out.write("creator='Correpicos - http://www.corepicos.es/'\n");
 	    	 out.write("xmlns='http://www.topografix.com/GPX/1/1'\n");
 	    	 out.write("xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n");
 	    	 out.write("xmlns:cpc='http://www.corepicos.es/gpx/CpcExtension/v1'\n");
 	    	 out.write("xsi:schemaLocation='http://www.topografix.com/GPX/1/1\n");
 	    	 out.write("http://www.topografix.com/GPX/1/1/gpx.xsd\n");
 	    	 out.write("http://www.corepicos.es/gpx/CpcExtension/v1\n");
 	    	 out.write("http://www.corepicos.es/gpx/CpcExtension/v1/CpcExtensionv1.xsd'>\n");
 	         
 	    	 out.write("<trk>\n");
 	    	 
 	    	 out.write("<name>" + nomSesion + "</name>\n");
 	    	 // aqui meto si es stared o no
 	    	 out.write("<cmt>" + estrellasSesion + "</cmt>\n");
 	    	 // aqui meto la descripcion
 	    	 out.write("<desc>" + descSesion + "</desc>\n");
 	    	 
 	    	 out.write("<number>1</number>");
 	    	
 	    	 
 	    	
 	    	 long numPuntos = cPuntos.getCount();
 	    	
 	    	 
 	    	 out.write("<extensions>\n");
        	 out.write("<cpc:CrcExtension>\n");
        	 out.write("<cpc:numpuntos>" + numPuntos + "</cpc:numpuntos>\n");  	
        	 out.write("<cpc:rating>" + estrellasSesion + "</cpc:rating>\n");
        	 out.write("</cpc:CrcExtension>\n");
        	 out.write("</extensions>\n");
        	 out.write("<trkseg>\n");
        	 
        	 lat = ""; lon=""; alt=""; hdop=""; seq=""; dist= (double) 0; vel="0"; tmpPausa = "0";
        	
        	 
 	    	 if ( numPuntos > 0 ) {
 	    		 
 	    		//guardo esto para tener el tempo inicial
 	    		timestampPuntoInicio = cPuntos.getLong( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_TIMESTAMP));
 	       
	 	         do {
	 	        	
	 	        	 
	 	        	 lat = Double.toString( cPuntos.getDouble( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_LAT)) );
	 	        	 lon =  Double.toString( cPuntos.getDouble( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_LONG)) );
	 	        	 altitud = cPuntos.getDouble( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_ALTITUD));
	 	        	 alt = Double.toString( altitud );
	 	        	 hdop = Double.toString( cPuntos.getDouble( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_PRECISIONH)) );
	 	        	 seq = Double.toString( cPuntos.getDouble( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_SECUENCIA)) );
	 	        	 dist =  cPuntos.getDouble( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_DISTANCIA));
	 	        	 vel = Double.toString( cPuntos.getDouble( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_VELOCIDAD)) );
	 	        	 timestampPunto = cPuntos.getLong( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_TIMESTAMP));
	 	        	
	 	        	 Long tiempoPausaPunto = cPuntos.getLong( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_TIEMPOPAUSADO));
	 	        	 tiempoPausadoTotal += tiempoPausaPunto;
	 	        	 tmpPausa  = Double.toString( tiempoPausaPunto );
	 	        	 
	 	        	 intervalo = cPuntos.getString( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_INTERVALO));
	 	        		 	
	 	        	 if (altitud != null ) {
	 	        		 
	 	        		 if (altitudAnt != null) {
	 	        			 
	 	        			double cambioAltid = altitud - altitudAnt;
	 	        			if (cambioAltid > 0) {
	 	        				altitudPos += cambioAltid;
	 	        			}
	 	        			else {
	 	        				altitudNeg += -1 * cambioAltid;
	 	        			}
	 	        	 
	 	        		 }
	 	        		altitudAnt = altitud;
	 	        		 
	 	        		 
	 	        		 
	 	        	 }
	 	        	 
	 	        	 out.write("<trkpt ");
	 	        	 out.write("lat='" +  lat + "'");
	 	        	 out.write(" lon='" + lon + "'>\n");
	 	        	 out.write("<ele>" + alt + "</ele>\n");
	 	        	 out.write("<time>" + df.format(new Date(timestampPunto)) + "</time>\n");
	 	        	 out.write("<hdop>" + hdop + "</hdop>\n"); 
	 	        	 out.write("<extensions>\n");
	 	        	 out.write("<cpc:CrcExtension>\n");
	 	        	 out.write("<cpc:seq>" + seq + "</cpc:seq>\n");
	 	        	 out.write("<cpc:dist>" + dist + "</cpc:dist>\n");
	 	        	 out.write("<cpc:vel>" + vel + "</cpc:vel>\n");
	 	        	 out.write("<cpc:tmpPausado>" + tmpPausa + "</cpc:tmpPausado>\n"); 
	 	        	 out.write("<cpc:intervalo>" + intervalo + "</cpc:intervalo>\n" ) ;
	 	        	 out.write("</cpc:CrcExtension>\n");
	 	        	 out.write("</extensions>\n");
	 	        	 
	 	        	 out.write("</trkpt>\n");
	 	        	 
 	        	             	
 	         	} while (cPuntos.moveToNext() );
 	         
 	    	 }
            	
 	         
 	        out.write("</trkseg>\n");
 	        out.write("</trk>\n");
 	    	out.write("</gpx>\n");

 	    	out.close();
 	        
    	 }
    	 catch (IOException e) {
     	    //Log.e("salvaSesion", "Could not write file " + e.getMessage());
     	    return null;
     	 }
    	 

    	 // Borro los puntos
    	 SQLiteDatabase db = mOpenHelper.getWritableDatabase();
         db.delete(
         	EstructuraDB.Punto.TABLE_NAME,        
         	EstructuraDB.Punto.COLUMN_NAME_SESION + "=" + sesionID,
         	null                           
         );
		 //db.close();
		 
		 Sesion returnSesion = new Sesion();
		
		 
		 returnSesion.nomFicheroSesion = nomFicheroExport;
		 returnSesion.distancia = dist.longValue();
		 returnSesion.duracion =  timestampPunto - timestampPuntoInicio - tiempoPausadoTotal ;
		 returnSesion.altitudPos = altitudPos.longValue();
		 returnSesion.altitudNeg = altitudNeg.longValue();
		 returnSesion.sesionId = sesionID;
		 returnSesion.rating = estrellasSesion;
		 returnSesion.descripcion = descSesion;
		 
		 
		 return returnSesion ;
		
	}
	
	//pasa las sesiones a la SD desde la BBDD
	// sabe cuales no stan ya por que tienen el COLUMN_NAME_FICHERO a null
	// por si alguna sesion se habia quedado colgada en la BBDD al colgarse la app
	// por falta de bateria o lo que sea
	public void PasaSesionesSD() {
		
			
		
		// Opens the database object in "read" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                
		Cursor cSesiones =  db.query(
				 EstructuraDB.Sesion.TABLE_NAME, 
				 new String[] {	EstructuraDB.Sesion._ID}
		 		, EstructuraDB.Sesion.COLUMN_NAME_FICHERO + " is null", null, null, null, null
		 		);
		
		cSesiones.moveToFirst();
		
		if (cSesiones.getCount() == 0) {
			
			db.close();
			return;
		}
		
		
		do {
			
			long sesionID = cSesiones.getLong(0);
			
			Sesion laSesion = guardaSesionGPX(sesionID);
			
			ContentValues  valores = new ContentValues();
	        valores.put(EstructuraDB.Sesion.COLUMN_NAME_DISTANCIA, laSesion.distancia );
	        valores.put(EstructuraDB.Sesion.COLUMN_NAME_DURACION, laSesion.duracion );
	        valores.put(EstructuraDB.Sesion.COLUMN_ALTITUD_POS, laSesion.altitudPos );
	        valores.put(EstructuraDB.Sesion.COLUMN_ALTITUD_NEG, laSesion.altitudNeg );
	        valores.put(EstructuraDB.Sesion.COLUMN_NAME_FICHERO,laSesion.nomFicheroSesion);
	        
			db.update(EstructuraDB.Sesion.TABLE_NAME,valores,EstructuraDB.Sesion._ID + "=" + sesionID, null);
			
			
			
		}while(cSesiones.moveToNext());
	
		
		db.close();
			
		}
	
	
	// esta funcion serviv´´a para importar ficheros gpx normales a 
	// la BBDD y estructura de gpx de correpicos
	
	// por el momento l ausaremos para pasar las sesion viejas en gpx
	// al nuevo formato, leemos el gpx, escribimos ñla sesion en la BBDD 
	// y generamso un nuevo gpx
	public void RecuperaSesionesSD(String directorio, ProgressDialog dialogo) {
    	ImportaFicherosThread cargaParcialThread = new ImportaFicherosThread( directorio, dialogo );
        cargaParcialThread.start(); 	
	}
	
	//actualiza la sesion con la distancia y tiempototales al terminar
	public void RecuperaSesionesSDThread(String directorio, ProgressDialog dialogo) {
		
		
		if (directorio == "") {
			directorio = Environment.getExternalStorageDirectory()+ File.separator  + pathSesiones;
		}
		
		//recorro el directorio
		File dir = new File(directorio);
		
		dialogo.setMax( dir.listFiles().length );
		
		int i = 0;
		for (File fichGPX : dir.listFiles()) {
		    
			i++;
			dialogo.setProgress( i );
			
			if (".".equals(fichGPX.getName()) || "..".equals(fichGPX.getName())) {
		      continue;  // Ignore the self and parent aliases.
		    }
		    	        
	        //me preparo para leer el fichero
	        InputStream gpxreader;
	    	XmlPullParser parser = Xml.newPullParser();
	    	try {
				gpxreader = new FileInputStream(fichGPX);
				parser.setInput(gpxreader, null);
	    	} catch (FileNotFoundException e) {
				//e.printStackTrace();
				return;
			} catch (XmlPullParserException e) {
				//e.printStackTrace();
				return;
			}
	    	
	    	// y voy leyendo los puntos para sacar lo que me falta
	    	PuntoGPX elPuntoGPX =  RecuperaPuntoSesionGpx ( parser);
	    	
	    	Date laFecha;    
	        Calendar c = Calendar.getInstance();
	        c.setTimeInMillis( elPuntoGPX.timestamp );
	        laFecha = c.getTime();
	            	
	    	//INSERTO LA SESION
	        long sesionId =  insertaSesion(laFecha);

	        if (elPuntoGPX != null) {
	        
	        	do {    
	        		
	        		//TODO: Por si importo ficheros que no son ya de correpicos
	        		// controlar que por lo menos de cada punto tengo el minimo
	        		// necesario o calcular lo que me falte
	        		
	        		elPuntoGPX.sesionId = sesionId;
	        		
					insertaPunto(elPuntoGPX);
		        	
	        		elPuntoGPX =  RecuperaPuntoSesionGpx ( parser) ;
	        	
	        	} while ( elPuntoGPX != null   );
	        
	        
	        }
	        	
			// actualizo la sesion con los datos globales
			terminaSesion(sesionId);
			
		  }

	}
	
	private class ImportaFicherosThread extends Thread {

        private String m_directorio;
        private ProgressDialog m_dialogo;
        
        public ImportaFicherosThread(String Directorio, ProgressDialog dialogo ) {
        	m_directorio = Directorio;
        	m_dialogo = dialogo;
        }

        @Override
        public void run() {         
        	RecuperaSesionesSDThread( m_directorio, m_dialogo );
            handler.sendEmptyMessage(0);
        }

        private Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                
            	m_dialogo.dismiss();
            	
            }
        };

 }
		
	
	
}
