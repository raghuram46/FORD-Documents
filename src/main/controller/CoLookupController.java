package com.fordpro.cloudrun.carryoverlookup;

import com.fordpro.cloudrun.carryoverlookup.api.*;

import com.fordpro.cloudrun.models.StandardErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/carryover/lookup")
@Validated
@AllArgsConstructor
public class CarryOverLookupController {

    private final CarryOverLookUpService carryOverLookUpService;

    @Operation(summary = "Price Protection - VL and MY records", description = "API to fetch vehicle lines and new model years")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(maxItems = 5000, schema = @Schema(implementation = VehicleModelYearDetails.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Page Not Found", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class)))})
    @GetMapping(value = "/vehicleLinesAndNewModelYears", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<VehicleModelYearDetails> fetchVehicleLinesAndModelYears() {
        return carryOverLookUpService.getVehicleLinesAndModelYears();
    }

    @Operation(summary = "Price Protection - Prior Model Years", description = "API to Get Prior Model Years")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(maxItems = 5000, schema = @Schema(implementation = String.class)))),
            @ApiResponse(description = "All Errors", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = StandardErrorResponse.class)))
    })
    @GetMapping(value = "/priorModelYears", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> fetchPriorModelYears(@RequestParam
                                                              @NotNull(message = "vehicleLineCode should not be null")
                                                              @Size(min = 2, max = 2, message = "Invalid length of vehicleLineCode ")
                                                              @Pattern(regexp = "^\\d{2}$", message = "Invalid Vehicle Line code")
                                                              @Schema(example = "24")
                                                              String vehicleLineCode,
                                                              @RequestParam
                                                              @NotNull(message = "newModelYear should not be null")
                                                              @Min(value = 2000)
                                                              @Max(value = 2099)
                                                              @Schema(example = "2024", type = "integer", format = "int32")
                                                              Integer newModelYear,
                                                              @RequestParam
                                                              @Schema(nullable = true)
                                                              @Size(max = 50, message = "Invalid length of vehicleLineName ")
                                                              @Pattern(regexp = "^.{0,50}$")
                                                              String vehicleLineName) {
        return new ResponseEntity<>(carryOverLookUpService.getPriorModelYears(vehicleLineCode, vehicleLineName, newModelYear), HttpStatus.OK);
    }

    @Operation(summary = "Price Protection Vin records", description = "Fetches Vin details based on Lookup filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(maxItems = 300, schema = @Schema(implementation = VinBodyStyleResponseApi.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Page Not Found", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
           @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class)))})
    @PostMapping(value = "/vins", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<VinBodyStyleResponseApi> fetchVinDetail(@RequestBody @Valid VinRequest vinRequest) {
        return carryOverLookUpService.fetchVinResponse(vinRequest);
    }

    @Operation(summary = "Prior Order number validations", description = "Validates the order number and corresponding end user fin code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PriorDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Page Not Found", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class)))})
    @PostMapping(value = "/priorOrderNumberValidator", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public PriorDetailResponse priorOrderValidator(@RequestBody @Valid PriorOrderValidatorRequest orderValidatorRequest) {
        return carryOverLookUpService.priorOrderValidator(orderValidatorRequest);
    }

    @Operation(summary = "Prior reference number validations", description = "Validates the reference number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PriorDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Page Not Found", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = StandardErrorResponse.class)))})
    @PostMapping(value = "/priorRefNumberValidator", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public PriorDetailResponse priorRefNumberValidator(@RequestBody @Valid PriorRefNumValidatorRequest validatorRequest) {
        return carryOverLookUpService.priorRefNumberValidator(validatorRequest);
    }

}

 