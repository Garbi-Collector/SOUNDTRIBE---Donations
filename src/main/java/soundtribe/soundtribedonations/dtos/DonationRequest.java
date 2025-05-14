package soundtribe.soundtribedonations.dtos;

import lombok.Data;

@Data
public class DonationRequest {
    private Integer amount;
    private String mensaje;
}
