package urlshortener.services;

import org.springframework.beans.factory.annotation.Autowired;
import urlshortener.repository.ShortURLRepository;
import urlshortener.service.CSVConverter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import urlshortener.service.ClickService;
import urlshortener.service.ShortURLService;
import urlshortener.service.URIAvailable;
import urlshortener.web.UrlShortenerController;

import static org.junit.Assert.*;

public class CsvTest {
/*
    @Autowired
    private ShortURLRepository shortURLRepository;

    @Autowired
    private CSVConverter csvCon;

    @Autowired
    private URIAvailable availableURI;
    public static final String SEPARATOR=",";
    private String[] datos = null;
    //ShortURLService s1;
    //ClickService s2;
    //private UrlShortenerController controller = new UrlShortenerController(s1, s2, csvCon);

    @Test
    public void numUrisCorrectas() { //Test 1
        try {

            int total;
            csvCon.CalcularTotal(new InputStreamReader
                    (new FileInputStream("src/test/resources/csv/Test1.csv"), StandardCharsets.UTF_8));
            total = csvCon.total();
            csvCon.ConverterCSV(new InputStreamReader
                    (new FileInputStream("src/test/resources/csv/Test1.csv"), StandardCharsets.UTF_8));


            assertEquals(total,8);
            assertEquals(csvCon.acortadas(), 5);


        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void enlacesAcortados() { //Primer enlace ok, segundo error. Test 2
        try {

            csvCon.ConverterCSV(new InputStreamReader
                    (new FileInputStream("src/test/resources/csv/Test2.csv"), StandardCharsets.UTF_8));
            //csvCon.guardar();

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("src/main/resources/static/csv/Salida.csv"), StandardCharsets.UTF_8));

            String line = br.readLine();
            datos = line.split(SEPARATOR);
            assert availableURI.isURIAvailable(datos[1]);
            line = br.readLine();
            datos = line.split(SEPARATOR);
            assert !availableURI.isURIAvailable(datos[1]);


        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void ficheroVacio() { //"fichero vacio" Test3
        try {

            csvCon.ConverterCSV(new InputStreamReader
                    (new FileInputStream("src/test/resources/csv/Test3.csv"), StandardCharsets.UTF_8));
            //csvCon.guardar();

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("src/main/resources/static/csv/Salida.csv"), StandardCharsets.UTF_8));

            String line = br.readLine();
            datos = line.split(SEPARATOR);
            assertEquals(datos[0],"Fichero vacío");



        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void MasArgumentosPorLinea() { // En la primera linea hay mas argumentos aparte del enlace. Acorta el enlace correctamente.
        try {

            int total;
            csvCon.CalcularTotal(new InputStreamReader
                    (new FileInputStream("src/test/resources/csv/Test4.csv"), StandardCharsets.UTF_8));
            total = csvCon.total();
            csvCon.ConverterCSV(new InputStreamReader
                    (new FileInputStream("src/test/resources/csv/Test4.csv"), StandardCharsets.UTF_8));


            assertEquals(total,2);
            assertEquals(csvCon.acortadas(), 2);

            //csvCon.guardar();

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("src/main/resources/static/csv/Salida.csv"), StandardCharsets.UTF_8));

            String line = br.readLine();
            datos = line.split(SEPARATOR);
            assert availableURI.isURIAvailable(datos[1]);



        } catch(IOException e) {
            e.printStackTrace();
        }
    }

*/
}