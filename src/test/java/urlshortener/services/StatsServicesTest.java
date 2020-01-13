package urlshortener.services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import urlshortener.domain.ShortURL;
import urlshortener.repository.ShortURLRepository;
import urlshortener.service.ClickService;
import urlshortener.service.StatsService;

import java.io.IOException;
import java.net.URI;
import java.sql.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StatsServicesTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ShortURLRepository shortURLRepository;

    @Autowired
    private ClickService clickService;

    @Autowired
    private StatsService statsService;

    @Before
    public void setup(){
        shortURLRepository.save(new ShortURL("f656", "https://www.google.es", null, null,
                null, null, null, null, null,
                null));
    }

    @Test
    public void statsEmptyReturnEmptyData() throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        // Create the request to the uri
        Request request = new Request.Builder()
                .url("http://localhost:" + port + "/f656/stats")
                .build();
        Response response = client.newCall(request).execute();

        // Check if the code is 200 but they have no visit
        assert  response.code() == 200;
        assert Objects.equals(response.request().url().queryParameter("number"), "0");
    }

    @Test
    public void statsNotEmptyReturnData() throws IOException, InterruptedException {
        Thread.sleep(clickService.TIME_UPDATE_CACHE);   // Now the cache is clean

        // Register a new click from localhost
        clickService.saveClick("f656", "127.0.0.1", "Spain", "Debug");

        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        // Create the request to the uri
        Request request = new Request.Builder()
                .url("http://localhost:" + port + "/f656/stats")
                .build();
        Response response = client.newCall(request).execute();

        // Check if the code is 200 and they have one visit
        assert  response.code() == 200;
        assert Objects.equals(response.request().url().queryParameter("number"), "1");
    }

    @Test
    public void statsNotEmptyReturnDataWithCache() throws IOException, InterruptedException {
        Thread.sleep(clickService.TIME_UPDATE_CACHE);   // Now the cache is clean

        // Register a new click from localhost
        clickService.saveClick("f656", "127.0.0.1", "Spain", "Debug");

        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        // Create the request to the uri
        Request request = new Request.Builder()
                .url("http://localhost:" + port + "/f656/stats")
                .build();
        // We call a petition
        Response response = client.newCall(request).execute();
        // Now register a new click from localhost immediately
        clickService.saveClick("f656", "127.0.0.1", "Spain", "Debug");
        response = client.newCall(request).execute();

        // Because the cache expire in TIME_UPDATE_CACHE seconds, the result is not update..
        assert  response.code() == 200;
        assert Objects.equals(response.request().url().queryParameter("number"), "2");

        Thread.sleep(clickService.TIME_UPDATE_CACHE);

        // Now before a time, call a new response
        response = client.newCall(request).execute();
        assert  response.code() == 200;
        assert Objects.equals(response.request().url().queryParameter("number"), "3");


    }



}
