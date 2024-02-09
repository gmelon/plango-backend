package dev.gmelon.plango.domain.fcm.repository;

import dev.gmelon.plango.domain.fcm.entity.FirebaseCloudMessageToken;
import dev.gmelon.plango.domain.member.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FirebaseCloudMessageTokenRepository extends JpaRepository<FirebaseCloudMessageToken, Long> {

    Optional<FirebaseCloudMessageToken> findByTokenValue(String tokenValue);

    List<FirebaseCloudMessageToken> findAllByMember(Member member);

    void deleteAllInBatchByMemberId(Long memberId);
}
