package urlshortener.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import urlshortener.service.QRCodeService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class QRCodeTest {

	private QRCodeService qr = new QRCodeService();

	private final String googleQRImagePath = "src/test/resources/qr_test/google_url.png";

	/**
	 * Checks QR image obtained from an url using our QRCode class it's equal to the
	 * same QR image stored on disk (previously tested)
	 *
	 * @throws IOException
	 */
	@Test
	public void correctQRCodeFromAPI() {
		try {
			InputStream query_image = qr.getQRImageFromAPI("https://www.google.com");
			InputStream stored_image = new FileInputStream(new File(googleQRImagePath));

			assertTrue(IOUtils.contentEquals(query_image, stored_image));

			stored_image.close();

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
			InputStream query_image = qr.getQRImageFromAPI("https://www.habbo.es");
			InputStream stored_image = new FileInputStream(new File(googleQRImagePath));

			assertFalse(IOUtils.contentEquals(query_image, stored_image));

			stored_image.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @throws IOException
	 */
	@Test
	public void correctQRCodeGenerated() {
		try {
			InputStream query_image = qr.generateQRImage("https://www.google.com");

			assertEquals("https://www.google.com", decode(query_image));

			query_image.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 *
	 * @throws IOException
	 */
	@Test
	public void incorrectQRGenerated() {
		try {
			InputStream query_image = qr.generateQRImage("https://www.habbo.es");

			assertNotEquals("https://www.google.com", decode(query_image));

			query_image.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param image
	 * @return
	 */
	@Test
	public void forceOpenCircuit() {
		QRCodeService qrBadAPI = new QRCodeService("https://www.badurlchoosenonporpuse.es");
		

		assertEquals("CLOSED", qrBadAPI.getCircuitState().name());

		// API is down so circuit must be opened after requests
		qrBadAPI.getQRImage("https://www.google.com");
		qrBadAPI.getQRImage("https://www.google.com");

		assertEquals("OPEN", qrBadAPI.getCircuitState().name());
	}

	/** FUNCITONS USED IN TEST */
	private String decode(InputStream image) {
		try {
			BufferedImage bufferedImage = ImageIO.read(image);
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
