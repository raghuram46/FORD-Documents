package com.fordpro.cloudrun.repository;


import com.fordpro.cloudrun.commonutil.Constants;
import com.fordpro.cloudrun.domain.entity.*;
import com.fordpro.cloudrun.domain.repository.DealerClaimRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@Transactional
@Disabled("Repository tests are excluded in pipeline")
class DealerClaimRepositoryTest {
    @Autowired
    DealerClaimRepository dealerClaimRepository;

    @Test
    void shouldSaveDealerClaim() {
        DealerClaim dealerClaim = getDealerClaimDetail();
        DealerClaim expected = dealerClaimRepository.save(dealerClaim);
        Assertions.assertNotNull(expected);
    }

    @Test
    void shouldUpdateDealerClaim() {
        DealerClaim dealerClaim = dealerClaimRepository.findById(1).get();
        dealerClaim.setDealerCode("13161");
        DealerClaim expected = dealerClaimRepository.save(dealerClaim);
        Assertions.assertNotNull(expected);
    }

    @Test
    void shouldGetAllDealerClaim() {
        List<DealerClaim> dealerClaims = dealerClaimRepository.findAll();
        Assertions.assertNotNull(dealerClaims);
    }

    @Test
    void shouldGetDealerClaim() {
        DealerClaim claim = dealerClaimRepository.findByDealerClaimKey(1);
        Assertions.assertNotNull(claim);
    }

    private DealerClaim getDealerClaimDetail() {
        return DealerClaim.builder()
                .claimType(PriceProtectionType.builder().ppTypeCode("C").build())
                .dealerCode("22524")
                .vehicleLineModelYear(VehicleLineModelYear.builder().vehicleLineKey(UUID.fromString("03732061-de82-48b1-bf2a-e6e43c70c233")).build())
                .internalStatus(StatusGroupMapping.builder().statusGroupMappingKey(1).build())
                .dealerStatus(StatusGroupMapping.builder().statusGroupMappingKey(1).build())
                .submittedFlag("A")
                .state(State.builder().stateKey(1).build())
                .fin(Fin.builder().finKey(UUID.fromString("240283c5-128b-4613-a7f4-df78e5055ae9")).build())
                .comment("Test Claim FedState change")
                .createTime(new Timestamp(new Date().getTime()))
                .createUser(Constants.LOGIN_USER)
                .updateTime(new Timestamp(new Date().getTime()))
                .updateUser(Constants.LOGIN_USER)
                .build();
    }
}

 