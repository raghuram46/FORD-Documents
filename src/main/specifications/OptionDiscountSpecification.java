package com.ford.protech.specifications;

import com.ford.protech.domain.entity.OptionDiscount;
import com.ford.protech.optiondiscount.api.OptionDiscountRequest;

import com.ford.protech.utils.*;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor

public class OptionDiscountSpecification {


    private final SpecificationBuilder specificationBuilder;

    public Specification<OptionDiscount> getOptionDiscountLookupSpecification(OptionDiscountRequest optionDiscountRequest, String stateCodeSearchProperty) {
        List<FilterCriteria> filterCriteriaList = new ArrayList<>();
        if (!Constants.ALL.equals(optionDiscountRequest.getStateCode())) {
            FilterCriteria stateCodeFilterCriteria = FilterCriteria.builder().field(stateCodeSearchProperty).operator(QueryOperatorEnum.EQUALS).value(optionDiscountRequest.getStateCode()).isOrPredicate(false).build();
            filterCriteriaList.add(stateCodeFilterCriteria);
        }

        if (!Constants.ALL.equals(optionDiscountRequest.getFinCode())) {
            String finCodeSearchProperty = PaginationAndDataSearchSortUtil.getPropertyPathFromRootEntity(OptionDiscount.class, Constants.FIN_CODE_FIELD_NAME);
            FilterCriteria finCodeFilterCriteria = FilterCriteria.builder().field(finCodeSearchProperty).operator(QueryOperatorEnum.EQUALS).value(optionDiscountRequest.getFinCode()).isOrPredicate(false).build();
            filterCriteriaList.add(finCodeFilterCriteria);
        }

        if (optionDiscountRequest.getBodyCode() != null && !Constants.ALL.equals(optionDiscountRequest.getBodyCode())) {
            String bodyCodeSearchProperty = PaginationAndDataSearchSortUtil.getPropertyPathFromRootEntity(OptionDiscount.class, Constants.BODY_CODE_FIELD_NAME);
            FilterCriteria bodyCodeFilterCriteria = FilterCriteria.builder().field(bodyCodeSearchProperty).operator(QueryOperatorEnum.EQUALS).value(optionDiscountRequest.getBodyCode()).isOrPredicate(false).build();
            filterCriteriaList.add(bodyCodeFilterCriteria);
        }

        if (optionDiscountRequest.getControllerApprovedStatus() == null) {
            optionDiscountRequest.setControllerApprovedStatus(Constants.ALL);
        }

        if (!Constants.ALL.equals(optionDiscountRequest.getControllerApprovedStatus())) {
            String optionStatusCodeSearchProperty = PaginationAndDataSearchSortUtil.getPropertyPathFromRootEntity(OptionDiscount.class, Constants.OPTION_STATUS_CODE_FIELD_NAME);
            OptionDiscountStatusEnum optionDiscountStatus = OptionDiscountStatusEnum.fromStatusName(optionDiscountRequest.getControllerApprovedStatus());
            FilterCriteria controllerApprovedStatusFilterCriteria = FilterCriteria.builder().field(optionStatusCodeSearchProperty).operator(QueryOperatorEnum.EQUALS).value(optionDiscountStatus.name()).isOrPredicate(false).build();
            filterCriteriaList.add(controllerApprovedStatusFilterCriteria);
        }
        if (optionDiscountRequest.getOptionCodes() != null) {
            String optionCodeSearchProperty = PaginationAndDataSearchSortUtil.getPropertyPathFromRootEntity(OptionDiscount.class, Constants.OPTION_CODE_FIELD_NAME);
            FilterCriteria optionCodeFilterCriteria = FilterCriteria.builder().field(optionCodeSearchProperty).operator(QueryOperatorEnum.IN).values(optionDiscountRequest.getOptionCodes()).isOrPredicate(false).build();
            filterCriteriaList.add(optionCodeFilterCriteria);
        }

        if (optionDiscountRequest.getBodyCodes() != null) {
            String bodyCodesSearchProperty = PaginationAndDataSearchSortUtil.getPropertyPathFromRootEntity(OptionDiscount.class, Constants.BODY_CODE_FIELD_NAME);
            FilterCriteria bodyCodesFilterCriteria = FilterCriteria.builder().field(bodyCodesSearchProperty).operator(QueryOperatorEnum.IN).values(optionDiscountRequest.getBodyCodes()).isOrPredicate(false).build();
            filterCriteriaList.add(bodyCodesFilterCriteria);
        }
        return specificationBuilder.getSpecificationBasedOnFilters(filterCriteriaList);
    }

    public Specification<OptionDiscount> getOptionDiscountDataSpecification(OptionDiscountRequest optionDiscountRequest) {
        List<FilterCriteria> filterCriteriaList = new ArrayList<>();
        String optionDiscountStartYearSearchProperty = PaginationAndDataSearchSortUtil.getPropertyPathFromRootEntity(OptionDiscount.class, Constants.OPTION_DISCOUNT_START_YEAR_FIELD_NAME);
        FilterCriteria optionDiscountStartYearFilterCriteria = FilterCriteria.builder().field(optionDiscountStartYearSearchProperty).operator(QueryOperatorEnum.EQUALS).value(DateUtil.formatDate(optionDiscountRequest.getStartDate())).isOrPredicate(false).build();
        filterCriteriaList.add(optionDiscountStartYearFilterCriteria);
        String takeRateSearchProperty = PaginationAndDataSearchSortUtil.getPropertyPathFromRootEntity(OptionDiscount.class, Constants.TAKE_RATE_FIELD_NAME);
        FilterCriteria takeRateFilterCriteria = FilterCriteria.builder().field(takeRateSearchProperty).operator(QueryOperatorEnum.EQUALS).value(String.valueOf(optionDiscountRequest.getTakeRatePercent())).isOrPredicate(false).build();
        filterCriteriaList.add(takeRateFilterCriteria);
        String optionAmountSearchProperty = PaginationAndDataSearchSortUtil.getPropertyPathFromRootEntity(OptionDiscount.class, Constants.OPTION_AMOUNT_FIELD_NAME);
        FilterCriteria optionAmountFilterCriteria = FilterCriteria.builder().field(optionAmountSearchProperty).operator(QueryOperatorEnum.EQUALS).value(String.valueOf(optionDiscountRequest.getDiscountAmount())).isOrPredicate(false).build();
        filterCriteriaList.add(optionAmountFilterCriteria);
        if (optionDiscountRequest.getExpirationDate() != null) {
            String optionDiscountExpirationYearSearchProperty = PaginationAndDataSearchSortUtil.getPropertyPathFromRootEntity(OptionDiscount.class, Constants.OPTION_DISCOUNT_EXP_YEAR_FIELD_NAME);
            FilterCriteria optionDiscountExpirationYearFilterCriteria = FilterCriteria.builder().field(optionDiscountExpirationYearSearchProperty).operator(QueryOperatorEnum.EQUALS).value(DateUtil.formatDate(optionDiscountRequest.getExpirationDate())).isOrPredicate(false).build();
            filterCriteriaList.add(optionDiscountExpirationYearFilterCriteria);
        }
        return specificationBuilder.getSpecificationBasedOnFilters(filterCriteriaList);
    }
}

 