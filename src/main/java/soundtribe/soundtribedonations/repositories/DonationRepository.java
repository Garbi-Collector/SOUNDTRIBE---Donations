package soundtribe.soundtribedonations.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soundtribe.soundtribedonations.entities.Donation;

import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByDonor(Long donor);

}