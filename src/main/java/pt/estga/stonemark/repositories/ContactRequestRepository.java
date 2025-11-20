package pt.estga.stonemark.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.stonemark.entities.ContactRequest;

public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {}

