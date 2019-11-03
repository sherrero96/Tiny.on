package urlshortener.web;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import urlshortener.domain.ShortURL;
import urlshortener.service.ClickService;
import urlshortener.service.ShortURLService;
import urlshortener.service.URIAvailable;
import urlshortener.service.QRCode;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
public class UrlShortenerController {
    private final ShortURLService shortUrlService;

    private final ClickService clickService;

    @Autowired
    private URIAvailable availableURI = new URIAvailable();  // To check if a URI is reachable

    public UrlShortenerController(ShortURLService shortUrlService, ClickService clickService) {
        this.shortUrlService = shortUrlService;
        this.clickService = clickService;
    }

    @RequestMapping(value = "/{id:(?!link|index).*}", method = RequestMethod.GET)
    public ResponseEntity<?> redirectTo(@PathVariable String id,
                                        HttpServletRequest request) {
        ShortURL l = shortUrlService.findByKey(id);
        if (l != null) {
            clickService.saveClick(id, extractIP(request));
            return createSuccessfulRedirectToResponse(l);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/link", method = RequestMethod.POST)
    public ResponseEntity<ShortURL> shortener(@RequestParam("url") String url,
                                              @RequestParam(value = "sponsor", required = false) String sponsor,
                                              HttpServletRequest request) {
        UrlValidator urlValidator = new UrlValidator(new String[]{"http",
                "https"});

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

    @RequestMapping(value = "/linkQRCode", method = RequestMethod.POST)
    public ResponseEntity<String> showQRImage (@RequestParam("url") String url, HttpServletRequest request) {
        UrlValidator urlValidator = new UrlValidator(new String[]{"http",
                "https"});

        if (urlValidator.isValid(url) && availableURI.isURIAvailable(url)) {
            String url_qrcode = QRCode.getQRCode(url);
            return new ResponseEntity<String>(url_qrcode, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
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
