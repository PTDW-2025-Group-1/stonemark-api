package pt.estga.stonemark.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.estga.stonemark.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {}

