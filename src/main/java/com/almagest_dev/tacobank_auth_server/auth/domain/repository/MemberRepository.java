package com.almagest_dev.tacobank_auth_server.auth.domain.repository;

import com.almagest_dev.tacobank_auth_server.auth.domain.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByTelAndDeletedNot(String tel, String deleted);
}
