package soundtribe.soundtribedonations.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import soundtribe.soundtribedonations.services.DonationService;

@RestController
@RequestMapping("/eliminate-donor") // Puerto 8085
public class EliminateAccountController {

    @Autowired
    private DonationService donationService;

    @DeleteMapping
    public ResponseEntity<?> eliminarDonacionesDelUsuario(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Token no proporcionado o inválido");
        }

        String jwt = token.replace("Bearer ", "");


        try {
            donationService.EliminarUsuarioDonacion(jwt);
            return ResponseEntity.ok("Donaciones actualizadas correctamente (donor eliminado)");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error al eliminar donor: " + e.getMessage());
        }
    }
}
