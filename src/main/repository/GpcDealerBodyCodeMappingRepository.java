package com.ford.protech.domain.repository;

import com.ford.protech.domain.entity.GpcDealerBodyCodeMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.data.jpa.repository.EntityGraph;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GpcDealerBodyCodeMappingRepository extends JpaRepository<GpcDealerBodyCodeMapping, Integer>, JpaSpecificationExecutor<GpcDealerBodyCodeMapping> {
    void deleteByGpcDealerGpcDealerKey(Integer gpcDealerKey);

    List<GpcDealerBodyCodeMapping> findByGpcDealerGpcDealerKey(Integer gpcDealerKey);

    @EntityGraph(value = "GpcDealerBodyCodeMappingEntityGraph", type = EntityGraph.EntityGraphType.LOAD)
    Page<GpcDealerBodyCodeMapping> findByGpcDealerGpcDealerKeyIn(List<Integer> gpcDealerKey, Pageable pageable);

    @EntityGraph(
            attributePaths = {
                    "gpcDealer.gpcRequest.fin.finKey",
                    "gpcDealer.gpcRequest.fin.accountName",
                    "bodyCode.vehicleLineModelYear.modelYearCode",
            }
    )
    List<GpcDealerBodyCodeMapping> findByGpcDealerGpcRequestStateStateCode(String stateCode);

    @EntityGraph(value = "GpcDealerBodyCodeMappingEntityGraph", type = EntityGraph.EntityGraphType.LOAD)
    Page<GpcDealerBodyCodeMapping> findByGpcDealerGpcDealerKeyInAndBodyCodeBodyReferenceCodeIn(List<Integer> gpcDealerKey, List<String> bodyCode, Pageable pageable);

    List<GpcDealerBodyCodeMapping> findByGpcDealerGpcRequestGpcRequestKey(Integer gpcRequestKey);

    List<GpcDealerBodyCodeMapping> findByGpcDealerGpcRequestGpcRequestKeyAndGpcDealerStatusGroupMappingStatusGroupMappingKey(Integer gpcRequestKey, Integer statusGroupMappingKey);

    @EntityGraph(
            attributePaths = {
                    "requestedGpcAmount",
                    "priceLevel",
                    "gpcDealer.gpcDealerKey",
                    "gpcDealer.solicitationNumber",
                    "gpcDealer.dealerBidOpenDate",
                    "gpcDealer.dealerCode",
                    "gpcDealer.gpcRequest.gpcRequestKey",
                    "gpcDealer.gpcRequest.gpcResponseDate",
                    "gpcDealer.gpcRequest.bidOpenDate",
                    "gpcDealer.gpcRequest.gpcExpiryDate",
                    "gpcDealer.gpcRequest.revisedBidOpenDate",
                    "gpcDealer.gpcRequest.state.stateCode",
                    "gpcDealer.gpcRequest.fin.finCode",
                    "gpcDealer.gpcRequest.fin.accountName",
                    "gpcDealer.gpcRequest.secondaryFin.finCode",
                    "gpcDealer.gpcRequest.secondaryFin.accountName",
                    "gpcDealer.gpcRequest.gpcRequestType.gpcRequestTypeCode",
                    "gpcDealer.bidType.bidTypeName",
                    "gpcDealer.statusGroupMapping.status.name",
                    "gpcDealer.gpcRequest.state.stateName",
                    "bodyCode.vehicleLineModelYear.modelYearCode",
                    "bodyCode.vehicleLineModelYear.vehicleLineCode",
                    "bodyCode.vehicleLineModelYear.vehicleLineName",
                    "bodyCode.bodyReferenceCode",
                    "bodyCode.bodyShortDetails",
                    "referenceNumber.referenceNumberKey",
                    "referenceNumber.referenceNumberType.referenceNumberTypeName"
                    //"referenceNumber.referenceNumberType.createTime",
            }
    )
    Page<GpcDealerBodyCodeMapping> findAll(Specification<GpcDealerBodyCodeMapping> specification, Pageable pageable);

    GpcDealerBodyCodeMapping findByGpcDealerGpcDealerKeyAndBodyCodeBodyKey(Integer gpcDealerKey, UUID bodyKey);


    List<GpcDealerBodyCodeMapping> findByGpcDealerGpcRequestGpcRequestKeyInAndGpcDealerDealerCodeInAndBodyCodeBodyKeyInAndReferenceNumberReferenceNumberKeyIsNull(List<Integer> gpcRequestKey, List<String> dealerCode, List<UUID> bodyKey);

    List<GpcDealerBodyCodeMapping> findByBodyCodeBodyKeyAndPriceLevelAndAssignedGpcAmount(UUID bodyKey, String priceLevel, Integer assignedGpcAmount);

    GpcDealerBodyCodeMapping findByReferenceNumberReferenceNumberKey(UUID referenceNumberKey);
}

 