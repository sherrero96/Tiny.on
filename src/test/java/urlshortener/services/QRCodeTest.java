package urlshortener.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.junit.Test;
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
			byte[] query_image = qr.getQRImageFromAPI("https://www.google.com");
			byte[] stored_image = Files.readAllBytes(Paths.get(GOOGLE_QR_IMAGE_PATH));

			assertTrue(Arrays.equals(query_image, stored_image));

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
			byte[] query_image = qr.getQRImageFromAPI("https://www.habbo.es");
			byte[] stored_image = FileUtils.readFileToByteArray(new File(GOOGLE_QR_IMAGE_PATH));

			assertNotEquals(query_image, stored_image);

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
		byte[] query_image = qr.generateQRImage("https://www.google.com");

		assertEquals("https://www.google.com", decode(query_image));
	}

	/**
	 * Checks QR image generated decoded content isn't equal to different content
	 * previously coded
	 *
	 * @throws IOException
	 */
	@Test
	public void incorrectQRGenerated() {
		byte[] query_image = qr.generateQRImage("https://www.habbo.es");

		assertNotEquals("https://www.google.com", decode(query_image));
	}

	/**
	 * Simulates API shut down so circuit will have to be open 
	 * 
	 * @param image
	 * @return
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

	@Test
	public void cache() {
		QRCodeService qrCode = new QRCodeService();
		
		long start_fail = System.currentTimeMillis();
		qrCode.getQRImage("https://www.google.com");
		long end_fail = System.currentTimeMillis();

		long start_hit = System.currentTimeMillis();
		qrCode.getQRImage("https://www.google.com");
		long end_hit = System.currentTimeMillis();

		// Could be more robust
		assertTrue((end_hit - start_hit) < (end_fail - start_fail));
	}

	/** Private functions used in tests */
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
