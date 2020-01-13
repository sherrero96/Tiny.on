package urlshortener.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import urlshortener.domain.Click;
import urlshortener.repository.ClickRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClickService {

    private static final Logger log = LoggerFactory
            .getLogger(ClickService.class);

    private final ClickRepository clickRepository;

    public ClickService(ClickRepository clickRepository) {
        this.clickRepository = clickRepository;
    }

    public void saveClick(String hash, String ip, String country, String platform) {
        Click cl = ClickBuilder.newInstance().hash(hash).createdNow().ip(ip).country(country).platform(platform).build();
        cl = clickRepository.save(cl);
        log.info(cl != null ? "[" + hash + "] saved with id [" + cl.getId() + "]" : "[" + hash + "] was not saved");
    }

    /**
     * Returns the list of the latest statistics for a given link.
     * Returns the number of times it has been visited, the address of the last visit,
     * the location of that address, and the platform from which it was accessed.
     * @param hash The url shortened for the stats
     * @return ArrayList[0]: number of times, ArrayList[1]:Address last visit, ArrayList[2]:location, ArrayList[3]:platform
     */
    public ArrayList<String> obtainLastStats(String hash){
        List<Click> hashes = clickRepository.findByHash(hash);
        ArrayList<String> result = new ArrayList<>();
        result.add(0, String.valueOf(hashes.size()));
        if(hashes.size() == 0){
            // Ninguna visita realizada... ponemos datos fijados
            result.add(1, "Desconocido");
            result.add(2, "Desconocido");
            result.add(3, "Desconocido");
        }else{
            Click lastClick = hashes.get(hashes.size() - 1);
            if(lastClick.getIp().equals("0:0:0:0:0:0:0:1")){
                result.add(1, "0.0.0.0");
            }else{
                result.add(1, lastClick.getIp());
            }
            result.add(2, lastClick.getCountry());
            result.add(3, lastClick.getPlatform());
        }

        return result;
    }

}
