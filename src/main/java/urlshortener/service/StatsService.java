package urlshortener.service;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
public class StatsService {

    private final ClickService clickService;

    public StatsService(ClickService clickService) {
        this.clickService = clickService;
    }

    private ArrayList<String> obtainLastStat(String id){
        return clickService.obtainLastStats(id);
    }

    @RequestMapping(value = "/{id:(?!link|index|stats).*}/stats", method = RequestMethod.GET)
    public ModelAndView obtainStats(@PathVariable String id, HttpServletResponse response) {
        ArrayList<String> lastClick = obtainLastStat(id);
        Map<String, Object> clicks = new HashMap<>();
        clicks.put("number", lastClick.get(0));
        clicks.put("ip", lastClick.get(1));
        clicks.put("location", lastClick.get(2));
        clicks.put("platform", lastClick.get(3));

        return new ModelAndView("redirect:/stats.html", clicks);
    }
}
