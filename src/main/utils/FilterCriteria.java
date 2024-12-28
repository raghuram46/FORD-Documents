package com.ford.protech.utils;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
@Builder
public class FilterCriteria {
    private String field;
    private QueryOperatorEnum operator;
    private String value;
    private Date dateValue;
    private List<String> values;
    private boolean isOrPredicate;
}