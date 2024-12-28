package com.ford.protech.specifications;

import com.ford.protech.domain.entity.BodyCode;
import com.ford.protech.domain.entity.BodyCodeKeyDates;
import com.ford.protech.domain.entity.Budget;
import com.ford.protech.domain.entity.VehicleLineModelYear;
import com.ford.protech.utils.*;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component

public class ModelYearVehicleLineBodyCodeSpecification {

    private SpecificationBuilder specificationBuilder;

    @Autowired
    public ModelYearVehicleLineBodyCodeSpecification(SpecificationBuilder specificationBuilder) {
        this.specificationBuilder = specificationBuilder;
    }

    public ModelYearVehicleLineBodyCodeSpecification(){}

    public <T> Specification<T> getModelYearVehicleLineBodyCodeSpecification(Class<T> className, Integer modelYear, String vehicleLineCode, List<String> bodyCodes) {
        List<FilterCriteria> filterCriteriaList = new ArrayList<>();

        String modelYearCodeSearchProperty = PaginationAndDataSearchSortUtil.getPropertyPathFromRootEntity(className, Constants.MODEL_YEAR_FIELD_NAME);
        FilterCriteria modelYearCodeCriteria = FilterCriteria.builder().isOrPredicate(false).field(modelYearCodeSearchProperty).operator(QueryOperatorEnum.EQUALS).value(String.valueOf(modelYear)).build();
        filterCriteriaList.add(modelYearCodeCriteria);

        if (!Constants.ALL.equals(vehicleLineCode)) {
            String vehicleLineCodeSearchProperty = PaginationAndDataSearchSortUtil.getPropertyPathFromRootEntity(className, Constants.VL_CODE_FIELD_NAME);
            FilterCriteria vehicleLineCodeFilterCriteria = FilterCriteria.builder().isOrPredicate(false).field(vehicleLineCodeSearchProperty).operator(QueryOperatorEnum.EQUALS).value(vehicleLineCode).build();
            filterCriteriaList.add(vehicleLineCodeFilterCriteria);
        }

        if (bodyCodes != null) {
            String bodyCodeSearchProperty = PaginationAndDataSearchSortUtil.getPropertyPathFromRootEntity(className, Constants.BODY_CODE_FIELD_NAME);
            FilterCriteria bodyReferenceCodeFilterCriteria = FilterCriteria.builder().isOrPredicate(false).field(bodyCodeSearchProperty).operator(QueryOperatorEnum.IN).values(bodyCodes).build();
            filterCriteriaList.add(bodyReferenceCodeFilterCriteria);
        }
        return specificationBuilder.getSpecificationBasedOnFilters(filterCriteriaList);
    }

    public Specification<BodyCode> getApprovedVehicleLineBodyCodesByModelYearSpecification(List<Integer> modelYears) {
        return (Root<BodyCode> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            Subquery<BodyCode> nonPoliceVehicleLinesSubquery = query.subquery(BodyCode.class);
            Root<BodyCode> bodyCodeRoot = nonPoliceVehicleLinesSubquery.from(BodyCode.class);
            Join<BodyCode, VehicleLineModelYear> bodyCodeVehicleLineModelYearJoin = bodyCodeRoot.join("vehicleLineModelYear");
            Join<BodyCode, BodyCodeKeyDates> bodyCodeBodyCodeKeyDatesJoin = bodyCodeRoot.join("bodyCodeKeyDates");
            Join<BodyCode, Budget> bodyCodeBudgetJoin = bodyCodeRoot.join("budget");
            nonPoliceVehicleLinesSubquery.select(bodyCodeVehicleLineModelYearJoin.getParent()).distinct(true);

            Predicate nonPoliceVehicleLinesPredicate = criteriaBuilder.and(
                    criteriaBuilder.equal(bodyCodeBodyCodeKeyDatesJoin.get("dealerAvailableFlag"), Constants.DEALER_AVAILABLE_FLAG),
                    criteriaBuilder.equal(bodyCodeBudgetJoin.get("controllerApprovedStatus"), Constants.CONTROLLER_APPROVED_STATUS),
                    bodyCodeVehicleLineModelYearJoin.get("modelYearCode").in(modelYears)
            );

            nonPoliceVehicleLinesSubquery.where(nonPoliceVehicleLinesPredicate);

            Subquery<BodyCode> policeVehicleLinesSubquery = query.subquery(BodyCode.class);
            Root<BodyCode> bodyCodeRoot2 = policeVehicleLinesSubquery.from(BodyCode.class);
            Join<BodyCode, VehicleLineModelYear> vehicleLineModelYearJoin = bodyCodeRoot2.join("vehicleLineModelYear");
            Join<BodyCode, BodyCodeKeyDates> bodyCodeKeyDatesJoin = bodyCodeRoot2.join("bodyCodeKeyDates");
            Join<BodyCode, Budget> budgetJoin = bodyCodeRoot2.join("budget");

            Subquery<VehicleLineModelYear> subquery = query.subquery(VehicleLineModelYear.class);
            Root<VehicleLineModelYear> subRoot = subquery.from(VehicleLineModelYear.class);
            Predicate policeVehicleLinesSubPredicate = criteriaBuilder.and(
                    criteriaBuilder.equal(subRoot.get("policeFlag"), 'Y'),
                    criteriaBuilder.equal(
                            subRoot.get("vehicleLineCode"),
                           criteriaBuilder.function("SUBSTRING", String.class,
                                    bodyCodeRoot2.get("policeVehicleCode"),
                                    criteriaBuilder.literal(1),
                                    criteriaBuilder.literal(2))
                    )
            );
            subquery.select(subRoot).where(policeVehicleLinesSubPredicate);

            Predicate policeVehicleLinesPredicate = criteriaBuilder.and(
                    criteriaBuilder.equal(bodyCodeKeyDatesJoin.get("dealerAvailableFlag"), Constants.DEALER_AVAILABLE_FLAG),
                    criteriaBuilder.equal(budgetJoin.get("controllerApprovedStatus"), Constants.CONTROLLER_APPROVED_STATUS),
                    criteriaBuilder.exists(subquery),
                    vehicleLineModelYearJoin.get("modelYearCode").in(modelYears)
            );

            policeVehicleLinesSubquery.select(vehicleLineModelYearJoin.getParent()).where(policeVehicleLinesPredicate);

            query.distinct(true);
            query.where(criteriaBuilder.or(
                    root.in(nonPoliceVehicleLinesSubquery),
                    root.in(policeVehicleLinesSubquery)
            ));

            return query.getRestriction();
        };
   }
}

 