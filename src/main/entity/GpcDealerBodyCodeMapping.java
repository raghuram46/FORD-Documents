package com.ford.protech.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "gbms_gpc_dealer_body_code_mapping")
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@AttributeOverride(name = "createUser", column = @Column(name = "create_user"))
@AttributeOverride(name = "createTime", column = @Column(name = "create_time"))
@AttributeOverride(name = "updateUser", column = @Column(name = "last_update_user"))
@AttributeOverride(name = "updateTime", column = @Column(name = "last_update_time"))
@NamedEntityGraph(name = "GpcDealerBodyCodeMappingEntityGraph",
        attributeNodes = {
                @NamedAttributeNode("gpcDealerBodyCodeMappingKey"),
                @NamedAttributeNode("requestedGpcAmount"),
                @NamedAttributeNode("priceLevel"),
                @NamedAttributeNode(value = "gpcDealer", subgraph = "gpcDealerSubgraph"),
                @NamedAttributeNode(value = "bodyCode", subgraph = "bodyCodeSubgraph"),
        },
        subgraphs = {
                @NamedSubgraph(name = "gpcDealerSubgraph", attributeNodes = {
                        @NamedAttributeNode("gpcDealerKey"),
                        @NamedAttributeNode("dealerCode"),
                        @NamedAttributeNode(value = "gpcRequest", subgraph = "gpcRequestSubgraph")
                }),
                @NamedSubgraph(name = "gpcRequestSubgraph", attributeNodes = {
                        @NamedAttributeNode("gpcRequestKey"),
                        @NamedAttributeNode("modelYear"),
                        @NamedAttributeNode("bidOpenDate"),
                        @NamedAttributeNode("gpcExpiryDate"),
                        @NamedAttributeNode("revisedBidOpenDate"),
                        @NamedAttributeNode(value = "state", subgraph = "stateSubgraph"),
                        @NamedAttributeNode(value = "fin", subgraph = "finSubgraph")
                }),
                @NamedSubgraph(name = "stateSubgraph", attributeNodes = {
                        @NamedAttributeNode("stateKey"),
                        @NamedAttributeNode(value = "stateCode"),
                        @NamedAttributeNode("stateName")
                }),
                @NamedSubgraph(name = "finSubgraph", attributeNodes = {
                        @NamedAttributeNode("finKey"),
                        @NamedAttributeNode(value = "finCode")
                }),
                @NamedSubgraph(name = "bodyCodeSubgraph", attributeNodes = {
                        @NamedAttributeNode("bodyKey"),
                        @NamedAttributeNode("bodyReferenceCode"),
                        @NamedAttributeNode("bodyShortDetails"),
                        @NamedAttributeNode(value = "vehicleLineModelYear", subgraph = "vehicleLineModelYearSubgraph")
                }),
                @NamedSubgraph(name = "vehicleLineModelYearSubgraph", attributeNodes = {
                        @NamedAttributeNode("vehicleLineKey"),
                        @NamedAttributeNode("vehicleLineCode"),
                        @NamedAttributeNode("vehicleLineName"),
                        @NamedAttributeNode("modelYearCode")
                })
        }
)

public class GpcDealerBodyCodeMapping extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gpc_dealer_body_code_mapping_key")

    private Integer gpcDealerBodyCodeMappingKey;


    @OneToOne
    @JoinColumn(name = "gpc_dealer_key")
    private GpcDealer gpcDealer;

    @OneToOne
    @JoinColumn(name = "body_key")
    private BodyCode bodyCode;

    @OneToOne
    @JoinColumn(name = "reference_number_key")
    private ReferenceNumber referenceNumber;

    @Column(name = "expect_to_sell_quantity")
    private Integer expectToSellQuantity;

    @Column(name = "price_level")
    private String priceLevel;

    @Column(name = "assigned_gpc_amount")
    private Integer assignedGpcAmount;

    @Column(name = "requested_gpc_amount")
    private Integer requestedGpcAmount;

    @Column(name = "local_body_revision_flag")
    private String localBodyRevisionFlag;

    @Column(name = "gpc_type_code")
    private String gpcTypeCode;

    @Column(name = "mail_send_flag")
    private String mailSendFlag;

    @Column(name = "post_price_flag")
    private String postPriceFlag;

    @Column(name = "post_submitted_flag")
    private String postSubmittedFlag;
}

 