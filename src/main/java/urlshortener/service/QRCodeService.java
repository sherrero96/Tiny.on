package urlshortener.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.time.Duration;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import io.vavr.control.Try;

import java.util.function.Supplier;

@Service
public class QRCodeService {

	private static String URL_QR_API;

	private URIAvailable uriAvailable;
	
	private CircuitBreaker circuitBreaker;

	// Max número de fallos antes de que se abra el circuito (porcentual)
	private static final int FALLOS_ANTES_OPEN = 5;
	
	// Max número de llamadas lentas antes de que se abra el circuito (porcentual)
	private static final int LLAMADAS_LENTAS_ANTES_OPEN = 50;
	
	// Intervalo de tiempo para que una llamada sea considerada lenta (s)
	private static final int TIEMPO_LLAMADA_LENTA = 1;
	
	// Mínimo número de llamadas para transición open -> half-open
	private static final int MIN_LLAMADAS_ANTES_CLOSE = 20;
	
	// Número de llamadas para evaluar el estado del sistema
	private static final int NUM_LLAMADAS_EVALUAR_ESTADO = 2;

	// Máximo número de veces que se intenta utilizar la API
	private static final int MAX_REINTENTOS = 2;
	
	public QRCodeService() {
		URL_QR_API = "https://api.qrserver.com/v1/create-qr-code/";
		setCircuit();
	}

	public QRCodeService(@NonNull String api) {
		URL_QR_API = api;
		setCircuit();
	}

	/**
	 * Configure circuitbreaker pattern
	 */
	public void setCircuit() {
		CircuitBreakerConfig config = CircuitBreakerConfig.custom().failureRateThreshold(FALLOS_ANTES_OPEN)
					.slowCallRateThreshold(LLAMADAS_LENTAS_ANTES_OPEN)
					.slowCallDurationThreshold(Duration.ofSeconds(TIEMPO_LLAMADA_LENTA))
					.waitDurationInOpenState(Duration.ofSeconds(MIN_LLAMADAS_ANTES_CLOSE))
					.minimumNumberOfCalls(NUM_LLAMADAS_EVALUAR_ESTADO)
					.build();

		CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(config);
	
		circuitBreaker = circuitBreakerRegistry.circuitBreaker("circuit");
	}

	/**
	 * Get circuit's state
	 *
	 * @return Current circuite'state
	 */
	public CircuitBreaker.State getCircuitState() {
		return circuitBreaker.getState();
	}

	/**
	 * Get QR image from a given text
	 * 
	 * @param short_url
	 * @return QR image as input stream
	 */
	public InputStream getQRImage(@NonNull String short_url) {
		Supplier<InputStream> image = () -> getQRImageFromAPI(short_url);

		image = CircuitBreaker.decorateSupplier(circuitBreaker, image);

		RetryConfig retryConfig = RetryConfig.custom().maxAttempts(MAX_REINTENTOS).build();
		Retry retry = Retry.of("imageQR", retryConfig);
		image = Retry.decorateSupplier(retry, image);

		InputStream finalImage = Try.ofSupplier(image)
								.recover(throwable -> generateQRImage(short_url)).get();

		return finalImage;
	}

	/**
	 * Makes API request in order to get QR image as byte stream
	 * 
	 * @param short_url Text to be converted into image
	 * @return QR image as input stream
	 * @throws Exception
	 */
	public InputStream getQRImageFromAPI(@NonNull String short_url) {
		try {
			String uri = UriComponentsBuilder.fromHttpUrl(URL_QR_API).queryParam("data", short_url)
					.queryParam("size", "100x100").queryParam("format", "png").toUriString();

			HttpRequestBase httpReq = new HttpGet(uri);
			HttpClient httpclient = HttpClientBuilder.create().build();
			HttpResponse response = httpclient.execute(httpReq);
			HttpEntity responseEntity = response.getEntity();

			// If API isn't available
			uriAvailable = new URIAvailable();
			if (!(uriAvailable.isURIAvailable(uri))) {
				throw new RuntimeException();
			}

			return responseEntity.getContent();
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	/**
	 * Create QR image and return it as a byte stream
	 * 
	 * @param short_url Text to be converted into image
	 * @return QR image as input stream
	 */
	public InputStream generateQRImage(@NonNull String short_url) {
		ByteArrayOutputStream os = QRCode.from(short_url).to(ImageType.PNG).withSize(100, 100).stream();
		return new ByteArrayInputStream(os.toByteArray());
	}
}
