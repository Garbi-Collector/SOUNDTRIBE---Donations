package soundtribe.soundtribedonations.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soundtribe.soundtribedonations.entities.Donation;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
}