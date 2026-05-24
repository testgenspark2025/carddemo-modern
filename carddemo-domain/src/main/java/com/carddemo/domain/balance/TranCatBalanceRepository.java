package com.carddemo.domain.balance;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TranCatBalanceRepository
        extends JpaRepository<TranCatBalanceEntity, TranCatBalanceId> {

    // Matches the sequential read order of TCATBAL VSAM file in CBACT04C.
    List<TranCatBalanceEntity> findAllByOrderByAcctIdAscTranTypeCdAscTranCatCdAsc();
}
