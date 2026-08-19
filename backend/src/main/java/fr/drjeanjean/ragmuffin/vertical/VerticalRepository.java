package fr.drjeanjean.ragmuffin.vertical;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerticalRepository extends JpaRepository<Vertical, UUID> {
}