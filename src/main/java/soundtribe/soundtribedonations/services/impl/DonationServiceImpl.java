// DonationServiceImpl.java
package soundtribe.soundtribedonations.services.impl;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soundtribe.soundtribedonations.dtos.DonationResponse;
import soundtribe.soundtribedonations.dtos.notis.NotificationPost;
import soundtribe.soundtribedonations.dtos.notis.NotificationType;
import soundtribe.soundtribedonations.entities.Donation;
import soundtribe.soundtribedonations.externalAPIS.ExternalJWTService;
import soundtribe.soundtribedonations.externalAPIS.NotificationService;
import soundtribe.soundtribedonations.repositories.DonationRepository;
import soundtribe.soundtribedonations.services.DonationService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class DonationServiceImpl implements DonationService {

    private static final Logger logger = LoggerFactory.getLogger(DonationServiceImpl.class);

    @Autowired
    private ExternalJWTService externalJWTService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private  DonationRepository donationRepository;

    @Value("${access.token}")
    private String accessToken;

    //http://soundtribe.art:4200
    @Value("${app.front.url}")
    private String front;



    @Transactional
    @Override
    public DonationResponse createDonation(String token, Integer amount, String mensaje) {

        Map<String, Object> userInfo = externalJWTService.validateToken(token);
        Boolean isUSer = (Boolean) userInfo.get("valid");
        if (!Boolean.TRUE.equals(isUSer)) {
            throw new RuntimeException("No eres un usuario");
        }
        Integer userIdInteger = (Integer) userInfo.get("userId");

        Long userId = userIdInteger.longValue();

        if (amount < 10) {
            throw new IllegalArgumentException("El monto mínimo de donación es 10");
        }

        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            // Guardar en base de datos
            Donation donation = Donation.builder()
                    .donor(userId)
                    .amount(amount)
                    .realAmount(realAmount(amount))
                    .success(false)
                    .build();
            Donation saveDonation = donationRepository.save(donation);

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(front + "/donation/success?donationId=" + saveDonation.getId())
                    .pending(front+"/donation/pending")
                    .failure(front+"/donation/failure")
                    .build();

            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(saveDonation.getId().toString())
                    .title("Donación a SoundTribe")
                    .description("Gracias por tu aporte")
                    .currencyId("ARS")
                    .quantity(1)
                    .unitPrice(new BigDecimal(amount))
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(itemRequest))
                    .backUrls(backUrls)
                    .build();


            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            logger.info("Preferencia creada con éxito. ID: {}", preference.getId());

            // crear notificacion
            notificationService.enviarNotificacion(
                    token,
                    NotificationPost.builder()
                            .type(NotificationType.DONATION)
                            .mensaje(mensaje)
                            .build()
            );

            return new DonationResponse(preference.getInitPoint(), saveDonation.getId());

        } catch (MPException e) {
            logger.error("Error en MercadoPago: {}", e.getMessage());
            throw new RuntimeException("Error en MercadoPago: " + e.getMessage());
        } catch (MPApiException e) {
            logger.error("Respuesta de MercadoPago: {}", e.getApiResponse().getContent());
            throw new RuntimeException("Error en MercadoPago: " + e.getApiResponse().getContent());
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            throw new RuntimeException("Error inesperado: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void confirmDonation(String token, Long donationId, String mensaje) {

        Map<String, Object> userInfo = externalJWTService.validateToken(token);
        Boolean isUSer = (Boolean) userInfo.get("valid");
        if (!Boolean.TRUE.equals(isUSer)) {
            throw new RuntimeException("No eres un usuario");
        }
        Integer userIdInteger = (Integer) userInfo.get("userId");

        Long userId = userIdInteger.longValue();

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donación no encontrada"));
        donation.setSuccess(true);
        donationRepository.save(donation);
        // crear notificacion

        notificationService.enviarNotificacion(
                token,
                NotificationPost.builder()
                        .type(NotificationType.DONATION)
                        .mensaje(mensaje)
                        .build()
        );
    }


    @Transactional
    @Async
    @Override
    public void EliminarUsuarioDonacion(String token) {
        Map<String, Object> userInfo = externalJWTService.validateToken(token);
        Boolean isUser = (Boolean) userInfo.get("valid");

        if (!Boolean.TRUE.equals(isUser)) {
            throw new RuntimeException("No eres un usuario válido");
        }

        Integer userIdInteger = (Integer) userInfo.get("userId");
        Long userId = userIdInteger.longValue();

        // 1. Obtener todas las donaciones del usuario
        List<Donation> donaciones = donationRepository.findByDonor(userId);

        // 2. Si no tiene donaciones, salimos sin hacer nada
        if (donaciones.isEmpty()) {
            return;
        }

        // 3. Setear donor a null en cada una
        for (Donation donacion : donaciones) {
            donacion.setDonor(null);
        }

        // 4. Guardar los cambios
        donationRepository.saveAll(donaciones);
    }




    /**
     * mercado pago me cobra un 6.29% por tener la plata al instante
     * @param amount el valor que paga el donador
     * @return el valor que recivo yo
     */
    private BigDecimal realAmount(Integer amount) {
        BigDecimal porcentaje = new BigDecimal("0.9371");
        return new BigDecimal(amount).multiply(porcentaje).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

}
