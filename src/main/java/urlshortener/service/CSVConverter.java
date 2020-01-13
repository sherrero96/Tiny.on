package urlshortener.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import urlshortener.domain.ShortURL;

import urlshortener.repository.ShortURLRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


// El cliente trata el fichero y envia un string o una cadena de caracteres simplemente, el servidor trata esa cadena y envia otra.
//      Y nuevamente el cliente trata la cadena y forma el fichero.

/**
 * Class to converter CSV.
 */
@Service
public class CSVConverter {

    public static final String SEPARATOR=",";
    private URIAvailable availableURI = new URIAvailable();
    private final ShortURLRepository shortURLRepository;
    private ShortURLService uris=null;
    private HashMap<String,String> resultados;

    public CSVConverter(ShortURLRepository shortURLRepository) {
        this.shortURLRepository = shortURLRepository;
    }

    private int uriTotal = 0;
    private int uriCorrectas = 0;

    /**
     FORMATO CORRECTO CSV:
     URI
     URI
     ...
     FORMATO CSV DEVUELTO:
     URI, ShortURI
     URI, ShortURI
     */
    public void CalcularTotal(@NonNull InputStreamReader nameFile) throws IOException {
        int cuenta = 0;
        String line = "";
        BufferedReader file = null;
        file = new BufferedReader(nameFile);
        try {
            while (file.readLine() != null){
                cuenta = cuenta + 1;
            }
            uriTotal = cuenta;

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (file != null) {
                try {
                    uriTotal = cuenta;
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public int total (){
        return uriTotal;
    }

    public int acortadas (){
        return uriCorrectas;
    }

    public HashMap<String,String> ConverterCSV(@NonNull InputStreamReader nameFile){
        BufferedReader br = null;

        String[] datos = null;
        String line = "";
        ShortURL link;
        uriCorrectas = 0;
        resultados = new LinkedHashMap<String,String>();

        uris = new ShortURLService(this.shortURLRepository);
        try {

            br = new BufferedReader(nameFile);

            //CalcularTotal(br2);
            //fw = new FileWriter("./csv/Salida.csv"); Las lineas comentadas son para otra funcion de pasar ED al fichero CSV
            if((line = br.readLine()) != null) {

                // use comma as separator
                datos = line.split(SEPARATOR);


                if (availableURI.isURIAvailable(datos[0])) {
                    // resultado = llamada al recortador y que nos devuelva aqui el resultado uris.save(datos[0]);
                    link = uris.save(datos[0], "Twitter", "127.0.0.1");
                    resultados.put(datos[0], link.getUri().toString());
                    uriCorrectas++;


                } else {
                    resultados.put(datos[0], "URI NO AVAILABLE");


                }
            }
            else{
                resultados.put("Fichero vacío","");
            }

            while ((line = br.readLine()) != null) {

                // use comma as separator
                datos = line.split(SEPARATOR);


                if(availableURI.isURIAvailable(datos[0])){
                    // resultado = llamada al recortador y que nos devuelva aqui el resultado uris.save(datos[0]);
                    link = uris.save(datos[0], "Twitter", "127.0.0.1");
                    resultados.put(datos[0],link.getUri().toString());
                    uriCorrectas++;


                }
                else{
                    resultados.put(datos[0], "URI NO AVAILABLE");


                }

            }
            return resultados; //Hacer estructura de datos con los resultados, uris totales y uris correctas.


        }catch (IOException e) {
            e.printStackTrace();
            return resultados;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public File guardar(String name) throws IOException {
        System.out.println(name);
        String nombreFichero = "src/main/resources/static/csv/Salida_" + name + ".csv";
        //File file = new File("src/main/resources/static/csv/Salida.csv");
        File file = new File(nombreFichero);
        boolean p = file.createNewFile();
        if(p){

            FileWriter fw = new FileWriter(file);
            for(Map.Entry<String,String> entry : resultados.entrySet()){


                //fw.write("Adios");
                fw.append(entry.getKey()).append(", ").append(entry.getValue()).append("\n");
            }
            fw.close();
        }

        return file;
    }

    @RequestMapping(value = "/{id:(?!link|index).*}/csvEstado", method = RequestMethod.GET)
    public String obtainCsv(HttpServletRequest request, HttpServletResponse response) {
        return "forward:/static/csvEstado.html";
    }

}