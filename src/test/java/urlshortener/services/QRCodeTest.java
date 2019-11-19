package urlshortener.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import urlshortener.service.QRCode;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class QRCodeTest {

    private QRCode qr = new QRCode();

    private final String googleQRImagePath = "src/test/resources/qr_test/google_url.png";

    /**
     * Checks QR image obtained from an url using our QRCode class it's
     * equal to the same QR image stored on disk (previously tested)
     *
     * @throws IOException
     */
    @Test
    public void correctQRCode() {
        try {
            InputStream query_image = qr.getQRImageAsStream("https://www.google.com");
			InputStream stored_image =
				new FileInputStream(new File(googleQRImagePath));

			assertTrue(IOUtils.contentEquals(query_image, stored_image));

            stored_image.close();
		
		} catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Checks QR image obtained from an url using our QRCode class it's
     * not equal to a different QR image stored on disk (previously tested)
     *
     * @throws IOException
     */
    @Test
    public void incorrectQRCode() {
        try {
            InputStream query_image = qr.getQRImageAsStream("https://www.habbo.es");
			InputStream stored_image =
				new FileInputStream(new File(googleQRImagePath));

			assertFalse(IOUtils.contentEquals(query_image, stored_image));

            stored_image.close();
		
		} catch(IOException e) {
            e.printStackTrace();
        }
    }
}
