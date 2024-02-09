package dev.gmelon.plango.domain.auth.repository;

import dev.gmelon.plango.domain.auth.entity.EmailToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailTokenRepository extends CrudRepository<EmailToken, String> {
}
