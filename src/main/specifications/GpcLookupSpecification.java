package com.ford.protech.specifications;

import com.ford.protech.domain.entity.*;
import com.ford.protech.gpclookup.api.GpcLookupRequest;
import com.ford.protech.utils.Constants;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class GpcLookupSpecification {

    public Specification<GpcDealerBodyCodeMapping> getGpcLookupSpecification(GpcLookupRequest gpcLookupRequest) {

        return (Root<GpcDealerBodyCodeMapping> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            Join<GpcDealerBodyCodeMapping, BodyCode> gpcDealerBodyCodeMappingJoin = root.join("bodyCode");
            Join<BodyCode, VehicleLineModelYear> bodyCodeVehicleLineModelYearJoin = gpcDealerBodyCodeMappingJoin.join("vehicleLineModelYear");

            Join<GpcDealerBodyCodeMapping, ReferenceNumber> gpcDealerBodyCodeMappingRefNumJoin = root.join("referenceNumber");
            Join<ReferenceNumber, ReferenceNumberType> referenceNumberTypeJoin = gpcDealerBodyCodeMappingRefNumJoin.join("referenceNumberType");

            Join<GpcDealerBodyCodeMapping, GpcDealer> gpcDealerBodyCodeMappingGpcDealerJoin = root.join("gpcDealer");
            Join<GpcDealer, GpcRequest> gpcDealerGpcRequestJoin = gpcDealerBodyCodeMappingGpcDealerJoin.join("gpcRequest");
            Join<GpcRequest, State> gpcRequestStateJoin = gpcDealerGpcRequestJoin.join("state");
            Join<GpcRequest, Fin> gpcRequestFinJoin = gpcDealerGpcRequestJoin.join("fin");

            List<Predicate> predicates = new ArrayList<>();
            addModelYearVehicleLineBodyCodePredicates(gpcLookupRequest, criteriaBuilder, bodyCodeVehicleLineModelYearJoin, predicates, gpcDealerBodyCodeMappingJoin);

            if (!Constants.ALL.equalsIgnoreCase(gpcLookupRequest.getStateCode())) {
                Predicate stateCodePredicate = criteriaBuilder.equal(gpcRequestStateJoin.get("stateCode"), gpcLookupRequest.getStateCode());
                predicates.add(stateCodePredicate);
            }

            if (!Constants.ALL.equalsIgnoreCase(gpcLookupRequest.getFinCode())) {
                Predicate finCodePredicate = criteriaBuilder.equal(gpcRequestFinJoin.get("finCode"), gpcLookupRequest.getFinCode());
                predicates.add(finCodePredicate);
            }

            if (gpcLookupRequest.getBidOpenDate() != null && Constants.DEALER_USER_TYPE.equalsIgnoreCase(gpcLookupRequest.getUserType())) {
                Predicate dealerBidOpenDatePredicate = criteriaBuilder.equal(gpcDealerBodyCodeMappingGpcDealerJoin.get("dealerBidOpenDate"), gpcLookupRequest.getBidOpenDate());
                predicates.add(dealerBidOpenDatePredicate);
                Predicate dealerCodePredicate = criteriaBuilder.equal(gpcDealerBodyCodeMappingGpcDealerJoin.get("dealerCode"), gpcLookupRequest.getUserId());
                predicates.add(dealerCodePredicate);
            }

            if (!Constants.ALL.equalsIgnoreCase(gpcLookupRequest.getGpcType())) {
                Predicate gpcTypePredicate = criteriaBuilder.equal(referenceNumberTypeJoin.get("referenceNumberTypeName"), gpcLookupRequest.getGpcType());
                predicates.add(gpcTypePredicate);
            }

            if (gpcLookupRequest.getBidOpenDate() != null && Constants.INTERNAL_USER_TYPE.equalsIgnoreCase(gpcLookupRequest.getUserType())) {
                Predicate internalUserBidOpenDatePredicate = criteriaBuilder.equal(gpcDealerGpcRequestJoin.get("bidOpenDate"), gpcLookupRequest.getBidOpenDate());
                predicates.add(internalUserBidOpenDatePredicate);
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addModelYearVehicleLineBodyCodePredicates(GpcLookupRequest gpcLookupRequest, CriteriaBuilder criteriaBuilder, Join<BodyCode, VehicleLineModelYear> bodyCodeVehicleLineModelYearJoin, List<Predicate> predicates, Join<GpcDealerBodyCodeMapping, BodyCode> gpcDealerBodyCodeMappingJoin) {
        if (!Constants.ALL.equalsIgnoreCase(gpcLookupRequest.getModelYear())) {
            Predicate modelYearPredicate = criteriaBuilder.equal(bodyCodeVehicleLineModelYearJoin.get("modelYearCode"), gpcLookupRequest.getModelYear());
            predicates.add(modelYearPredicate);
        }

        if (!Constants.ALL.equalsIgnoreCase(gpcLookupRequest.getVehicleLineCode())) {
            Predicate vehicleLineCodePredicate = criteriaBuilder.equal(bodyCodeVehicleLineModelYearJoin.get("vehicleLineCode"), gpcLookupRequest.getVehicleLineCode());
            predicates.add(vehicleLineCodePredicate);
        }

        Optional.ofNullable(gpcLookupRequest.getBodyCodes()).ifPresent(bodyCodes -> gpcDealerBodyCodeMappingJoin.get("bodyReferenceCode").in(gpcLookupRequest.getBodyCodes()));
    }
}

 