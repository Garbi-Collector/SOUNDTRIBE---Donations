package soundtribe.soundtribedonations.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soundtribe.soundtribedonations.dtos.DonationResponse;

@Service
public interface DonationService {


    @Transactional
    DonationResponse createDonation(String token, Integer amount, String mensaje);

    @Transactional
    void confirmDonation(String token, Long donationId, String mensaje);

    @Transactional
    @Async
    void EliminarUsuarioDonacion(String token);
}
