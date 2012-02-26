package es.fenoll.javier;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class FormatosDisplay {
	
	/** Obtiene las decimas de segundo de los milisegundos */
	public String desdeMSobtenSS( long time) {
		
		long secs = (long)(time/1000);
		secs = secs % 60;
		
		NumberFormat formatter = new DecimalFormat("00");
				
		return ":" + formatter.format(secs);
	
		
	}
	
	/** Pasa de milisegundos a horas:minutos:segundos  */
	public String desdeMStoHHMM( long time) {
		
		//int horasTranscurridas = (int )( tiempoMS/3600000 );
		//int minutosTranscuridos = (int) ( (tiempoMS - horasTranscurridas * 3600000) / 60000 );
		//long segundosTranscurridos = (int)( (tiempoMS - horasTranscurridas * 3600000- minutosTranscuridos * 60000 ) /1000 );
			
		
		long mins = (long)((time/1000)/60);
		long hrs = (long)(((time/1000)/60)/60);
		
		
		mins = mins % 60;
		
		NumberFormat formatter = new DecimalFormat("00");
		return  hrs + ":" + formatter.format(mins) ;
				
		
	}
	
	/** Pasa de m/s a km/h */
	public String desdeMsaKh( double velocidad) {
		
		NumberFormat formatter = new DecimalFormat("#.#");
		
		return formatter.format(  (double)(velocidad * 3.6) );
	}
	
	/** Pasa de m/s minutos por kilometro */
	public String desdeMsaMKM(double velocidad) {
		
		double minPorKm = (1000 / velocidad ) / 60;
		
		long min = (long)minPorKm;
		double seg = minPorKm-min;
		
		seg = seg * 60;
		
		NumberFormat formatter = new DecimalFormat("00");
		
		return min + ":" + formatter.format( seg );
	}
	

	
	
}
