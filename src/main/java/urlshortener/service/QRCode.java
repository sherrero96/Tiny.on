package urlshortener.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class QRCode {

    private static final String QR_API = "https://api.qrserver.com/v1/create-qr-code";

    public static String getQRCode(@NonNull String uri_shortened) {
        /*URL url = new URL(QR_API);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        // Send data (URL-shortened) to the server API
        con.setDoOutput(true);

        Map<String, String> paramether = new HashMap<>();
        paramether.add("data", uri_shortened);
        paramether.add("size", "100x100"); // By default 100x100 pixels QR image*/
        

        return QR_API +  "/?data=" + uri_shortened;
    }
}