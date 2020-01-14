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
import java.util.Calendar;
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

    /**
     * This test checks whether access to the statistics of an unreachable uri returns an error404
     * @throws IOException
     */
    @Test
    public void linkNotAvailableReturnError() throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        // Create the request to the uri
        Request request = new Request.Builder()
                .url("http://localhost:" + port + "/asd6/stats")
                .build();
        Response response = client.newCall(request).execute();

        assert response.request().url().pathSegments().get(0).equals("error404.html");
    }

    /**
     * This test verifies that the stats request of a uri that has never been visited returns 0 visits
     * @throws IOException
     */
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

    /**
     * This test verifies that the request for statistics of a uri that has been visited correctly
     * returns the statistics
     * @throws IOException
     */
    @Test
    public void statsNotEmptyReturnData() throws IOException {

        // Register a new click from localhost
        clickService.saveClick("f656", "127.0.0.1", "Spain", "Debug",
                new Date(Calendar.getInstance().getTime().getTime()));

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

    /**
     * This test checks that the statistics request of a uri
     * that has the cache stored is faster than if the cache is removed
     * @throws IOException
     */
    @Test
    public void statsNotEmptyReturnDataWithCacheIsFaster() throws IOException {
        // Register a new click from localhost
        clickService.saveClick("f656", "127.0.0.1", "Spain", "Debug",
                new Date(Calendar.getInstance().getTime().getTime()));

        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        // Create the request to the uri
        Request request = new Request.Builder()
                .url("http://localhost:" + port + "/f656/stats")
                .build();

        // We call a petition
        long timeBefore = System.currentTimeMillis();
        Response response = client.newCall(request).execute();
        long timeWithoutCache = System.currentTimeMillis() - timeBefore;

        // We call a new petition
        timeBefore = System.currentTimeMillis();
        response = client.newCall(request).execute();
        long timeWithCache = System.currentTimeMillis() - timeBefore;

        assert timeWithoutCache > timeWithCache;

    }



}
