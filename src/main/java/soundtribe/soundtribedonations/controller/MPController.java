package soundtribe.soundtribedonations.controller;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MPController {

    private static final Logger logger = LoggerFactory.getLogger(MPController.class);


    @Value("${access.token}")
    private String accessToken;

    @GetMapping("/mercado")
    public String mercado() {
        try {
            if (accessToken == null || accessToken.isEmpty()) {
                logger.error("AccessToken no está configurado.");
                return "Error: AccessToken no está definido";
            }

            MercadoPagoConfig.setAccessToken(accessToken);
            logger.info("AccessToken configurado correctamente");

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("http://localhost:8085/api/success")
                    .pending("http://localhost:8085/api/pending")
                    .failure("http://localhost:8085/api/failure")
                    .build();

            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id("1234")
                    .title("Game")
                    .description("PS5")
                    .pictureUrl("img/650_1200.jpeg")
                    .categoryId("games")
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(new BigDecimal("10.00"))
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            logger.info("Preferencia creada con éxito. ID: {}", preference.getId());

            return preference.getInitPoint();

        } catch (MPApiException e) {
            logger.error("MPApiException al crear la preferencia: Status {}, Message {}", e.getStatusCode(), e.getMessage());
            return "Error API MercadoPago: " + e.getMessage();
        } catch (MPException e) {
            logger.error("MPException general: {}", e.getMessage());
            return "Error general de MercadoPago: " + e.getMessage();
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            return "Error inesperado: " + e.getMessage();
        }
    }

    // Este endpoint se ejecuta cuando el usuario vuelve desde MercadoPago
    @GetMapping("/success")
    public String success(@RequestParam(name = "payment_id", required = false) String paymentId,
                          @RequestParam(name = "status", required = false) String status,
                          @RequestParam(name = "merchant_order_id", required = false) String orderId,
                          @RequestParam(name = "collection_status", required = false) String collectionStatus) {

        logger.info("Pago recibido:");
        logger.info(" - Payment ID: {}", paymentId);
        logger.info(" - Status: {}", status);
        logger.info(" - Collection Status: {}", collectionStatus);
        logger.info(" - Order ID: {}", orderId);

        return "Pago recibido. Estado: " + collectionStatus;
    }

    @GetMapping("/failure")
    public String failure(@RequestParam(name = "status", required = false) String status) {
        logger.warn("Pago rechazado o fallido. Estado: {}", status);
        return "Pago fallido. Estado: " + status;
    }
}
