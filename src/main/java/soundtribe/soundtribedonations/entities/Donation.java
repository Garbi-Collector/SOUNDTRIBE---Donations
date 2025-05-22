package soundtribe.soundtribedonations.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * en caso de que no tenga un donador como tal, es muy probable que sea porque el usuario elimino su cuenta
     */
    private Long donor; //usuario que dono

    private Integer amount;

    private BigDecimal realAmount;

    private Boolean success;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
