package dev.gmelon.plango.domain.auth;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailTokenRepository extends CrudRepository<EmailToken, String> {
}
