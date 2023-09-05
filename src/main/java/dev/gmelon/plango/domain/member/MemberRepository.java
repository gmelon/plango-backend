package dev.gmelon.plango.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String nickname);

    @Query("select m from Member m " +
            "where function('replace', m.nickname, ' ', '') like %:nickname% " +
            "and m.id != :currentMemberId")
    List<Member> searchByNicknameWithoutCurrentMember(@Param("currentMemberId") Long currentMemberId, @Param("nickname") String trimmedNickname);
}
