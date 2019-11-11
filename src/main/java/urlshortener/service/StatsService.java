package urlshortener.service;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@RestController
public class StatsService {

    private final ClickService clickService;

    public StatsService(ClickService clickService) {
        this.clickService = clickService;
    }

    @RequestMapping(value = "/{id:(?!link|index).*}/stats", method = RequestMethod.GET)
    public String obtainStats(@PathVariable String id, HttpServletResponse response) {
        ArrayList<String> lastClick = clickService.obtainLastStats(id);
        // TODO: Preguntar a Javier como se hace la redirección a web html
        // return redirection to stats.html
        // Now we write in the output standard
        String result = "";
        result = result + "Nº of visits: \t" + lastClick.get(0) + "\n";
        result = result + "IP address last visit: \t" + lastClick.get(1) + "\n";
        result = result + "Location last visit: \t" + lastClick.get(2) + "\n";
        result = result + "Platform last visit: \t" + lastClick.get(3) + "\n";
        return result;
    }
}
