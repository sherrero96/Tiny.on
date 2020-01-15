package urlshortener.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import urlshortener.repository.ShortURLRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class to check if a URI is reachable.
 */
@Service
@EnableScheduling
public class URIAvailable {

    // List of error codes admited by the checker
    private static final int[] errorCodesOK= {200, 201, 204};

    // Time between check and check
    private static final int TIME_URI_CHECK = 3000;

    // Timeout get petition
    private static final int TIME_GET = 1000;

    // Number of failed requests allowed before delete
    private static final int ALLOWED_FAILS = 1;

    @Autowired
    private ShortURLRepository shortURLRepository;

    private ConcurrentHashMap<String, AtomicInteger> map = new ConcurrentHashMap<>();

    /**
     * Update the URI fault structure
     * If the number of faults exceeds the permitted number, it will be removed from the
     */
    @Scheduled(fixedRate = TIME_URI_CHECK)
    public void checkUris(){
        // Iterate over all the structure
        for(Map.Entry<String, AtomicInteger> entry : map.entrySet()){
            // If the Uri is not reachable
            if(!checkUriAvailable(entry.getKey())){
                // If the Uri is not available, increment one unit the fails
                entry.getValue().incrementAndGet();
                // If the number of errors is the allowed one, it is removed from the database
                if(entry.getValue().get() > ALLOWED_FAILS){
                    map.remove(entry.getKey());
                }
            // The URI is reachable
            }else{
                entry.setValue(new AtomicInteger(0));
            }
        }
    }

    /**
     * Save the uri in the hashmap for check
     * @param uri the uri to register in the hashmap
     */
    public void saveURI(String uri){
        if(!map.containsKey(uri) && checkUriAvailable(uri)){
            map.put(uri, new AtomicInteger(0));
        }
    }

    /**
     * Check if the uri is available, searching in the hashmap or get petition
     * @param uri the uri to check if is available
     * @return true if is available or false if not
     */
    public boolean isURIAvailable(String uri){
        boolean isAvailable = false;
        if(map.containsKey(uri)){
            isAvailable =  (map.get(uri).get() == 0);
        }else{
            isAvailable = checkUriAvailable(uri);
        }
        return isAvailable;
    }

    /**
     * Returns true if and only if the uri is reachable, false in other cases.
     * @param uri The uri to check
     * @return True if the uri is reachable, false in other cases.
     */
    private boolean checkUriAvailable(@NonNull String uri){
        int response = getURIResponseGet(uri);
        for (int value : errorCodesOK) {
            if (response == value) {
                return true;
            }
        }

        return false;
    }

    /**
     * Make a get request to the uri and check if it is reachable or not.
     * If it is reachable, it returns 1. Otherwise it returns -1.
     * @param uri The uri to check if it is reachable
     * @return 1 if is reachable, -1 if not.
     */
    private int getURIResponseGet(@NonNull String uri){
        // OKhttpClient to get the petition, with a timeout
        UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" });
        if(!urlValidator.isValid(uri)){
            return -1;
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(TIME_GET, TimeUnit.MILLISECONDS)
                .build();

        // Create the request to the uri
        Request request = new Request.Builder()
                    .url(uri)
                    .build();
        try {
            Response response = client.newCall(request).execute();
            return response.code();
        }catch (Exception e) {
            return -1;
        }
    }

}
