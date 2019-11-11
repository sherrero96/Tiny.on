package urlshortener.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

@RestController
public class StatsService {

    private final ClickService clickService;

    public StatsService(ClickService clickService) {
        this.clickService = clickService;
    }

    @RequestMapping(value = "/{id:(?!link|index).*}/stats", method = RequestMethod.GET)
    public void prueba(@PathVariable String id) {
        ArrayList<String> lastClick = clickService.obtainLastStats(id);
        // For debug
        for(String aux : lastClick){
            System.out.println(aux);
        }
    }
}
