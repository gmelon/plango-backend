package dev.gmelon.plango.domain.refreshtoken.repository;

import dev.gmelon.plango.domain.refreshtoken.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
