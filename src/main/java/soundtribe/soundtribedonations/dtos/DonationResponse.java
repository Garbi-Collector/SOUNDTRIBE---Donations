// DonationResponse.java
package soundtribe.soundtribedonations.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DonationResponse {
    private String initPoint;
    private Long donationId;
}
