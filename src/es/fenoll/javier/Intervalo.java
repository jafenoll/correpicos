package es.fenoll.javier;

import android.os.Parcel;
import android.os.Parcelable;

//esta clase la hago Parcelable para poderla pasar 
	// como parametro en intent entre activities
	public class Intervalo implements Parcelable {
		
		
		public String etiqueta;
		public Long duracion;
		public String unidad;
		
				
		public static final Parcelable.Creator<Intervalo> CREATOR = new Parcelable.Creator<Intervalo>() {
			public Intervalo  createFromParcel(Parcel in) {
				return new Intervalo(in);
			}	

			public Intervalo[] newArray(int size) {
				return new Intervalo[size];
			}
		};
		
		public Intervalo(String letiqueta, Long lduracion, String lunidad) {
			this.etiqueta = letiqueta;
			this.duracion = lduracion;
			this.unidad = lunidad;
		}
		
		

		
		public Intervalo(Parcel in) {
			unidad = in.readString();
			duracion = in.readLong();
			etiqueta = in.readString();
	     }


		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel out, int arg1) {
			
			 out.writeString(etiqueta);
			 out.writeLong(duracion);
			 out.writeString(unidad);

			
		}
		
	}
