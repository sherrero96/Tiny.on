package urlshortener.web;

import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.util.StreamUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import urlshortener.domain.ShortURL;
import urlshortener.service.ClickService;
import urlshortener.service.ShortURLService;
import urlshortener.service.URIAvailable;
import urlshortener.service.QRCode;

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
    private QRCode qrCode = new QRCode();

    public UrlShortenerController(ShortURLService shortUrlService, ClickService clickService) {
        this.shortUrlService = shortUrlService;
        this.clickService = clickService;
    }

    @RequestMapping(value = "/{id:(?!link|index).*}", method = RequestMethod.GET)
    public ResponseEntity<?> redirectTo(@PathVariable String id, HttpServletRequest request) {
        ShortURL l = shortUrlService.findByKey(id);
        if (l != null) {
            clickService.saveClick(id, extractIP(request));
            return createSuccessfulRedirectToResponse(l);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/qr", method = RequestMethod.GET)
    public void qr(@RequestParam("id") String id, HttpServletResponse response) throws IOException {
        ShortURL l = shortUrlService.findByKey(id);
        if (l != null) {
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            InputStream in = qrCode.getQRImageAsStream(baseUrl + '/' + id);
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
            ShortURL su = shortUrlService.save(url, sponsor, request.getRemoteAddr());
            HttpHeaders h = new HttpHeaders();
            h.setLocation(su.getUri());
            return new ResponseEntity<>(su, h, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private String extractIP(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private ResponseEntity<?> createSuccessfulRedirectToResponse(ShortURL l) {
        HttpHeaders h = new HttpHeaders();
        h.setLocation(URI.create(l.getTarget()));
        return new ResponseEntity<>(h, HttpStatus.valueOf(l.getMode()));
    }
}
