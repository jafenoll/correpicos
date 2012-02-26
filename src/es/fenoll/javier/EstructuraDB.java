package es.fenoll.javier;

import android.provider.BaseColumns;

public final class EstructuraDB {

	// This class cannot be instantiated
    private EstructuraDB() {
    }

    public static final class Sesion implements BaseColumns {
    	
    	// This class cannot be instantiated
        private Sesion() {}

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "sesiones";
        
        /*
         * Column definitions
         */
        /**
         * Column name for the sequence number of the point within the sesion
         * <P>Type: TEXT</P>  por que SsqLite no tiene data, pero con esta va bien, ver documentacion de la BBDD
         */
        public static final String COLUMN_NAME_FECHA = "fecha";
        
        /**
         * Column name for the total distance in meters 
         * <P>Type: LONG</P>  
         */
        public static final String COLUMN_NAME_DISTANCIA = "dist";
        
        /**
         * Column name for the duration in ms
         * <P>Type: DOUBLE</P>   
         */
        public static final String COLUMN_NAME_DURACION = "duracion";
        
       
        public static final String COLUMN_ALTITUD_POS = "altitudpos";
        
        public static final String COLUMN_ALTITUD_NEG = "altitudneg";
        
    	
    	
    }
    
    
 public static final class Deportes implements BaseColumns {
    	
    	// This class cannot be instantiated
        private Deportes() {}

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "deportes";
        
        /*
         * Column definitions
         */
        /**
         * Column name for the sequence number of the point within the sesion
         * <P>Type: TEXT</P>  por que SsqLite no tiene data, pero con esta va bien, ver documentacion de la BBDD
         */
        public static final String COLUMN_NAME_NOMBRE = "nombre";
        
        /**
         * Column name for the total distance in meters 
         * <P>Type: LONG</P>  
         */
        public static final String COLUMN_NAME_GPS_GAP_DIST = "gpsgapdist";
        
        /**
         * Column name for the duration in ms
         * <P>Type: DOUBLE</P>   
         */
        public static final String COLUMN_NAME_GPS_GAP_TIEMPO = "gpsgaptmp";
        
       
        public static final String COLUMN_NAME_AUTOPAUSE = "autopause";
        
        
        public static final String COLUMN_NAME_UMBRAL_AUTOPAUSE = "umbralautpa";
        
    	
    	
    }
    
    public static final class Punto implements BaseColumns {

        // This class cannot be instantiated
        private Punto() {}

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "puntos";
        
        /*
         * Column definitions
         */

        /**
         * Column name for the sequence number of the point within the sesion
         * <P>Type: INTEGER</P>
         */
        public static final String COLUMN_NAME_SECUENCIA = "secuencia";
        
        /**
         * Column name for the latitude of the point
         * <P>Type: DOUBLE</P>
         */
        public static final String COLUMN_NAME_LAT = "lat";
        
        /**
         * Column name for the  longitude of the point
         * <P>Type: DOUBLE</P>
         */
        public static final String COLUMN_NAME_LONG = "long";
        
        /**
         * Column name for the speed in km/h  of the point
         * <P>Type: DOUBLE</P>
         */
        public static final String COLUMN_NAME_VELOCIDAD = "velocidad";
        
        /**
         * Column name for the altitude in meters of the point
         * <P>Type: INTEGER</P>
         */
        public static final String COLUMN_NAME_ALTITUD = "alt";
        
        /**
         * Column name for the distance up to this point
         * <P>Type: INTEGER</P>
         */
        public static final String COLUMN_NAME_DISTANCIA = "dist";
        
        /**
         * Column name for the time elapsed since start in ms
         * <P>Type: INTEGER</P>
         */
        public static final String COLUMN_NAME_TIEMPOTRANS = "tiempotrans";
        
        /**
         * Column name for the time elapsed since start in ms
         * <P>Type: INTEGER</P>
         */
        public static final String COLUMN_NAME_SESION = "sesion";
        
        /**
         * Column name for the tposition acuracy
         * <P>Type: FLOAT</P>
         */
        public static final String COLUMN_NAME_PRECISION = "precision";

    }
}
