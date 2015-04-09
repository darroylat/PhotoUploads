package cl.arroyo.daniel.camara;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by darroyo on 08-04-2015.
 */
public class MetodosCamara {


    public String fechaCompleta(){
        Date fechaActual = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSSS");
        String cadenaFecha = formato.format(fechaActual);
        return cadenaFecha;
    }



}
