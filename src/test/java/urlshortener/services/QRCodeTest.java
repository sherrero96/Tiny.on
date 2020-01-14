package urlshortener.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.springframework.cache.annotation.CacheEvict;
import org.apache.commons.io.FileUtils;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import urlshortener.service.QRCodeService;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


public class QRCodeTest {

	private static final Logger log = LoggerFactory
            .getLogger(QRCodeTest.class);

	private QRCodeService qr = new QRCodeService();

	private final String GOOGLE_QR_IMAGE_PATH = "src/test/resources/qr_test/google_url.png";

	/**
	 * Checks QR image obtained from an url using our QRCode class it's equal to the
	 * same QR image stored on disk (previously tested)
	 *
	 * @throws IOException
	 */
	@Test
	public void correctQRCodeFromAPI() {
		try {
			byte[] queryImage = qr.getQRImageFromAPI("https://www.google.com");
			byte[] storedImage = Files.readAllBytes(Paths.get(GOOGLE_QR_IMAGE_PATH));

			assertTrue(Arrays.equals(queryImage, storedImage));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks QR image obtained from an url using QRCode API it's different from
	 * other QR image stored on disk (previously tested)
	 *
	 * @throws IOException
	 */
	@Test
	public void incorrectQRCodeFromAPI() {
		try {
			byte[] queryImage = qr.getQRImageFromAPI("https://www.habbo.es");
			byte[] storedImage = FileUtils.readFileToByteArray(new File(GOOGLE_QR_IMAGE_PATH));

			assertNotEquals(queryImage, storedImage);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks QR image generated decoded content it's equal to same content previously
	 * coded
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctQRCodeGenerated() {
		byte[] queryImage = qr.generateQRImage("https://www.google.com");

		assertEquals("https://www.google.com", decode(queryImage));
	}

	/**
	 * Checks QR image generated decoded content isn't equal to different content
	 * previously coded
	 *
	 * @throws IOException
	 */
	@Test
	public void incorrectQRGenerated() {
		byte[] queryImage = qr.generateQRImage("https://www.habbo.es");

		assertNotEquals("https://www.google.com", decode(queryImage));
	}

	/**
	 * Simulates API shut down so circuit will have to be open
	 * 
	 */
	@Test
	public void forceOpenCircuit() {
		QRCodeService qrBadAPI = new QRCodeService("https://www.badurlchoosenonporpuse.es");

		assertEquals("CLOSED", qrBadAPI.getCircuitState());

		// API is down so circuit must be open after requests
		qrBadAPI.getQRImage("https://www.google.com");
		qrBadAPI.getQRImage("https://www.google.com");
		
		assertEquals("OPEN", qrBadAPI.getCircuitState());
	}

	/**
	 * Checks cached data is obtained in less time rather than when it's not
	 * 
	 */
	@Test
	@CacheEvict(value="{qr, lastStats}", allEntries=true)
	public void cache() {
		QRCodeService qrCode = new QRCodeService();
		
		long startFail = System.currentTimeMillis();
		qrCode.getQRImage("https://www.google.com");
		long endFail = System.currentTimeMillis();
		long timeFail = endFail - startFail;

		long startHit = System.currentTimeMillis();
		qrCode.getQRImage("https://www.google.com");
		long endHit = System.currentTimeMillis();
		long timeHit = endHit - startHit;

		boolean cacheHitLessTime = timeHit < timeFail;
		log.info("fail " + endFail);
		log.info("hit " + endHit);
		log.debug("fail " + endFail);
		log.debug("hit " + endHit);

		// Could be more robust
		assertTrue(cacheHitLessTime);
	}

	/** Private functions used in tests */

	/**
	 * Decoded given QR as a byte array 
	 * 
	 * @param image is the image encoded
	 * @return QR content as String
	 */
	private String decode(byte[] image) {
		try {
			BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
			LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			QRCodeReader qrReader = new QRCodeReader();
			Result result = qrReader.decode(bitmap);
			return result.getText();
		} catch (IOException | NotFoundException | ChecksumException | FormatException e) {
			e.printStackTrace();
			return null;
		}
	}
}