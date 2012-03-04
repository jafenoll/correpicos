package es.fenoll.javier;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class AlmacenDatos  {

	// Used for debugging and logging
    private static final String TAG = "AlmacenDatos";

	
	/**
     * The database that the provider uses as its underlying data store
     */
    // TODO: Hacer que la BBDD este en la SD y asi no ocupo memoria del telefono
    // ya que previsiblemente la BBDD puede crecer mucho
    private static final String DATABASE_NAME = "correcaminos.db";

    /**
     * The database version
     */
    private static final int DATABASE_VERSION = 10;
	
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
	           // calls the super constructor, requesting the default cursor factory.
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
	                   + EstructuraDB.Punto.COLUMN_NAME_TIEMPOTRANS + " DOUBLE,"
	                   + EstructuraDB.Punto.COLUMN_NAME_SESION + " INTEGER,"
	                   + EstructuraDB.Punto.COLUMN_NAME_PRECISION + " LONG"
	                   + ");");
	           
	           db.execSQL("CREATE TABLE " + EstructuraDB.Sesion.TABLE_NAME + " ("
	                   + EstructuraDB.Sesion._ID + " INTEGER PRIMARY KEY," 
	                   + EstructuraDB.Sesion.COLUMN_NAME_FECHA + " TEXT,"
	                   + EstructuraDB.Sesion.COLUMN_NAME_DISTANCIA + " LONG,"
	                   + EstructuraDB.Sesion.COLUMN_NAME_DURACION + " DOUBLE,"
	                   + EstructuraDB.Sesion.COLUMN_ALTITUD_POS + " INTEGER,"
	                   + EstructuraDB.Sesion.COLUMN_ALTITUD_NEG + " INTEGER"
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
	        		   + " VALUES (0,'correr',1,1);");
	        		   
	           
	           
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

	           
	           
	           /*
               db.execSQL("ALTER TABLE " + EstructuraDB.Sesion.TABLE_NAME
       		   		+ " ADD COLUMN " + EstructuraDB.Sesion.COLUMN_ALTITUD_POS + " INTEGER"
       		   		+ ";"
       		   );
               
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

	public long insertaPunto(ContentValues  valores) {
		
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
	public void terminaSesion(long rowid, double distancia, double duracion, int altitudPos, int altitudNeg) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		ContentValues  valores = new ContentValues();
        valores.put(EstructuraDB.Sesion.COLUMN_NAME_DISTANCIA, distancia );
        valores.put(EstructuraDB.Sesion.COLUMN_NAME_DURACION, duracion );
        valores.put(EstructuraDB.Sesion.COLUMN_ALTITUD_POS, altitudPos );
        valores.put(EstructuraDB.Sesion.COLUMN_ALTITUD_NEG, altitudNeg );
        
		db.update(EstructuraDB.Sesion.TABLE_NAME,valores,EstructuraDB.Sesion._ID + "=" + rowid, null);
		
		db.close();
	}
	
	// crea una sesion nueva y devuelve su rowid, automatimente pone la fecha de inicio
	public long insertaSesion() {
		
		// Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
          
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());

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
								EstructuraDB.Sesion.COLUMN_ALTITUD_NEG}
		 		, strQuery, null, null, null, strOrder
		 		);
		
	}
	
	/**
	 * Return a Cursor over the list of all puntos of a known sesion
     * 
     * @return Cursor over all puntos de una sesion
     */
	//TODO: hacer que los puntos se guarden en u fiechro en la SD
	// para no llenar la memoria del telefono
	// o valorar si con la BBDD en SD ya esta bien
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
						        EstructuraDB.Punto.COLUMN_NAME_TIEMPOTRANS,
						        EstructuraDB.Punto.COLUMN_NAME_PRECISION}
		 		, EstructuraDB.Punto.COLUMN_NAME_SESION + "=" + sesionId , null, null, null, null
		 		);
		
	}
	
	
	
	// borra una sesion y toos sus puntos dado du id
	public boolean borraSesion(long sesionId) {
			
		// Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        

        // Borro los puntos
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
        
		
		return true;
	}

	public class Deporte{
		
		public String nombre;
		public int gpsgapdist;
		public int gpsgaptmp;
		public boolean autopause;
		public Double umbralautopause;
		
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
	
	
}
