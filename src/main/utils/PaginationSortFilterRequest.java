package com.ford.protech.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@Schema(description = "Pagination, Sorting and Filtering Request for APIs")
public class PaginationSortFilterRequest {

    @Min(value = 0)
    @Max(value = 5000)
    @Schema(example = "0", type = "integer", format = "int32", description = "Page number of the result set")
    Integer pageNumber;

    @Min(value = 10)
    @Max(value = 100)
    @Schema(example = "10", type = "integer", format = "int32", description = "Number of records per page")
    Integer pageSize;

    @Size(max = 35, message = "Invalid length of sort")
    @Pattern(regexp = "^[A-Za-z]+,(asc|desc)$")
    @Schema(example = "bodyReferenceCode,asc", type = "string", description = "Sort the result set by given field name")
    String sort;

    @Nullable
    @Pattern(regexp = "^([A-Za-z]+=[A-Za-z0-9-/# ]+(,[A-Za-z]+=[A-Za-z0-9-/# ]+){0,11})?$")
    @Size(max=1000)
    @Schema(example = "finCode=KA001,stateCode=FG", type = "string", description = "one or more search parameters")
    String filterParam;
}