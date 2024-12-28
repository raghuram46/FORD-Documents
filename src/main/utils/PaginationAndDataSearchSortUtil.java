package com.ford.protech.utils;

import com.ford.protech.domain.entity.AuditFields;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class PaginationAndDataSearchSortUtil {

    private PaginationAndDataSearchSortUtil() {
    }

    public static Pageable configurePaginationWithDataSorting(Class<?> className, Integer pageNumber, Integer pageSize, String sort) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        if (sort != null && !sort.isEmpty()) {
            List<String> splitSortValue = getSplitSortValue(sort);
            Sort.Direction direction = Sort.Direction.ASC;
            if (Objects.equals(splitSortValue.get(1), "desc")) {
                direction = Sort.Direction.DESC;
            }
            String absoluteEntityPropertyPath = getAbsoluteEntityPropertyPath(className, "", splitSortValue.get(0));
            pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, absoluteEntityPropertyPath));
        }
        return pageable;
    }

    public static String getPropertyPathFromRootEntity(Class<?> className, String searchPropertyName) {
        String absoluteEntityPropertyPath = null;
        if (searchPropertyName != null && !searchPropertyName.isEmpty()) {
            absoluteEntityPropertyPath = getAbsoluteEntityPropertyPath(className, "", getSplitSortValue(searchPropertyName).get(0));
        }
        return absoluteEntityPropertyPath;
    }

    private static List<String> getSplitSortValue(String sort) {
        return Arrays.stream(sort.split(Constants.COMMA)).toList();
    }

    private static String getAbsoluteEntityPropertyPath(Class<?> className, String classNamePrefix, String sortFieldName) {
        List<Class<? extends Serializable>> preDefinedClasses = List.of(Integer.class, Boolean.class, Double.class, String.class, Character.class, BigDecimal.class, UUID.class, Date.class, Timestamp.class);
        List<Field> classFields = Arrays.stream(className.getDeclaredFields()).collect(Collectors.toCollection(ArrayList::new));
        Class<?> superclass = className.getSuperclass();
        if (superclass == AuditFields.class) {
            List<Field> superClassFields = Arrays.stream(superclass.getDeclaredFields()).toList();
            classFields.addAll(superClassFields);
        }
        for (Field classField : classFields) {
            String absoluteEntityPropertyPath = getEntityName(classNamePrefix, sortFieldName, classField, preDefinedClasses);
            if (absoluteEntityPropertyPath != null) return absoluteEntityPropertyPath;
        }
        return null;

    }

    private static String getEntityName(String classNamePrefix, String sortFieldName, Field classField, List<Class<? extends Serializable>> preDefinedClasses) {
        String entityFieldName;
        Class<?> fieldType = classField.getType();
        fieldType = Set.class.isAssignableFrom(fieldType) ? (Class<?>) ((ParameterizedType) classField.getGenericType()).getActualTypeArguments()[0] : fieldType;
        if (!preDefinedClasses.contains(fieldType) && !isBiDirectionalEntityRelation(classNamePrefix)) {
            String prefix = classNamePrefix.isEmpty() ? classField.getName() : classNamePrefix + "." + classField.getName();
            String absoluteEntityPropertyPath = getAbsoluteEntityPropertyPath(fieldType, prefix, sortFieldName);
            if (absoluteEntityPropertyPath != null) {
                return absoluteEntityPropertyPath;
            }
        } else if (!isBiDirectionalEntityRelation(classNamePrefix)) {
            entityFieldName = getEntityFieldName(classNamePrefix, sortFieldName, classField);
            if (entityFieldName != null) {
                return entityFieldName;
            }
        }
        return null;
    }


    private static String getEntityFieldName(String classNamePrefix, String sortFieldName, Field classField) {
        String entityFieldName;
        if (!classNamePrefix.isEmpty()) {
            entityFieldName = classNamePrefix + "." + classField.getName();
        } else {
            entityFieldName = classField.getName();
        }
        if (entityFieldName.contains(sortFieldName)) {
            return entityFieldName;
        }
        return null;
    }

    private static boolean isBiDirectionalEntityRelation(String input) {
        Set<String> uniqueWords = Arrays.stream(input.split(Constants.PERIOD))
                .collect(Collectors.toSet());
        return uniqueWords.size() != Arrays.stream(input.split(Constants.PERIOD)).count();
    }


}