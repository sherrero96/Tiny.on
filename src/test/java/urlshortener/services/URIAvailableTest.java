package urlshortener.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import urlshortener.domain.ShortURL;
import urlshortener.repository.impl.ShortURLRepositoryImpl;
import urlshortener.service.ShortURLBuilder;
import urlshortener.service.ShortURLService;
import urlshortener.service.URIAvailable;
import urlshortener.web.UrlShortenerController;

/**
 *
 */

public class URIAvailableTest {

    private URIAvailable availableURI = new URIAvailable();

    /**
     * Checks if it returns false given a uri that doesn't exist
     */
    @Test
    public void isURIAvailableFalse(){
        assert !availableURI.isURIAvailable("http://www.thisdomainIhopedoesnotexist.es");
    }

    /**
     * Checks if it returns true given a uri that exist
     */
    @Test
    public void isURIAvailableTrue(){
        assert availableURI.isURIAvailable("http://www.google.es");
    }
}
