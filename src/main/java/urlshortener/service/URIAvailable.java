package urlshortener.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class to check if a URI is reachable.
 */
@Service
public class URIAvailable {

    /**
     * Returns true if and only if the uri is reachable, false in other cases.
     * @param uri The uri to check
     * @return True if the uri is reachable, false in other cases.
     */
    public boolean isURIAvailable(@NonNull String uri){
        // If the response is not 2XX Http response, then will we assume it's unreachable.
        return (getURIResponseGet(uri)/100) == 2;
    }

    /**
     * Make a get request to the uri and check if it is reachable or not.
     * If it is reachable, it returns 1. Otherwise it returns -1.
     * @param uri The uri to check if it is reachable
     * @return 1 if is reachable, -1 if not.
     */
    private static int getURIResponseGet(@NonNull String uri){
        try{
            // First we create and open the http connection
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(uri).openConnection();
            // We make a request only to get the header
            httpConnection.setRequestMethod("HEAD");
            // We return the code that has arrived from the request.
            return httpConnection.getResponseCode();
        }catch(Exception e){
            // In case of error, we return -1
            //System.out.println(e);
            return -1;
        }
    }
}
