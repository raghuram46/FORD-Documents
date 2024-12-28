package com.ford.protech.specifications;

import com.ford.protech.exception.ManageGpcException;
import com.ford.protech.utils.Constants;
import com.ford.protech.utils.FilterCriteria;
import com.ford.protech.utils.PaginationAndDataSearchSortUtil;
import com.ford.protech.utils.QueryOperatorEnum;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;

import lombok.SneakyThrows;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import java.util.*;


import static org.springframework.util.ObjectUtils.isEmpty;

@Component
@AllArgsConstructor

public class SpecificationBuilder {


    public <T> Specification<T> getSpecificationBasedOnFilters(List<FilterCriteria> filterCriteria) {
        if (isEmpty(filterCriteria)) {
            return (root, query, criteriaBuilder) -> null;
        }

        Specification<T> specification = Specification.where(createSpecification(filterCriteria.get(0)));

        for (int i = 1; i < filterCriteria.size(); i++) {
            specification = filterCriteria.get(i).isOrPredicate() ? specification.or(createSpecification(filterCriteria.get(i))) : specification.and(createSpecification(filterCriteria.get(i)));
        }
        return specification;
    }

    @SafeVarargs
    public final <T> Specification<T> createLogicalSpecification(String operation, Specification<T>... specifications) {
        return createLogicalSpecification(operation, Arrays.asList(specifications));
    }

    public final <T> Specification<T> createLogicalSpecification(String operation, List<Specification<T>> specifications) {
        if (isEmpty(specifications)) {
            return (root, query, criteriaBuilder) -> null;
        } else {
            return (root, query, criteriaBuilder) -> {
                Predicate predicate = null;
                List<Predicate> predicates = specifications.stream().map(specification -> specification.toPredicate(root, query, criteriaBuilder)).toList();
                if (Constants.AND_OPERATION.equals(operation)) {
                    predicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                } else if (Constants.OR_OPERATION.equals(operation)) {
                    predicate = criteriaBuilder.or(predicates.toArray(new Predicate[0]));
                }
                return predicate;
            };
        }
    }

    public <T> List<FilterCriteria> getFilterParamCriteria(String filterParam, Class<T> type) {
        List<FilterCriteria> filterCriteriaList = new ArrayList<>();
        if (filterParam.contains(Constants.COMMA)) {
            List<String> searchKeywords = Arrays.stream(filterParam.split(Constants.COMMA)).filter(StringUtils::isNotBlank).map(String::trim).toList();
            List<FilterCriteria> multipleFilterCriterionCriteria = searchKeywords.stream().map(searchKeyword -> {
                List<String> splitSearchValue = Arrays.asList(searchKeyword.split(Constants.EQUALS_OPERATOR));
                String searchProperty = PaginationAndDataSearchSortUtil.getPropertyPathFromRootEntity(type, splitSearchValue.get(0));
                return FilterCriteria.builder().isOrPredicate(false).field(searchProperty).operator(QueryOperatorEnum.LIKE).value(splitSearchValue.get(1)).build();
            }).toList();

            filterCriteriaList.addAll(multipleFilterCriterionCriteria);

        } else {
            List<String> splitSearchValue = Arrays.asList(filterParam.split(Constants.EQUALS_OPERATOR));
            String searchProperty = PaginationAndDataSearchSortUtil.getPropertyPathFromRootEntity(type, splitSearchValue.get(0));
            FilterCriteria singleFilterCriteria = FilterCriteria.builder().isOrPredicate(false).field(searchProperty).operator(QueryOperatorEnum.LIKE).value(splitSearchValue.get(1)).build();
            filterCriteriaList.add(singleFilterCriteria);

        }
        return filterCriteriaList;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private <T> Specification<T> createSpecification(FilterCriteria filterCriteria) {
        return switch (filterCriteria.getOperator()) {
            case EQUALS -> (root, query, criteriaBuilder) -> {
                if (filterCriteria.getField().contains(".")) {
                    Path<T> pathForNestedField = getAttributePathForNestedField(filterCriteria, root);
                    return criteriaBuilder.equal(pathForNestedField, castToRequiredType(pathForNestedField.getJavaType(), filterCriteria.getValue()));
                } else {
                    return criteriaBuilder.equal(root.get(filterCriteria.getField()), castToRequiredType(root.get(filterCriteria.getField()).getJavaType(), filterCriteria.getValue()));
                }
            };
            case NOT_EQUALS -> (root, query, criteriaBuilder) ->
                    criteriaBuilder.notEqual(root.get(filterCriteria.getField()), castToRequiredType(root.get(filterCriteria.getField()).getJavaType(), filterCriteria.getValue()));
            case GREATER_THAN -> (root, query, criteriaBuilder) ->
                    criteriaBuilder.gt(root.get(filterCriteria.getField()), (Number) castToRequiredType(root.get(filterCriteria.getField()).getJavaType(), filterCriteria.getValue()));
            case LESS_THAN -> (root, query, criteriaBuilder) ->
                    criteriaBuilder.lt(root.get(filterCriteria.getField()), (Number) castToRequiredType(root.get(filterCriteria.getField()).getJavaType(), filterCriteria.getValue()));
            case LIKE -> (root, query, criteriaBuilder) -> {
                if (filterCriteria.getField().contains(".")) {
                    Path<T> pathForNestedField = getAttributePathForNestedField(filterCriteria, root);
                    return criteriaBuilder.like(pathForNestedField.as(String.class), "%" + filterCriteria.getValue() + "%");
                } else {
                    return criteriaBuilder.like(root.get(filterCriteria.getField()), "%" + filterCriteria.getValue() + "%");
                }
            };
            case IN -> (root, query, criteriaBuilder) -> {
                if (filterCriteria.getField().contains(".")) {
                    Path<T> pathForNestedField = getAttributePathForNestedField(filterCriteria, root);
                    return criteriaBuilder.in(pathForNestedField).value((T) castToRequiredType(pathForNestedField.getJavaType(), filterCriteria.getValues()));
                } else {
                    return criteriaBuilder.in(root.get(filterCriteria.getField())).value(castToRequiredType(root.get(filterCriteria.getField()).getJavaType(), filterCriteria.getValues()));
                }
            };
            case YEARS_IN -> ((root, query, criteriaBuilder) -> {
                Predicate predicate = null;
                for (String year : filterCriteria.getValues()) {
                    Expression<String> dateStringExpression = criteriaBuilder.function("TO_CHAR", String.class, root.get(filterCriteria.getField()), criteriaBuilder.literal("YYYY"));
                    Expression<String> yearExpression = criteriaBuilder.function("SUBSTRING", String.class, dateStringExpression, criteriaBuilder.literal(1), criteriaBuilder.literal(4));
                    Predicate yearPredicate = criteriaBuilder.equal(yearExpression, year);
                    if (predicate == null) {
                        predicate = yearPredicate;
                    } else {
                        predicate = criteriaBuilder.or(predicate, yearPredicate);
                    }
                }
                return predicate;
            });
            case NULL -> (root, query, criteriaBuilder) ->
                    criteriaBuilder.isNull(root.get(filterCriteria.getField()));
            default -> throw new ManageGpcException("Operation not supported yet");
        };
    }

    private static <T> Path<T> getAttributePathForNestedField(FilterCriteria filterCriteria, Root<T> root) {
        List<String> innerJoinFields = Arrays.asList(filterCriteria.getField().split(Constants.PERIOD));
        Path<T> path = root;
        for (String part : innerJoinFields) {
            path = path.get(part);
        }
        return path;
    }

    @SneakyThrows
    private <T> Object castToRequiredType(Class<T> fieldType, String value) {
        if (fieldType.isAssignableFrom(Double.class)) {
            return Double.valueOf(value);
        } else if (fieldType.isAssignableFrom(Integer.class)) {
            return Integer.valueOf(value);
        } else if (String.class.isAssignableFrom(fieldType)) {
            return String.valueOf(value);
        } else if (fieldType.isAssignableFrom(Boolean.class)) {
            return Boolean.valueOf(value);
        } else if (UUID.class.isAssignableFrom(fieldType)) {
            return UUID.fromString(value);
        } else if (fieldType.isAssignableFrom(BigDecimal.class)) {
            return new BigDecimal(value);
        } else if (fieldType.isAssignableFrom(Date.class)) {
            return new SimpleDateFormat("yyyy-MM-dd").parse(value);
        } else if (fieldType.isAssignableFrom(Character.class)) {
            return value.charAt(0);
        }
        return null;
    }

    private <T> Object castToRequiredType(Class<T> fieldType, List<String> values) {
        List<Object> lists = new ArrayList<>();
        for (String value : values) {
            lists.add(castToRequiredType(fieldType, value));
        }
        return lists;
    }

}

 