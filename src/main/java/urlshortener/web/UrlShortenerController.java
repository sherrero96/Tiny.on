package urlshortener.web;

import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import urlshortener.domain.ShortURL;
import urlshortener.service.ClickService;
import urlshortener.service.QRCodeService;
import urlshortener.service.ShortURLService;
import urlshortener.service.URIAvailable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@RestController
public class UrlShortenerController {
    private final ShortURLService shortUrlService;

    private final ClickService clickService;

    @Autowired
    private URIAvailable availableURI = new URIAvailable(); // To check if a URI is reachable

    @Autowired
    private QRCodeService qrCode = new QRCodeService();

    public UrlShortenerController(ShortURLService shortUrlService, ClickService clickService) {
        this.shortUrlService = shortUrlService;
        this.clickService = clickService;
    }

    @RequestMapping(value = "/{id:(?!link|index|stats).*}", method = RequestMethod.GET)
    public ResponseEntity<?> redirectTo(@PathVariable String id, HttpServletRequest request) {
        ShortURL l = shortUrlService.findByKey(id);
        if(l != null){
            if(availableURI.isURIAvailable(l.getTarget())){
                // Obtain all the information about the request and save in the DB
                clickService.saveClick(id, extractIP(request), extractCountry(request),
                        extractPlatform(request));
                return createSuccessfulRedirectToResponse(l);
            }else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/qr", method = RequestMethod.GET)
    public void qr(@RequestParam("id") String id, HttpServletResponse response) throws IOException {
        ShortURL l = shortUrlService.findByKey(id);
        if (l != null && availableURI.isURIAvailable(l.getTarget())) {
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            InputStream in = qrCode.getQRImage(baseUrl + '/' + id);
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            IOUtils.copy(in, response.getOutputStream());
        }
    }

    @RequestMapping(value = "/link", method = RequestMethod.POST)
    public ResponseEntity<ShortURL> shortener(@RequestParam("url") String url,
            @RequestParam(value = "sponsor", required = false) String sponsor, HttpServletRequest request) {
        UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" });

        // If the uri is valid and reachable, it is shortened.
        if (urlValidator.isValid(url) && availableURI.isURIAvailable(url)) {
            // Save the state in the available
            availableURI.saveURI(url);
            ShortURL su = shortUrlService.save(url, sponsor, request.getRemoteAddr());
            HttpHeaders h = new HttpHeaders();
            h.setLocation(su.getUri());
            return new ResponseEntity<>(su, h, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Return the remote ip address of the request
     * @param request The user request
     * @return The remote ip address
     */
    private String extractIP(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    /**
     * Return the country of the request, using a API geoLocation
     * @param request The request
     * @return string with the country of the request
     */
    private String extractCountry(HttpServletRequest request){
        HttpRequestBase httpReq = new HttpGet("http://ip-api.com/json/"+request.getRemoteAddr());
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpResponse response = null;
        String result = "Unknown";
        try {
            response = httpclient.execute(httpReq); // Execute the petition
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity != null){
                String retSrc = EntityUtils.toString(responseEntity);
                // Parse json
                JSONObject json = new JSONObject(retSrc); //Convert String to JSON Object
                result = json.getString("country");
            }
        } catch (IOException | JSONException e) {
            //e.printStackTrace();
        }
        return result;
    }

    private String extractPlatform(HttpServletRequest request){
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        return userAgent.getOperatingSystem().getName();
    }

    /**
     * Returns the redirection of the shortened web page
     * @param l shortUrl
     * @return web page
     */
    private ResponseEntity<?> createSuccessfulRedirectToResponse(ShortURL l) {
        HttpHeaders h = new HttpHeaders();
        h.setLocation(URI.create(l.getTarget()));
        return new ResponseEntity<>(h, HttpStatus.valueOf(l.getMode()));
    }
}
