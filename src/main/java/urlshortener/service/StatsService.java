package urlshortener.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import urlshortener.domain.ShortURL;
import urlshortener.repository.ShortURLRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
public class StatsService {

    private final ClickService clickService;

    @Autowired
    private URIAvailable uriAvailable;

    @Autowired
    private ShortURLRepository shortURLRepository;

    public StatsService(ClickService clickService) {
        this.clickService = clickService;
    }

    /**
     * Return the last stats of the uri with the hash id
     * @param id
     * @return
     */
    private ArrayList<String> obtainLastStat(String id){
        return clickService.obtainLastStats(id);
    }

    /**
     * Redirect to a html with the last stats of the uri
     * @param id hash of the uri to see the stats
     * @return view of the last stats if the uri is reachable
     */
    @RequestMapping(value = "/{id:(?!link|index|stats|error404.html).*}/stats", method = RequestMethod.GET)
    public ModelAndView obtainStats(@PathVariable String id) {
        // Obtain the url
        ShortURL shortURL = shortURLRepository.findByKey(id);
        if(shortURL != null && uriAvailable.isURIAvailable(shortURL.getTarget())){
            ArrayList<String> lastClick = obtainLastStat(id);
            Map<String, Object> clicks = new HashMap<>();
            clicks.put("number", lastClick.get(0));
            clicks.put("ip", lastClick.get(1));
            clicks.put("location", lastClick.get(2));
            clicks.put("platform", lastClick.get(3));
            return new ModelAndView("redirect:/stats.html", clicks);
        }else{
            return new ModelAndView("redirect:/error404.html");
        }

    }
}
