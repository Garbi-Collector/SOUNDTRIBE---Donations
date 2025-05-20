// MPController.java
package soundtribe.soundtribedonations.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import soundtribe.soundtribedonations.dtos.DonationRequest;
import soundtribe.soundtribedonations.dtos.DonationResponse;
import soundtribe.soundtribedonations.services.DonationService;

@RestController
@RequestMapping("/api") //8085
public class MPController {

    @Autowired
    private DonationService donationService;


    @PostMapping("/donate")
    public ResponseEntity<DonationResponse> createDonation(
            @RequestHeader("Authorization") String token,
            @RequestBody DonationRequest request
    ) {
        DonationResponse response = donationService.createDonation(
                cleanToken(token),
                request.getAmount(),
                request.getMensaje());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/success")
    public String success(
            @RequestHeader("Authorization") String token,
            @RequestParam("mensaje") String mensaje,
            @RequestParam(name = "donationId") Long donationId,
            @RequestParam(name = "payment_id", required = false) String paymentId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "collection_status", required = false) String collectionStatus
    ) {
        donationService.confirmDonation(cleanToken(token),donationId,mensaje);
        return "Gracias por tu donación. Estado: " + collectionStatus;
    }


    @GetMapping("/failure")
    public String failure(@RequestParam(name = "status", required = false) String status) {
        return "Pago fallido. Estado: " + status;
    }

    // Utilidad para limpiar el "Bearer "
    private String cleanToken(String token) {
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }

}
