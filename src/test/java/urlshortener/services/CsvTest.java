package urlshortener.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import urlshortener.repository.ShortURLRepository;
import urlshortener.repository.impl.ShortURLRepositoryImpl;
import urlshortener.service.CSVConverter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.JDBCType;


import urlshortener.service.ClickService;
import urlshortener.service.ShortURLService;
import urlshortener.service.URIAvailable;
import urlshortener.web.UrlShortenerController;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CsvTest {

    private URIAvailable availableURI = new URIAvailable();

    @Autowired
    private CSVConverter csvCon;

    @Value("${converter.test.1:classpath:csv/Test1.csv}")
    private Resource test1;
    @Value("${converter.test.1:classpath:csv/Test2.csv}")
    private Resource test2;
    @Value("${converter.test.1:classpath:csv/Test3.csv}")
    private Resource test3;
    @Value("${converter.test.1:classpath:csv/Test4.csv}")
    private Resource test4;


    public static final String SEPARATOR=",";
    private String[] datos = null;

    @Test
    public void numUrisCorrectas() throws IOException { //Test 1
            int total;
            csvCon.CalcularTotal(new InputStreamReader(test1.getInputStream(), StandardCharsets.UTF_8));
            total = csvCon.total();
            csvCon.ConverterCSV(new InputStreamReader(test1.getInputStream(), StandardCharsets.UTF_8));


            assertEquals(total,8);
            assertEquals(csvCon.acortadas(), 5);

            String filename = "src/main/resources/static/csv/Salida_Test1.csv";
            File file = new File(filename);
            file.delete();
    }

    @Test
    public void enlacesAcortados() throws IOException { //Primer enlace ok, segundo error. Test 2

            csvCon.ConverterCSV(new InputStreamReader(test2.getInputStream(), StandardCharsets.UTF_8));
            csvCon.guardar("Test2.csv");

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("src/main/resources/static/csv/Salida_Test2.csv"), StandardCharsets.UTF_8));

            String line = br.readLine();
            datos = line.split(SEPARATOR);
            assert availableURI.isURIAvailable(datos[0]);
            line = br.readLine();
            datos = line.split(SEPARATOR);
            assert !availableURI.isURIAvailable(datos[0]);
            String filename = "src/main/resources/static/csv/Salida_Test2.csv";
            File file = new File(filename);
            file.delete();



    }

    @Test
    public void ficheroVacio() throws IOException { //"fichero vacio" Test3


            csvCon.ConverterCSV(new InputStreamReader(test3.getInputStream(), StandardCharsets.UTF_8));
            csvCon.guardar("Test3.csv");

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("src/main/resources/static/csv/Salida_Test3.csv"), StandardCharsets.UTF_8));

            String line = br.readLine();
            datos = line.split(SEPARATOR);
            assertEquals(datos[0],"Fichero vacío");
            String filename = "src/main/resources/static/csv/Salida_Test3.csv";
            File file = new File(filename);
            file.delete();
    }

    @Test
    public void MasArgumentosPorLinea() throws IOException { // En la primera linea hay mas argumentos aparte del enlace. Acorta el enlace correctamente.
            int total;
            csvCon.CalcularTotal(new InputStreamReader(test4.getInputStream(), StandardCharsets.UTF_8));
            total = csvCon.total();
            csvCon.ConverterCSV(new InputStreamReader(test4.getInputStream(), StandardCharsets.UTF_8));
            csvCon.guardar("Test4.csv");

            assertEquals(total,2);
            assertEquals(csvCon.acortadas(), 2);

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("src/main/resources/static/csv/Salida_Test4.csv"), StandardCharsets.UTF_8));

            String line = br.readLine();
            datos = line.split(SEPARATOR);
            assert availableURI.isURIAvailable(datos[0]);
            String filename = "src/main/resources/static/csv/Salida_Test4.csv";
            File file = new File(filename);
            file.delete();

    }


}