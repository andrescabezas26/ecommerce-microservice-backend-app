package com.selimhorri.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.selimhorri.app.domain.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {

    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken vt WHERE vt.id = :id")
    void deleteByIdCustom(Integer id);

}
