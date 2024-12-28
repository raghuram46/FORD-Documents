package com.ford.protech.specifications;

import com.ford.protech.domain.entity.*;

import com.ford.protech.utils.Constants;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class LookupSpecification {

    public Specification<Fin> getTYPPAFins() {
        return (Root<Fin> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) -> {
            Join<Fin, Segment> finSegmentJoin = root.join("segment", JoinType.INNER);
            Join<Fin, FinType> finTypeJoin = root.join("finType", JoinType.INNER);

            Root<FinParentChildMapping> finParentChildMappingRoot = criteriaQuery.from(FinParentChildMapping.class);
            Join<FinParentChildMapping, Fin> childFinJoin = finParentChildMappingRoot.join("childFin", JoinType.INNER);
            Join<Fin, FinType> finTypeJoin1 = childFinJoin.join("finType", JoinType.INNER);


            Predicate predicate1 = criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("operatingUnitCode"), Constants.ODS_OPERATING_UNIT_CODE),
                    finSegmentJoin.get("segmentCode").in("G", "M", "L"),
                    criteriaBuilder.equal(finTypeJoin.get("finTypeKey"), Constants.TVPPA_FIN_TYPE)
            );

            Predicate predicate2 = criteriaBuilder.and(
                    criteriaBuilder.equal(finParentChildMappingRoot.get("mappingStatus"), Constants.APPROVED_STATUS_CODE),
                    criteriaBuilder.isNull(finParentChildMappingRoot.get("inactivateDate")),
                    criteriaBuilder.equal(finTypeJoin1.get("finTypeKey"), Constants.TVPPA_FIN_TYPE)
            );
            return criteriaBuilder.and(predicate1, predicate2);

        };
    }
}

 