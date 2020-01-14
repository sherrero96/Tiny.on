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
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import urlshortener.domain.ShortURL;
import urlshortener.service.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.io.IOException;
import java.net.URI;
import java.sql.Date;
import java.util.Calendar;


@RestController
public class UrlShortenerController {
    private final ShortURLService shortUrlService;
    private final CSVConverter csv;
    private InputStreamReader InputCsv;

    private final ClickService clickService;

    @Autowired
    private URIAvailable availableURI = new URIAvailable(); // To check if a URI is reachable

    @Autowired
    private QRCodeService qrCode = new QRCodeService();

    public UrlShortenerController(ShortURLService shortUrlService, ClickService clickService, CSVConverter csv) {
        this.shortUrlService = shortUrlService;
        this.clickService = clickService;
        this.csv = csv;
    }

    @RequestMapping(value = "/{id:(?!link|index|stats|error404.html).*}", method = RequestMethod.GET)
    public ResponseEntity<?> redirectTo(@PathVariable String id, HttpServletRequest request) {
        ShortURL l = shortUrlService.findByKey(id);
        if(l != null){
            if(availableURI.isURIAvailable(l.getTarget())){
                // Obtain all the information about the request and save in the DB
                clickService.saveClick(id, extractIP(request), extractCountry(request),
                        extractPlatform(request), new Date(Calendar.getInstance().getTime().getTime()));
                return createSuccessfulRedirectToResponse(l);
            }else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/qr", method = RequestMethod.GET)
    public ResponseEntity<byte[]> qr(@RequestParam("id") String id, HttpServletResponse response) throws IOException {
        ShortURL l = shortUrlService.findByKey(id);
        if (l != null && availableURI.isURIAvailable(l.getTarget())) {
			String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
			byte[] in = qrCode.getQRImage(baseUrl + '/' + id);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_PNG);
			
			return new ResponseEntity<>(in, headers, HttpStatus.CREATED);
		}
		else {
			return ResponseEntity.badRequest().build();
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
        String result = "Desconocido";
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

    /**
     * Returns a string with the different results separated by a comma.
     * @param file
     * @return String
     *          case Escalable: "escalable,total uri's, link download Resultfile, name ResultFile"
     *          case noEscalable: "total uri's, link download ResultFile"
     * @throws IOException
     */
    @RequestMapping(value = "/csv", method = RequestMethod.POST)
    public String handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {

        String nombreFichero = file.getOriginalFilename();
        String uriFichero2222 = "src/main/resources/static/csv/Salida_" + nombreFichero;
        File fff = new File(uriFichero2222);
        while(fff.exists()){
            nombreFichero = String.valueOf(nombreFichero.hashCode()) + ".csv";
            uriFichero2222 = "src/main/resources/static/csv/Salida_" + nombreFichero;
            fff = new File(uriFichero2222);
        }

        int total;
        // SI total = 0 enviar algo para que sepa que es fichero vacío.
        csv.CalcularTotal(new InputStreamReader
                (file.getInputStream(), StandardCharsets.UTF_8));
        total = csv.total();
        if(total == 0){
            nombreFichero = "vacio";
        }
        if(total > 1) {
            InputCsv = new InputStreamReader(file.getInputStream());
            String respuestaDescarga = "http://localhost:8080/download/" + nombreFichero;
            String resultadoEscalable = "escalable," + total + "," + respuestaDescarga + "," + nombreFichero;
            return resultadoEscalable;

        }

        HashMap<String, String> strings = csv.ConverterCSV(new InputStreamReader
                (file.getInputStream(), StandardCharsets.UTF_8));
        int acortadas = csv.acortadas();

        csv.guardar(nombreFichero);

        String respuestaDescarga = "http://localhost:8080/download/" + nombreFichero;
        String resultado = total + "," + respuestaDescarga;
        return resultado;


    }

    /**
     * Download result file.
     * @param name Name of file
     * @return Response of download file with filename is "Salida" + name.
     * @throws IOException
     */
    @RequestMapping(value = "/download/{fileName}", method = RequestMethod.GET)
    public ResponseEntity<Object> downloadFile(@PathVariable("fileName") String name) throws IOException
    {
        System.out.println("NOMBRE DESCARGAAAAA: " + name);
        String filename = "src/main/resources/static/csv/Salida_" + name ;
        File file = new File(filename);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition",
                String.format("attachment; filename=\"%s\"", file.getName()));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        ResponseEntity<Object> responseEntity = ResponseEntity.ok().headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/txt")).body(resource);

        return responseEntity;
    }

    /**
     * Delete file in database.
     * @param name Name of file.
     * @throws IOException
     */
    @RequestMapping(value = "/delete/{fileName}", method = RequestMethod.GET)
    public void deleteFile(@PathVariable("fileName") String name) throws IOException
    {
        String filename = "src/main/resources/static/csv/Salida" + name + ".csv";
        File file = new File(filename);
        file.delete();

        //Politica de empresa: Que borre semanalmente todos los ficheros generados que haya en el archivo?
    }

    /**
     * Returns an integer that indicates if the process of converting uri's of csv file
     *      in short uri's has been correct.
     * @param name Name of file
     * @return -1 in case of error. Return result > -1 in otherwise.
     * @throws IOException
     */
    @RequestMapping(value = "/csvEscalable/{fileName}", method = RequestMethod.POST)
    public int ConverterCSVEscalable(@PathVariable("fileName") String name) throws IOException {

        int result = csv.escalable(InputCsv, name);

        return result;

    }
}