package com.fordpro.cloudrun.domain.entity;

import com.fordpro.cloudrun.domain.entitybase.BaseDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "gbms_fin_mapping_request")
@AttributeOverride(name = "createUser", column = @Column(name = "create_user"))
@AttributeOverride(name = "createTime", column = @Column(name = "create_time"))

@AttributeOverride(name = "updateUser", column = @Column(name = "last_update_user"))

@AttributeOverride(name = "updateTime", column = @Column(name = "last_update_time"))
public class FinMappingRequest extends BaseDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fin_mapping_request_sequence_generator")
    @SequenceGenerator(name = "fin_mapping_request_sequence_generator", sequenceName = "MGBNG31_FIN_MAP_REQ_SQ", allocationSize = 1)
    @Column(name = "fin_mapping_request_key")
    private Integer finMappingRequestKey;

    @Column(name = "request_status_flag")
    private String requestStatusFlag;

    @Column(name = "request_submit_date")
    @Temporal(TemporalType.DATE)
    private Date requestSubmitDate;

    @Column(name = "approved_rejected_date")
    @Temporal(TemporalType.DATE)
    private Date approvedOrRejectedDate;

    @Column(name = "approved_rejected_cds_id")
    private String approvedOrRejectedCdsId;

    @Column(name = "comment_x")
    private String comments;

    @Column(name = "request_for_flag")
    private String requestForFlag;

    @Column(name = "requested_cds_id")
    private String requestedCdsId;

    @Column(name = "dealer_code")
    private String dealerCode;
}

 