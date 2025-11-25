package pt.estga.contact;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.contact.entities.ContactRequest;

public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {}
