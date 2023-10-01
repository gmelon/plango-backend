package dev.gmelon.plango.domain.fcm;

import dev.gmelon.plango.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FirebaseCloudMessageTokenRepository extends JpaRepository<FirebaseCloudMessageToken, Long> {

    Optional<FirebaseCloudMessageToken> findByTokenValue(String tokenValue);

    List<FirebaseCloudMessageToken> findAllByMember(Member member);
}
