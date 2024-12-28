package com.ford.protech.domain.repository;

import com.ford.protech.domain.entity.GpcDealer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GpcDealerRepository extends JpaRepository<GpcDealer, Integer>, JpaSpecificationExecutor<GpcDealer> {
    GpcDealer findByGpcRequestGpcRequestKeyAndDealerCode(Integer gpcRequestKey, String dealerCode);

    @EntityGraph(attributePaths = {"gpcDealerKey"})
    List<GpcDealer> findByGpcRequestGpcRequestKey(Integer gpcRequestKey);

    List<GpcDealer> findByGpcRequestGpcRequestKeyAndStatusGroupMappingStatusCodeNot(Integer gpcRequestKey, String statusCode);


    @EntityGraph(
            attributePaths = {
                    "gpcDealerKey",
                    "solicitationNumber",
                    "dealerBidOpenDate",
                    "dealerCode",
                    "gpcRequest.gpcRequestKey",
                    "gpcRequest.gpcResponseDate",
                    "gpcRequest.state.stateCode",
                    "gpcRequest.fin.finCode",
                    "gpcRequest.fin.accountName",
                    "gpcRequest.fin.address.state.stateCode",
                    "gpcRequest.secondaryFin.finCode",
                    "gpcRequest.secondaryFin.accountName",
                    "gpcRequest.gpcRequestType.gpcRequestTypeCode",
                    "bidType.bidTypeName",
                    "statusGroupMapping.status.name",
            }
    )
    Page<GpcDealer> findAll(Specification<GpcDealer> specification, Pageable pageable);

}

 