package pt.estga.support.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.support.entities.ContactRequest;

public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {}
