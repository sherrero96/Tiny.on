package urlshortener.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;


@Service
public class QRCode {

    private static final String URL_QR_API = "https://api.qrserver.com/v1/create-qr-code/";

    /**
     * Return qr image of short url as byte stream
     */
    public InputStream getQRImageAsStream(@NonNull String short_url) {
        try {
            String uri = UriComponentsBuilder.fromHttpUrl(URL_QR_API)
                    .queryParam("data", short_url)
                    .queryParam("size", "100x100")
                    .queryParam("format", "png").toUriString();

            HttpRequestBase httpReq = new HttpGet(uri);
            HttpClient httpclient = HttpClientBuilder.create().build();
            HttpResponse response = httpclient.execute(httpReq);
            HttpEntity responseEntity = response.getEntity();

            return responseEntity.getContent();
        } catch(IOException e){
            e.printStackTrace();
            return null;
        }

    }
}
