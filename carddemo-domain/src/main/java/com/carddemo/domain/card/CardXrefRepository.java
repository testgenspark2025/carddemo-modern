package com.carddemo.domain.card;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardXrefRepository extends JpaRepository<CardXrefEntity, String> {
    // Replaces the CXACAIX alternate-index path used in COACTVWC.
    Optional<CardXrefEntity> findFirstByAcctId(Long acctId);
}
