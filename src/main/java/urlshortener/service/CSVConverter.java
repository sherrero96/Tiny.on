package urlshortener.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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

    @Value("${converter.1:classpath:csv}")
    private Resource path;

    /**
     * Separator of strings in CSV file.
     */
    public static final String SEPARATOR=",";

    /**
     * Variables to initialize
     */
    private URIAvailable availableURI = new URIAvailable();

    @Autowired
    private ShortURLService uris;

    /**
     * Variables to save the result
     */
    private HashMap<String,String> resultados;


    /**
     * Counters to know the number of uri's in a file
     */
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

    /**
     * Calculate the total number of uri's inside the file and save it in local variable "uriTotal"
     * @param nameFile
     * @throws IOException
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

    /**
     * Returns the total number of uri's
     * @return uriTotal
     */
    public int total (){
        return uriTotal;
    }

    /**
     * Returns the total number of short uri's
     * @return uriTotal
     */
    public int acortadas (){
        return uriCorrectas;
    }

    /**
     * Return HashMap with uri's and your short uri's.
     *      If uri not available, it don't have short uri.
     * @param nameFile is name of csv file.
     * @return hashmap with the result of converter CSV file.
     */
    public HashMap<String,String> ConverterCSV(@NonNull InputStreamReader nameFile){
        BufferedReader br = null;

        String[] datos = null;
        String line = "";
        ShortURL link;
        uriCorrectas = 0;
        resultados = new LinkedHashMap<String,String>();

        try {

            br = new BufferedReader(nameFile);

            if((line = br.readLine()) != null) {

                // use comma as separator
                datos = line.split(SEPARATOR);
                System.out.println(datos);

                if (availableURI.isURIAvailable(datos[0])) {
                    link = uris.save(datos[0], "Twitter", "127.0.0.1");
                    availableURI.saveURI(datos[0]);
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
                    availableURI.saveURI(datos[0]);
                    resultados.put(datos[0],link.getUri().toString());
                    uriCorrectas++;
                }
                else{
                    resultados.put(datos[0], "URI NO AVAILABLE");
                }
            }
            return resultados;

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

    /**
     * Save the hashmap stored in local variable "resultados" in a file in the database with the name "name".
     * @param name is name of file.
     * @throws IOException
     */
    public void guardar(String name) {
        try{
            System.out.println(name);
            String nombreFichero = "src/main/resources/static/csv/Salida_" + name ;
            File file = new File(nombreFichero);
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
            boolean p = true;
            if(!file.exists()){
                p = file.createNewFile();
            }

            if(p){
                FileWriter fw = new FileWriter(file);
                for(Map.Entry<String,String> entry : resultados.entrySet()){

                    fw.append(entry.getKey()).append(", ").append(entry.getValue()).append("\n");
                }
                fw.close();
            }else{
                System.err.println("No se ha podido abrir el fichero de escritura");
            }
        } catch (IOException e) {
            System.out.print("ERROR! ");
            e.printStackTrace();
        }
    }

    /**
     * Returns an integer that indicates if the process of converting uri's of csv file
     *      in short uri's has been correct and store the result in ResultFile in the database.
     * @param nameFile is InputStream of CSV file for reader its content.
     * @param name is name of File
     * @return -1 in case of error. Return result > -1 in otherwise.
     * @throws IOException
     */
    public int escalable(InputStreamReader nameFile, String name) throws IOException, IllegalArgumentException {
        BufferedReader br = null;

        String[] datos = null;
        String line = "";
        ShortURL link;
        uriCorrectas = 0;
        resultados = new LinkedHashMap<String, String>();

        String nombreFichero = "src/main/resources/static/csv/Salida_" + name;
        File file = new File(nombreFichero);
        boolean p = file.createNewFile();
        FileWriter fw = new FileWriter(file);
        try {
            br = new BufferedReader(nameFile);

            if ((line = br.readLine()) != null) {
                // use comma as separator
                datos = line.split(SEPARATOR);

                if (availableURI.isURIAvailable(datos[0])) {
                    link = uris.save(datos[0], "Twitter", "127.0.0.1");
                    availableURI.saveURI(datos[0]);

                    String prueba = datos[0] + ", " + link.getUri().toString() + "\n";
                    fw.append(prueba);

                    uriCorrectas++;
                } else {
                    String prueba2 = datos[0] + ", " + "URI NO AVAILABLE" + "\n";
                    fw.append(prueba2);
                }
            } else {
                String prueba3 = "Fichero vacio" + "\n";
                fw.append(prueba3);
            }

            while ((line = br.readLine()) != null) {

                // use comma as separator
                datos = line.split(SEPARATOR);
                if (availableURI.isURIAvailable(datos[0])) {

                    link = uris.save(datos[0], "Twitter", "127.0.0.1");
                    availableURI.saveURI(datos[0]);

                    String prueba = datos[0] + ", " + link.getUri().toString() + "\n";
                    fw.append(prueba);

                    uriCorrectas++;


                } else {
                    String prueba2 = datos[0] + ", " + "URI NO AVAILABLE" + "\n";
                    fw.append(prueba2);

                }

            }
            fw.close();
            return uriCorrectas;

        } catch (IllegalArgumentException e) {
            String prueba2 = datos[0] + ", " + "URI NO AVAILABLE" + "\n";
            fw.append(prueba2);
            return 0;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
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

}