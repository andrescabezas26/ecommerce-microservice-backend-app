package com.selimhorri.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.selimhorri.app.domain.Credential;

public interface CredentialRepository extends JpaRepository<Credential, Integer> {

	Optional<Credential> findByUsername(final String username);

	boolean existsByUsername(String username);

	boolean existsByUserUserId(Integer userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Credential c WHERE c.credentialId = :credentialId")
    void deleteByCredentialId(Integer credentialId);

}
