package com.fordpro.cloudrun.optionpriceclaim;

import com.fordpro.cloudrun.priceproctectionutil.DocumentStorageService;

import com.fordpro.cloudrun.priceproctectionutil.PriceProtectionVinService;

import com.fordpro.cloudrun.carryoverclaim.api.PriceProtectionClaimResponse;
import com.fordpro.cloudrun.domain.entity.*;
import com.fordpro.cloudrun.domain.repository.*;
import com.fordpro.cloudrun.exception.PriceProtectionException;
import com.fordpro.cloudrun.optionpriceclaim.api.OptionClaimRequest;
import com.fordpro.cloudrun.optionpriceclaim.api.OptionVinInfo;
import com.fordpro.cloudrun.commonutil.Constants;
import com.fordpro.cloudrun.commonutil.DealerUserStatus;
import com.fordpro.cloudrun.commonutil.InternalUserStatus;
import com.fordpro.cloudrun.priceproctectionutil.PriceProtectionUtilService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(value = MockitoExtension.class)
class OptionPriceClaimServiceTest {
    @InjectMocks
    OptionPriceClaimService optionPriceClaimService;
    @Mock
    DealerClaimVinMappingRepository dealerClaimVinMappingRepository;
    @Mock
    FinParentChildMapRepository finParentChildMapRepository;
    @Mock
    DealerClaimRepository dealerClaimRepository;
    @Mock
    StateRepository stateRepository;
    @Mock
    ClaimVinExceptionRepository claimVinExceptionRepository;
    @Mock
    InvoiceInfoRepository invoiceInfoRepository;
    @Mock
    ReferenceNumberRepository referenceNumberRepository;
    @Mock
    FinRepository finRepository;
    @Mock
    ClaimExceptionRepository claimExceptionRepository;
    @Mock
    PriceProtectionUtilService priceProtectionUtilService;
    @Mock
    PriceProtectionVinService priceProtectionVinService;
    @Mock
    DocumentStorageService documentStorageService;

    @Test
    void shouldThrowExceptionWhenVinAlreadyExists() {
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCodeIn(any())).thenReturn(getDealerClaimVinMappingList());
        try {
            OptionClaimRequest optionClaimRequest = getOptionClaimRequest();
            optionClaimRequest.setPriceProtectionClaimKey(1);
            optionPriceClaimService.processOptionPriceProtectionClaimRequest(optionClaimRequest);
        } catch (PriceProtectionException exception) {
            assertEquals("1FTVW1EL3PWG32401 - VIN is part of existing Price Protection claims. Remove those entries to process the claim.", exception.getMessage());
        }
    }

    @Test
    void shouldThrowExceptionWhenFinIsInvalid() {
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCodeIn(any())).thenReturn(new ArrayList<>());
        List<FinParentChildMap> finParentChildMapList = new ArrayList<>();
        finParentChildMapList.add(getFinParentChildMapping());
        when(finParentChildMapRepository.findByParentFinFinCodeAndMappingStatusFlag(anyString(), anyString())).thenReturn(finParentChildMapList);
        try {
            optionPriceClaimService.processOptionPriceProtectionClaimRequest(getOptionClaimRequest());
        } catch (PriceProtectionException exception) {
            assertEquals("Sold to End User FIN does not match the FIN selected in this claim. Please change FIN in claim.", exception.getMessage());
        }
    }

    @Test
    void shouldSaveOptionPriceClaimForExistingClaim() {
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCodeIn(any())).thenReturn(getDealerClaimVinMappingList());
        List<FinParentChildMap> finParentChildMapList = new ArrayList<>();
        finParentChildMapList.add(getFinParentChildMapping2());
        when(finParentChildMapRepository.findByParentFinFinCodeAndMappingStatusFlag(anyString(), anyString())).thenReturn(finParentChildMapList);
        when(dealerClaimRepository.findByDealerClaimKey(anyInt())).thenReturn(getDealerClaim());
        when(stateRepository.findByStateCodeNot(anyString())).thenReturn(getStates());
        when(claimExceptionRepository.findById(anyInt())).thenReturn(Optional.of(ClaimIssue.builder().exceptionKey(1).build()));
        when(claimVinExceptionRepository.findByDealerClaimDealerClaimKeyAndDealerClaimVinMappingDealerClaimVinKey(anyInt(), any())).thenReturn(new ArrayList<>());
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCode(anyString())).thenReturn(getDealerClaimVinMapping());
        when(invoiceInfoRepository.findByVinCode(anyString())).thenReturn(InvoiceInfo.builder().vinCode("1FTVW1EL3PWG32401").flatPurchaseOrder("4").build());
        when(referenceNumberRepository.findByReferenceNumberCodeAndBodyBodyKey(anyString(), any())).thenReturn(getReferenceNumber());
        when(finRepository.findByFinCode(anyString())).thenReturn(Fin.builder().build());
        when(dealerClaimRepository.save(any(DealerClaim.class))).thenReturn(getDealerClaim());
        doNothing().when(dealerClaimVinMappingRepository).deleteAll(any());
        when(dealerClaimVinMappingRepository.saveAll(any())).thenReturn(new ArrayList<>());
        when(claimVinExceptionRepository.saveAll(any())).thenReturn(new ArrayList<>());
        doNothing().when(documentStorageService).saveDocuments(anyList(), anyInt(), anyInt());
        doNothing().when(priceProtectionUtilService).deletePriceInfoPriceOptionPriceConstantClaimSnapshot(any());

        PriceProtectionClaimResponse PriceProtectionClaimResponse = optionPriceClaimService.processOptionPriceProtectionClaimRequest(getOptionClaimRequest());
        verify(dealerClaimVinMappingRepository, times(1)).deleteAll(any());
        verify(dealerClaimVinMappingRepository, times(1)).saveAll(any());
        verify(claimVinExceptionRepository, times(1)).saveAll(any());
        assertEquals(44, PriceProtectionClaimResponse.getClaimId());
        assertEquals(Constants.PP_CLAIM_SAVE_MSG, PriceProtectionClaimResponse.getResponseMessage());
        assertEquals(0, optionPriceClaimService.getDeletedPrimaryVinInfoList().size());
    }

    @Test
    void shouldSaveOptionPriceClaimForNewClaim() {
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCodeIn(any())).thenReturn(new ArrayList<>());
        List<FinParentChildMap> finParentChildMapList = new ArrayList<>();
        finParentChildMapList.add(getFinParentChildMapping2());
        when(finParentChildMapRepository.findByParentFinFinCodeAndMappingStatusFlag(anyString(), anyString())).thenReturn(finParentChildMapList);
        when(stateRepository.findByStateCodeNot(anyString())).thenReturn(getStates());
        when(claimExceptionRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCode(anyString())).thenReturn(getDealerClaimVinMapping());
        when(invoiceInfoRepository.findByVinCode(anyString())).thenReturn(InvoiceInfo.builder().flatPurchaseOrder("4").build());
        when(referenceNumberRepository.findByReferenceNumberCodeAndBodyBodyKey(anyString(), any())).thenReturn(getReferenceNumber());
        when(finRepository.findByFinCode(anyString())).thenReturn(Fin.builder().build());
        when(dealerClaimRepository.save(any(DealerClaim.class))).thenReturn(getDealerClaim());
        when(dealerClaimVinMappingRepository.saveAll(any())).thenReturn(new ArrayList<>());
        when(claimVinExceptionRepository.saveAll(any())).thenReturn(new ArrayList<>());
        doNothing().when(documentStorageService).saveDocuments(anyList(), anyInt(), anyInt());
        doNothing().when(priceProtectionUtilService).deletePriceInfoPriceOptionPriceConstantClaimSnapshot(any());

        OptionClaimRequest optionClaimRequest = getOptionClaimRequest();
        optionClaimRequest.setPriceProtectionClaimKey(null);
        List<OptionVinInfo> optionVinInfoList = new ArrayList<>();
        optionVinInfoList.add(getOptionVinInfo2());
        optionClaimRequest.setOptionVinInfoList(optionVinInfoList);
        optionClaimRequest.setDeleteOptionVinInfoList(new ArrayList<>());
        PriceProtectionClaimResponse PriceProtectionClaimResponse = optionPriceClaimService.processOptionPriceProtectionClaimRequest(optionClaimRequest);
        verify(dealerClaimVinMappingRepository, times(1)).saveAll(any());
        verify(claimVinExceptionRepository, times(1)).saveAll(any());
        assertEquals(44, PriceProtectionClaimResponse.getClaimId());
        assertEquals(Constants.PP_CLAIM_SAVE_MSG, PriceProtectionClaimResponse.getResponseMessage());
    }

    @Test
    void shouldThrowExceptionWhileSavingClaim() {
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCodeIn(any())).thenReturn(new ArrayList<>());
        List<FinParentChildMap> finParentChildMapList = new ArrayList<>();
        finParentChildMapList.add(getFinParentChildMapping2());
        when(finParentChildMapRepository.findByParentFinFinCodeAndMappingStatusFlag(anyString(), anyString())).thenReturn(finParentChildMapList);
        when(dealerClaimRepository.findByDealerClaimKey(anyInt())).thenReturn(getDealerClaim());
        when(stateRepository.findByStateCodeNot(anyString())).thenReturn(getStates());
        DealerClaimVinMapping dealerClaimVinMapping = getDealerClaimVinMapping();
        dealerClaimVinMapping.setApprovalRejectFlag("R");
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCode(anyString())).thenReturn(dealerClaimVinMapping);
        when(invoiceInfoRepository.findByVinCode(anyString())).thenReturn(InvoiceInfo.builder().flatPurchaseOrder("4").build());
        when(referenceNumberRepository.findByReferenceNumberCodeAndBodyBodyKey(anyString(), any())).thenReturn(getReferenceNumber());
        when(finRepository.findByFinCode(anyString())).thenReturn(Fin.builder().build());
        when(dealerClaimRepository.save(any(DealerClaim.class))).thenThrow(PriceProtectionException.class);
        try {
            OptionClaimRequest optionClaimRequest = getOptionClaimRequest();
            List<OptionVinInfo> deleteOptionVinInfoList = new ArrayList<>();
            deleteOptionVinInfoList.add(getOptionVinInfo2());
            optionClaimRequest.setDeleteOptionVinInfoList(deleteOptionVinInfoList);
            List<OptionVinInfo> optionVinInfoList = new ArrayList<>();
            OptionVinInfo optionVinInfo = getOptionVinInfo2();
            optionVinInfo.setExceptionCodes(null);
            optionVinInfoList.add(optionVinInfo);
            optionClaimRequest.setOptionVinInfoList(optionVinInfoList);
            optionPriceClaimService.processOptionPriceProtectionClaimRequest(optionClaimRequest);
        } catch (PriceProtectionException exception) {
            verify(dealerClaimVinMappingRepository, times(0)).saveAll(any());
        }
    }

    @Test
    void shouldReturnDealerClaimVinMappingWhenDealerClaimVinMappingIsNotNull() {
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCode(anyString())).thenReturn(getDealerClaimVinMapping());
        DealerClaimVinMapping dealerClaimVinMapping = optionPriceClaimService.getExistingDealerClaimVinMapping(getOptionVinInfo1());
        assertEquals(UUID.fromString("71516fe0-e704-4104-86f6-4fa7131348a3"), dealerClaimVinMapping.getDealerClaimVinKey());
    }

    @Test
    void shouldReturnDealerClaimVinMappingWhenDealerClaimVinMappingKeyIsNotNull() {
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCode(anyString())).thenReturn(null);
        when(dealerClaimVinMappingRepository.findByDealerClaimVinKey(any())).thenReturn(getDealerClaimVinMapping());
        DealerClaimVinMapping dealerClaimVinMapping = optionPriceClaimService.getExistingDealerClaimVinMapping(getOptionVinInfo1());
        assertEquals(UUID.fromString("71516fe0-e704-4104-86f6-4fa7131348a3"), dealerClaimVinMapping.getDealerClaimVinKey());
    }

    @Test
    void shouldReturnDealerClaimVinMappingWhenDealerClaimVinMappingKeyIsNull() {
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCode(anyString())).thenReturn(null);
        OptionVinInfo optionVinInfo = getOptionVinInfo1();
        optionVinInfo.setDealerClaimVinMappingKey(null);
        DealerClaimVinMapping dealerClaimVinMapping = optionPriceClaimService.getExistingDealerClaimVinMapping(optionVinInfo);
        assertNull(dealerClaimVinMapping);
    }

    @Test
    void shouldReturnNewDealerClaimVinMapping() {
        DealerClaimVinMapping dealerClaimVinMapping = optionPriceClaimService.updateStatusFlagsToDealerClaimVinMapping(null);
        assertEquals(Constants.LOGIN_USER, dealerClaimVinMapping.getCreateUser());
        assertNotNull(dealerClaimVinMapping.getCreateTime());
        assertEquals(Constants.PENDING, dealerClaimVinMapping.getApprovalRejectFlag());
        assertEquals(Constants.PENDING, dealerClaimVinMapping.getCtlFlag());
    }

    @Test
    void shouldUpdateExistingDealerClaimVinMapping() {
        DealerClaimVinMapping dealerClaimVinMapping = getDealerClaimVinMapping();
        dealerClaimVinMapping.setApprovalRejectFlag("R");
        DealerClaimVinMapping dealerClaimVinMappingNew = optionPriceClaimService.updateStatusFlagsToDealerClaimVinMapping(dealerClaimVinMapping);
        assertEquals("P", dealerClaimVinMappingNew.getApprovalRejectFlag());
    }

    @Test
    void shouldUpdateControllerFlagForExistingDealerClaimVinMapping() {
        DealerClaimVinMapping dealerClaimVinMapping = getDealerClaimVinMapping();
        dealerClaimVinMapping.setApprovalRejectFlag("R");
       dealerClaimVinMapping.setCtlFlag("R");
        optionPriceClaimService.updateControllerFlagForExistingDealerClaimVinMapping(dealerClaimVinMapping);
        assertEquals("P", dealerClaimVinMapping.getApprovalRejectFlag());
    }

    @Test
    void shouldUpdateApproveRejectFlagForExistingDealerClaimVinMapping() {
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCodeIn(any())).thenReturn(new ArrayList<>());
        List<FinParentChildMap> finParentChildMapList = new ArrayList<>();
        finParentChildMapList.add(getFinParentChildMapping2());
        when(finParentChildMapRepository.findByParentFinFinCodeAndMappingStatusFlag(anyString(), anyString())).thenReturn(finParentChildMapList);
        when(stateRepository.findByStateCodeNot(anyString())).thenReturn(getStates());
        DealerClaimVinMapping dealerClaimVinMapping = getDealerClaimVinMapping();
        dealerClaimVinMapping.setApprovalRejectFlag("R");
        dealerClaimVinMapping.setCtlFlag("R");
        dealerClaimVinMapping.setInvoiceInfo(InvoiceInfo.builder().vinCode("1FTVW1EL3PWG32402").build());
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCode(anyString())).thenReturn(dealerClaimVinMapping);
        when(invoiceInfoRepository.findByVinCode(anyString())).thenReturn(InvoiceInfo.builder().vinCode("1FTVW1EL3PWG32401").flatPurchaseOrder("4").build());
        when(referenceNumberRepository.findByReferenceNumberCodeAndBodyBodyKey(anyString(), any())).thenReturn(getReferenceNumber());
        when(finRepository.findByFinCode(anyString())).thenReturn(Fin.builder().build());
        when(claimExceptionRepository.findById(anyInt())).thenReturn(Optional.of(ClaimIssue.builder().exceptionKey(1).build()));
        when(dealerClaimRepository.save(any(DealerClaim.class))).thenReturn(getDealerClaim());
        when(dealerClaimVinMappingRepository.saveAll(any())).thenReturn(new ArrayList<>());
        when(claimVinExceptionRepository.saveAll(any())).thenReturn(new ArrayList<>());
        doNothing().when(priceProtectionUtilService).deletePriceInfoPriceOptionPriceConstantClaimSnapshot(any());

        OptionClaimRequest optionClaimRequest = getOptionClaimRequest();
        optionClaimRequest.setPriceProtectionClaimKey(null);
        List<OptionVinInfo> optionVinInfoList = new ArrayList<>();
        optionVinInfoList.add(getOptionVinInfo2());
        optionClaimRequest.setOptionVinInfoList(optionVinInfoList);
        List<OptionVinInfo> deleteOptionVinInfoList = new ArrayList<>();
        OptionVinInfo optionVinInfo = getOptionVinInfo1();
        optionVinInfo.setPrimaryVinFlag("N");
        deleteOptionVinInfoList.add(optionVinInfo);
        optionClaimRequest.setDeleteOptionVinInfoList(deleteOptionVinInfoList);
        PriceProtectionClaimResponse PriceProtectionClaimResponse = optionPriceClaimService.processOptionPriceProtectionClaimRequest(optionClaimRequest);
        verify(dealerClaimVinMappingRepository, times(1)).saveAll(any());
        verify(claimVinExceptionRepository, times(1)).saveAll(any());
        assertEquals(44, PriceProtectionClaimResponse.getClaimId());
        assertEquals(Constants.PP_CLAIM_SAVE_MSG, PriceProtectionClaimResponse.getResponseMessage());
    }

    @Test
    void shouldUpdateApproveRejectFlagForExistingDealerClaimVinMappingWithPrimaryVinFlagNull() {
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCodeIn(any())).thenReturn(new ArrayList<>());
        List<FinParentChildMap> finParentChildMapList = new ArrayList<>();
        finParentChildMapList.add(getFinParentChildMapping2());
        when(finParentChildMapRepository.findByParentFinFinCodeAndMappingStatusFlag(anyString(), anyString())).thenReturn(finParentChildMapList);
        when(stateRepository.findByStateCodeNot(anyString())).thenReturn(getStates());
        DealerClaimVinMapping dealerClaimVinMapping = getDealerClaimVinMapping();
        dealerClaimVinMapping.setApprovalRejectFlag("R");
        dealerClaimVinMapping.setCtlFlag("R");
        dealerClaimVinMapping.setInvoiceInfo(InvoiceInfo.builder().vinCode("1FTVW1EL3PWG32402").build());
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCode(anyString())).thenReturn(dealerClaimVinMapping);
        when(invoiceInfoRepository.findByVinCode(anyString())).thenReturn(InvoiceInfo.builder().vinCode("1FTVW1EL3PWG32401").flatPurchaseOrder("4").build());
        when(referenceNumberRepository.findByReferenceNumberCodeAndBodyBodyKey(anyString(), any())).thenReturn(getReferenceNumber());
        when(finRepository.findByFinCode(anyString())).thenReturn(Fin.builder().build());
        when(dealerClaimRepository.save(any(DealerClaim.class))).thenReturn(getDealerClaim());
        when(dealerClaimVinMappingRepository.saveAll(any())).thenReturn(new ArrayList<>());
       doNothing().when(priceProtectionUtilService).deletePriceInfoPriceOptionPriceConstantClaimSnapshot(any());

        OptionClaimRequest optionClaimRequest = getOptionClaimRequest();
        optionClaimRequest.setPriceProtectionClaimKey(null);
        List<OptionVinInfo> optionVinInfoList = new ArrayList<>();
        OptionVinInfo optionVinInfo = getOptionVinInfo2();
        optionVinInfo.setExceptionCodes(new ArrayList<>());
        optionVinInfoList.add(optionVinInfo);
        optionClaimRequest.setOptionVinInfoList(optionVinInfoList);
        PriceProtectionClaimResponse PriceProtectionClaimResponse = optionPriceClaimService.processOptionPriceProtectionClaimRequest(optionClaimRequest);
        verify(dealerClaimVinMappingRepository, times(1)).saveAll(any());
        assertEquals(44, PriceProtectionClaimResponse.getClaimId());
        assertEquals(Constants.PP_CLAIM_SAVE_MSG, PriceProtectionClaimResponse.getResponseMessage());
    }

    @Test
    void shouldUpdateApproveRejectFlagForExistingDealerClaimVinMappingForMatchingVinCodes() {
        DealerClaimVinMapping dealerClaimVinMapping = getDealerClaimVinMapping();
        dealerClaimVinMapping.setApprovalRejectFlag("R");
        dealerClaimVinMapping.setInvoiceInfo(InvoiceInfo.builder().vinCode("1FTVW1EL3PWG32401").build());
        dealerClaimVinMapping.setPrimaryVinFlag("Y");
        optionPriceClaimService.getDeletedPrimaryVinInfoList().add(getOptionVinInfo1());
        optionPriceClaimService.updateApproveRejectFlagForExistingDealerClaimVinMapping(dealerClaimVinMapping);
        assertNull(dealerClaimVinMapping.getPrimaryVinFlag());
    }

    @Test
    void shouldThrowExceptionIfFinIsNull() {
        when(finRepository.findByFinCode(anyString())).thenReturn(null);
        try {
            optionPriceClaimService.assignFinToDealerClaimVinMapping(getOptionVinInfo1(), getDealerClaimVinMapping());
        } catch (PriceProtectionException exception) {
            assertEquals("Invalid End user fin", exception.getMessage());
        }
    }

    @Test
    void shouldProcessDeletedClaimVinMapping() {
        when(claimVinExceptionRepository.findByDealerClaimDealerClaimKeyAndDealerClaimVinMappingDealerClaimVinKey(anyInt(), any())).thenReturn(new ArrayList<>());
        optionPriceClaimService.processDeletedClaimVinMapping(getOptionClaimRequest(), getOptionVinInfo1(), new ArrayList<>(), new ArrayList<>());
        assertEquals(1, optionPriceClaimService.getDeletedPrimaryVinInfoList().size());
    }

    @Test
    void shouldUpdateClaimVinExceptionListFromExceptionCodesWithNonPendingApprovalRejectFlag() {
        DealerClaimVinMapping dealerClaimVinMapping = getDealerClaimVinMapping();
        dealerClaimVinMapping.setApprovalRejectFlag("A");
        List<ClaimVinException> claimVinExceptionList = new ArrayList<>();
        optionPriceClaimService.updateClaimVinExceptionListFromExceptionCodes(getOptionVinInfo1(), getDealerClaim(), dealerClaimVinMapping, claimVinExceptionList);
        assertEquals(0, claimVinExceptionList.size());
    }

    @Test
    void shouldBuildClaimVinException() {
        List<ClaimVinException> claimVinExceptionList = new ArrayList<>();
        ClaimVinException claimVinException = ClaimVinException.builder().claimIssue(ClaimIssue.builder().exceptionKey(1).build()).build();
        claimVinExceptionList.add(claimVinException);
        when(claimVinExceptionRepository.findByDealerClaimDealerClaimKeyAndDealerClaimVinMappingDealerClaimVinKey(anyInt(), any())).thenReturn(claimVinExceptionList);
        when(claimExceptionRepository.findById(anyInt())).thenReturn(Optional.of(ClaimIssue.builder().exceptionKey(1).build()));
        List<ClaimVinException> claimVinExceptions = optionPriceClaimService.buildClaimVinException(getOptionVinInfo1(), getDealerClaim(), getDealerClaimVinMapping());
        assertEquals(3, claimVinExceptions.size());
        assertEquals(Constants.LOGIN_USER, claimVinExceptions.get(2).getCreateUser());
        assertNotNull(claimVinExceptions.get(2).getCreateTime());
        assertEquals(Constants.LOGIN_USER, claimVinExceptions.get(2).getUpdateUser());
        assertNotNull(claimVinExceptions.get(2).getUpdateTime());
        assertNotNull(claimVinExceptions.get(2).getDealerClaim());
        assertNotNull(claimVinExceptions.get(2).getDealerClaimVinMapping());
    }

    @Test
    void shouldValidateClaimVinException() {
        when(claimVinExceptionRepository.findByDealerClaimDealerClaimKeyAndDealerClaimVinMappingDealerClaimVinKey(anyInt(), any())).thenReturn(new ArrayList<>());
        List<ClaimVinException> claimVinExceptionList = new ArrayList<>();
        ClaimVinException claimVinException = ClaimVinException.builder().claimIssue(ClaimIssue.builder().exceptionKey(1).build()).build();
        claimVinExceptionList.add(claimVinException);
        boolean isExceptionAvailable = optionPriceClaimService.validateClaimVinException("1", getDealerClaim(), getDealerClaimVinMapping(), claimVinExceptionList);
        assertTrue(isExceptionAvailable);
    }

    @Test
    void shouldValidateClaimVinExceptionWhenForNewClaim() {
        List<ClaimVinException> claimVinExceptionList = new ArrayList<>();
        ClaimVinException claimVinException = ClaimVinException.builder().claimIssue(ClaimIssue.builder().exceptionKey(1).build()).build();
        claimVinExceptionList.add(claimVinException);
        DealerClaimVinMapping dealerClaimVinMapping = getDealerClaimVinMapping();
        dealerClaimVinMapping.setDealerClaimVinKey(null);
        boolean isExceptionAvailable = optionPriceClaimService.validateClaimVinException("1", getDealerClaim(), dealerClaimVinMapping, claimVinExceptionList);
        assertFalse(isExceptionAvailable);
    }

    @Test
    void shouldBuildDealerClaim() {
        when(stateRepository.findByStateCodeNot(anyString())).thenReturn(getStates());
        when(dealerClaimRepository.findByDealerClaimKey(anyInt())).thenReturn(getDealerClaim());

        DealerClaim dealerClaim = optionPriceClaimService.buildDealerClaim(getOptionClaimRequest());
        assertEquals(Constants.LOGIN_USER, dealerClaim.getUpdateUser());
        assertNotNull(dealerClaim.getUpdateTime());
        assertEquals(Constants.DEALER_CODE, dealerClaim.getDealerCode());
        assertEquals("NY", dealerClaim.getState().getStateCode());
        assertEquals(UUID.fromString("b1757035-8a54-4fee-a2a3-01f016909a76"), dealerClaim.getFin().getFinKey());
        assertEquals(UUID.fromString("046f5b93-a9a0-4538-b0ec-fcd8cc456d2c"), dealerClaim.getVehicleLineModelYear().getVehicleLineKey());
        assertNull(dealerClaim.getPriorModelYear());
        assertEquals(173, dealerClaim.getGpcRequest().getGpcRequestKey());
        assertEquals(Constants.PP_TYPE_CODE_O, dealerClaim.getClaimType().getPpTypeCode());
        assertEquals(Constants.NO, dealerClaim.getSubmittedFlag());
        assertNull(dealerClaim.getSubmittedDate());
        assertEquals("option claim", dealerClaim.getComment());
    }

    @Test
    void shouldReturnNullForState() {
        List<State> states = getStates();
        states.get(0).setStateCode("FL");
        when(stateRepository.findByStateCodeNot(anyString())).thenReturn(states);
        State state = optionPriceClaimService.getState(getOptionClaimRequest());
        assertNull(state);
    }

    @Test
    void shouldReturnGpcRequestNumberWhenOriginalGpcRequestNumberIsNotNull() {
        OptionClaimRequest optionClaimRequest = getOptionClaimRequest();
       optionClaimRequest.setSelectedGpcRequestNumber("121-test");
        GpcRequest gpcRequest = optionPriceClaimService.getGpcRequest(optionClaimRequest);
        assertEquals(121, gpcRequest.getGpcRequestKey());
    }

    @Test
    void shouldReturnSubmittedDate() {
        Date date = optionPriceClaimService.getSubmittedDate("SUBMIT");
        assertNotNull(date);
    }

    @Test
    void shouldBuildEmptyDealerClaimVinMappingList() {
        List<ClaimVinException> claimVinExceptionList = new ArrayList<>();
        OptionClaimRequest optionClaimRequest = getOptionClaimRequest();
        List<OptionVinInfo> optionVinInfoList = new ArrayList<>();
        OptionVinInfo optionVinInfo = getOptionVinInfo1();
        optionVinInfo.setVinCode(null);
        optionVinInfoList.add(optionVinInfo);
        optionClaimRequest.setOptionVinInfoList(optionVinInfoList);
        List<DealerClaimVinMapping> dealerClaimVinMappings = optionPriceClaimService.buildDealerClaimVinMappingList(optionClaimRequest, getDealerClaim(), claimVinExceptionList);
        assertEquals(0, dealerClaimVinMappings.size());
    }

    @Test
    void shouldBuildDealerClaimVinMapping() {
        DealerClaimVinMapping dealerClaimVinMapping = getDealerClaimVinMapping();
        dealerClaimVinMapping.setApprovalRejectFlag("R");
        dealerClaimVinMapping.setCtlFlag("R");
        dealerClaimVinMapping.setPriorOrderNumberCode("111");
        dealerClaimVinMapping.setPriorBody(BodyCode.builder().build());
        dealerClaimVinMapping.setInvoiceInfo(InvoiceInfo.builder().vinCode("1FTVW1EL3PWG32401").build());
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCode(anyString())).thenReturn(dealerClaimVinMapping);
        when(invoiceInfoRepository.findByVinCode(anyString())).thenReturn(InvoiceInfo.builder().flatPurchaseOrder("4").build());
        when(referenceNumberRepository.findByReferenceNumberCodeAndBodyBodyKey(anyString(), any())).thenReturn(getReferenceNumber());
        when(finRepository.findByFinCode(anyString())).thenReturn(Fin.builder().build());

        OptionVinInfo optionVinInfo = getOptionVinInfo1();
        DealerClaimVinMapping dealerClaimVinMappingNew = optionPriceClaimService.buildDealerClaimVinMapping(optionVinInfo, getDealerClaim());
        assertEquals(2022, dealerClaimVinMappingNew.getDealerClaim().getPriorModelYear());
        assertEquals(Constants.LOGIN_USER, dealerClaimVinMappingNew.getUpdateUser());
        assertNotNull(dealerClaimVinMappingNew.getUpdateTime());
        assertEquals("4", dealerClaimVinMappingNew.getOrderNumCode());
        assertEquals(UUID.fromString("4ffe3476-6b9a-415e-8c0a-2988a8583c60"), dealerClaimVinMappingNew.getCurrentBody().getBodyKey());
        assertEquals("80892C", dealerClaimVinMappingNew.getCurrentRefNumber().getReferenceNumberCode());
        assertNull(dealerClaimVinMappingNew.getPriorOrderNumberCode());
        assertNull(dealerClaimVinMappingNew.getPriorBody());
        assertEquals(UUID.fromString("7c51fab9-0acc-49ec-b85e-03ead8341c4c"), dealerClaimVinMappingNew.getPriorRefNumber().getReferenceNumberKey());
        assertEquals("P", dealerClaimVinMappingNew.getApprovalRejectFlag());
        assertEquals("P", dealerClaimVinMappingNew.getCtlFlag());
        assertNotNull(dealerClaimVinMappingNew.getEndUserFin());
    }

    @Test
    void shouldReturnReferenceNumber() {
        OptionVinInfo optionVinInfo = getOptionVinInfo1();
        optionVinInfo.setNewReferenceNumber(null);
        ReferenceNumber referenceNumber = optionPriceClaimService.getReferenceNumberForDealerClaimVinMapping(optionVinInfo);
        assertNull(referenceNumber);
    }

    @Test
    void shouldReturnPriorReferenceNumber() {
        OptionVinInfo optionVinInfo = getOptionVinInfo1();
        optionVinInfo.setPriorReferenceNumberKey(null);
        ReferenceNumber priorReferenceNumber = optionPriceClaimService.getPriorReferenceNumberForDealerClaimVinMapping(optionVinInfo);
        assertNull(priorReferenceNumber);
    }

    @Test
    void shouldUpdateClaimStatusForSave() {
        DealerClaim dealerClaim = getDealerClaim();
        OptionClaimRequest optionClaimRequest = getOptionClaimRequest();
        optionPriceClaimService.claimStatusUpdate(optionClaimRequest, dealerClaim);
        assertEquals(DealerUserStatus.SAVED.getStatusGroupKey(), dealerClaim.getDealerStatus().getStatusGroupMappingKey());
        assertEquals(InternalUserStatus.SAVED.getStatusGroupKey(), dealerClaim.getInternalStatus().getStatusGroupMappingKey());
    }

    @Test
    void shouldUpdateDeletedDealerClaimVinMappingListAndPrimaryVinInfoList() {
        List<DealerClaimVinMapping> dealerClaimVinMappingList = new ArrayList<>();
        DealerClaimVinMapping dealerClaimVinMapping = getDealerClaimVinMapping();
        dealerClaimVinMapping.setInvoiceInfo(InvoiceInfo.builder().vinCode("1FTVW1EL3PWG32401").build());
        dealerClaimVinMappingList.add(dealerClaimVinMapping);
        List<DealerClaimVinMapping> deletedDealerClaimVinMappingList = getDealerClaimVinMappingList();
        optionPriceClaimService.getDeletedPrimaryVinInfoList().add(getOptionVinInfo1());
        optionPriceClaimService.updateDeletedDealerClaimVinMappingListAndPrimaryVinInfoList(deletedDealerClaimVinMappingList, dealerClaimVinMappingList);
        assertEquals(0, deletedDealerClaimVinMappingList.size());
        assertEquals(0, optionPriceClaimService.getDeletedPrimaryVinInfoList().size());
        assertEquals("Y", dealerClaimVinMappingList.get(0).getPrimaryVinFlag());
    }

    @Test
    void shouldUpdateDeletedDealerClaimVinMappingListAndPrimaryVinInfoListWithoutDuplicateVinCodes() {
        List<DealerClaimVinMapping> dealerClaimVinMappingList = new ArrayList<>();
        DealerClaimVinMapping dealerClaimVinMapping = getDealerClaimVinMapping();
        dealerClaimVinMapping.setInvoiceInfo(InvoiceInfo.builder().vinCode("1FTVW1EL3PWG32401").build());
        dealerClaimVinMapping.setDealerClaimVinKey(UUID.fromString("71516fe0-e704-4104-86f6-4fa7131348b3"));
        dealerClaimVinMappingList.add(dealerClaimVinMapping);
        List<DealerClaimVinMapping> deletedDealerClaimVinMappingList = getDealerClaimVinMappingList();
        OptionVinInfo optionVinInfo = getOptionVinInfo1();
        optionVinInfo.setVinCode("1FTVW1EL3PWG32402");
        optionPriceClaimService.getDeletedPrimaryVinInfoList().add(optionVinInfo);
        optionPriceClaimService.updateDeletedDealerClaimVinMappingListAndPrimaryVinInfoList(deletedDealerClaimVinMappingList, dealerClaimVinMappingList);
        assertEquals(1, deletedDealerClaimVinMappingList.size());
        assertEquals(1, optionPriceClaimService.getDeletedPrimaryVinInfoList().size());
    }

    @Test
    void shouldSaveOptionPriceProtectionClaim() {
        when(dealerClaimRepository.save(any(DealerClaim.class))).thenReturn(getDealerClaim());
        DealerClaim dealerClaim = getDealerClaim();
        List<DealerClaimVinMapping> deleteDealerClaimVinMappingList = new ArrayList<>();
        List<DealerClaimVinMapping> dealerClaimVinMappingList = new ArrayList<>();
        List<ClaimVinException> claimVinExceptionList = new ArrayList<>();
        Integer claimId = optionPriceClaimService.saveOptionPriceProtectionClaim(dealerClaim, deleteDealerClaimVinMappingList, dealerClaimVinMappingList, claimVinExceptionList);
        verify(dealerClaimRepository, times(1)).save(any(DealerClaim.class));
        assertEquals(44, claimId);
    }

    @Test
    void shouldGetDeletedPrimaryVinKeyList() {
        List<OptionVinInfo> deletedPrimayVinInfoList = new ArrayList<>();
        deletedPrimayVinInfoList.add(getOptionVinInfo1());
        List<UUID> vinKeysList = optionPriceClaimService.getDeletedPrimaryVinKeysList(deletedPrimayVinInfoList);
        assertFalse(vinKeysList.isEmpty());
    }

    @Test
    void shouldSubmitOptionPriceClaimForExistingClaim() {
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCodeIn(any())).thenReturn(new ArrayList<>());
        List<FinParentChildMap> finParentChildMapList = new ArrayList<>();
        finParentChildMapList.add(getFinParentChildMapping2());
        when(finParentChildMapRepository.findByParentFinFinCodeAndMappingStatusFlag(anyString(), anyString())).thenReturn(finParentChildMapList);
        when(dealerClaimRepository.findByDealerClaimKey(anyInt())).thenReturn(getDealerClaim());
        when(stateRepository.findByStateCodeNot(anyString())).thenReturn(getStates());
        when(claimExceptionRepository.findById(anyInt())).thenReturn(Optional.of(ClaimIssue.builder().exceptionKey(1).build()));
        when(claimVinExceptionRepository.findByDealerClaimDealerClaimKeyAndDealerClaimVinMappingDealerClaimVinKey(anyInt(), any())).thenReturn(new ArrayList<>());
        DealerClaimVinMapping dealerClaimVinMapping = getDealerClaimVinMapping();
        dealerClaimVinMapping.setApprovalRejectFlag("R");
        dealerClaimVinMapping.setCtlFlag("R");
        dealerClaimVinMapping.setInvoiceInfo(InvoiceInfo.builder().vinCode("1FTVW1EL3PWG32401").build());
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCode(anyString())).thenReturn(dealerClaimVinMapping);
        when(invoiceInfoRepository.findByVinCode(anyString())).thenReturn(InvoiceInfo.builder().flatPurchaseOrder("4").build());
        when(referenceNumberRepository.findByReferenceNumberCodeAndBodyBodyKey(anyString(), any())).thenReturn(getReferenceNumber());
        when(finRepository.findByFinCode(anyString())).thenReturn(Fin.builder().build());
        when(dealerClaimRepository.save(any(DealerClaim.class))).thenReturn(getDealerClaim());
        when(dealerClaimVinMappingRepository.saveAll(any())).thenReturn(new ArrayList<>());
        when(claimVinExceptionRepository.saveAll(any())).thenReturn(new ArrayList<>());
        doNothing().when(priceProtectionUtilService).deletePriceInfoPriceOptionPriceConstantClaimSnapshot(any());

        OptionClaimRequest optionClaimRequest = getOptionClaimRequest();
        optionClaimRequest.setOperation("SUBMIT");
        optionClaimRequest.setDeleteOptionVinInfoList(new ArrayList<>());
        PriceProtectionClaimResponse PriceProtectionClaimResponse = optionPriceClaimService.processOptionPriceProtectionClaimRequest(optionClaimRequest);
        verify(dealerClaimVinMappingRepository, times(1)).saveAll(any());
        verify(claimVinExceptionRepository, times(1)).saveAll(any());
        assertEquals(44, PriceProtectionClaimResponse.getClaimId());
        assertEquals(Constants.PP_CLAIM_SUBMIT_MSG, PriceProtectionClaimResponse.getResponseMessage());
    }

    @Test
    void shouldSubmitOptionPriceClaimForExistingClaimForBackGroundClaimProcess() {
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCodeIn(any())).thenReturn(new ArrayList<>());
        List<FinParentChildMap> finParentChildMapList = new ArrayList<>();
        finParentChildMapList.add(getFinParentChildMapping2());
        when(finParentChildMapRepository.findByParentFinFinCodeAndMappingStatusFlag(anyString(), anyString())).thenReturn(finParentChildMapList);
        DealerClaim dealerClaim = getDealerClaim();
        when(dealerClaimRepository.findByDealerClaimKey(anyInt())).thenReturn(dealerClaim);
        when(stateRepository.findByStateCodeNot(anyString())).thenReturn(getStates());
        when(claimExceptionRepository.findById(anyInt())).thenReturn(Optional.of(ClaimIssue.builder().exceptionKey(1).build()));
        when(claimVinExceptionRepository.findByDealerClaimDealerClaimKeyAndDealerClaimVinMappingDealerClaimVinKey(anyInt(), any())).thenReturn(new ArrayList<>());
        DealerClaimVinMapping dealerClaimVinMapping = getDealerClaimVinMapping();
        dealerClaimVinMapping.setApprovalRejectFlag("R");
        dealerClaimVinMapping.setCtlFlag("R");
        dealerClaimVinMapping.setInvoiceInfo(InvoiceInfo.builder().vinCode("1FTVW1EL3PWG32401").build());
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCode(anyString())).thenReturn(dealerClaimVinMapping);
        when(invoiceInfoRepository.findByVinCode(anyString())).thenReturn(InvoiceInfo.builder().flatPurchaseOrder("4").build());
        when(referenceNumberRepository.findByReferenceNumberCodeAndBodyBodyKey(anyString(), any())).thenReturn(getReferenceNumber());
        when(finRepository.findByFinCode(anyString())).thenReturn(Fin.builder().build());
        when(dealerClaimRepository.save(any(DealerClaim.class))).thenReturn(getDealerClaim());
        doNothing().when(dealerClaimVinMappingRepository).deleteAll(any());
        when(dealerClaimVinMappingRepository.saveAll(any())).thenReturn(new ArrayList<>());
        when(claimVinExceptionRepository.saveAll(any())).thenReturn(new ArrayList<>());
        doNothing().when(priceProtectionUtilService).deletePriceInfoPriceOptionPriceConstantClaimSnapshot(any());
        when(priceProtectionVinService.priceProtectionVinCall(any())).thenReturn("success");

        OptionClaimRequest optionClaimRequest = getOptionClaimRequest();
        optionClaimRequest.setOperation("SUBMIT");
        PriceProtectionClaimResponse PriceProtectionClaimResponse = optionPriceClaimService.processOptionPriceProtectionClaimRequest(optionClaimRequest);
        verify(dealerClaimVinMappingRepository, times(1)).deleteAll(any());
        verify(dealerClaimVinMappingRepository, times(1)).saveAll(any());
        verify(claimVinExceptionRepository, times(1)).saveAll(any());
        assertEquals(44, PriceProtectionClaimResponse.getClaimId());
        assertEquals(Constants.PP_CLAIM_SUBMIT_MSG, PriceProtectionClaimResponse.getResponseMessage());
        assertEquals(InternalUserStatus.IN_PROGRESS.getStatusGroupKey(), dealerClaim.getInternalStatus().getStatusGroupMappingKey());
    }

    @Test
    void shouldSubmitOptionPriceClaimForExistingClaimWithNewVinAddedFlag() {
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCodeIn(any())).thenReturn(new ArrayList<>());
        List<FinParentChildMap> finParentChildMapList = new ArrayList<>();
        finParentChildMapList.add(getFinParentChildMapping2());
        when(finParentChildMapRepository.findByParentFinFinCodeAndMappingStatusFlag(anyString(), anyString())).thenReturn(finParentChildMapList);
        when(dealerClaimRepository.findByDealerClaimKey(anyInt())).thenReturn(getDealerClaim());
        when(stateRepository.findByStateCodeNot(anyString())).thenReturn(getStates());
        when(claimExceptionRepository.findById(anyInt())).thenReturn(Optional.of(ClaimIssue.builder().exceptionKey(1).build()));
        when(claimVinExceptionRepository.findByDealerClaimDealerClaimKeyAndDealerClaimVinMappingDealerClaimVinKey(anyInt(), any())).thenReturn(new ArrayList<>());
        when(dealerClaimVinMappingRepository.findByInvoiceInfoVinCode(anyString())).thenReturn(null);
        when(dealerClaimVinMappingRepository.findByDealerClaimVinKey(any())).thenReturn(null);
        when(invoiceInfoRepository.findByVinCode(anyString())).thenReturn(InvoiceInfo.builder().flatPurchaseOrder("4").build());
        when(referenceNumberRepository.findByReferenceNumberCodeAndBodyBodyKey(anyString(), any())).thenReturn(getReferenceNumber());
        when(finRepository.findByFinCode(anyString())).thenReturn(Fin.builder().build());
        when(dealerClaimRepository.save(any(DealerClaim.class))).thenReturn(getDealerClaim());
        doNothing().when(dealerClaimVinMappingRepository).deleteAll(any());
        when(dealerClaimVinMappingRepository.saveAll(any())).thenReturn(new ArrayList<>());
        when(claimVinExceptionRepository.saveAll(any())).thenReturn(new ArrayList<>());
        doNothing().when(priceProtectionUtilService).deletePriceInfoPriceOptionPriceConstantClaimSnapshot(any());
        when(priceProtectionVinService.priceProtectionVinCall(any())).thenReturn("success");

        OptionClaimRequest optionClaimRequest = getOptionClaimRequest();
        optionClaimRequest.setOperation("SUBMIT");
        PriceProtectionClaimResponse PriceProtectionClaimResponse = optionPriceClaimService.processOptionPriceProtectionClaimRequest(optionClaimRequest);
        verify(dealerClaimVinMappingRepository, times(1)).deleteAll(any());
        verify(dealerClaimVinMappingRepository, times(1)).saveAll(any());
        verify(claimVinExceptionRepository, times(1)).saveAll(any());
        assertEquals(44, PriceProtectionClaimResponse.getClaimId());
        assertEquals(Constants.PP_CLAIM_SUBMIT_MSG, PriceProtectionClaimResponse.getResponseMessage());
    }

    @Test
    void shouldUpdateClaimStatusForSubmitRejected() {
        DealerClaim dealerClaim = getDealerClaim();
        OptionClaimRequest optionClaimRequest = getOptionClaimRequest();
        optionClaimRequest.setDealerClaimStatus("R");
        optionClaimRequest.setOperation("SUBMIT");
        optionPriceClaimService.claimStatusUpdateForSubmit(optionClaimRequest, dealerClaim);
        assertEquals(DealerUserStatus.PENDING_APPROVAL.getStatusGroupKey(), dealerClaim.getDealerStatus().getStatusGroupMappingKey());
        assertEquals(InternalUserStatus.PENDING_GSL.getStatusGroupKey(), dealerClaim.getInternalStatus().getStatusGroupMappingKey());
    }

    @Test
    void shouldUpdateClaimStatusForSubmitPending() {
        DealerClaim dealerClaim = getDealerClaim();
        OptionClaimRequest optionClaimRequest = getOptionClaimRequest();
       optionClaimRequest.setDealerClaimStatus("P");
        optionClaimRequest.setOperation("SUBMIT");
        optionPriceClaimService.claimStatusUpdateForSubmit(optionClaimRequest, dealerClaim);
        assertEquals(DealerUserStatus.SUBMITTED.getStatusGroupKey(), dealerClaim.getDealerStatus().getStatusGroupMappingKey());
        assertEquals(InternalUserStatus.IN_PROGRESS.getStatusGroupKey(), dealerClaim.getInternalStatus().getStatusGroupMappingKey());
    }

    private ReferenceNumber getReferenceNumber() {
        return ReferenceNumber.builder()
                .referenceNumberCode("80892C")
                .gpcRequest(GpcRequest.builder().gpcRequestKey(121).fin(Fin.builder().finCode("QS037").build()).build())
                .fin(Fin.builder().finCode("QS066").build())
                .body(BodyCode.builder()
                        .bodyKey(UUID.fromString("b959933f-7b2f-46d1-af8b-7f166f4a7a37"))
                        .bodyReferenceCode("W1E")
                        .vehicleLineModelYear(VehicleLineModelYear.builder().modelYearCode(2024).build())
                        .build())
                .priceLevel("300")
                .referenceNumberType(ReferenceNumberType.builder().referenceNumberTypeName("PRIMARY").build())
                .statusCode("A")
                .dealerCode("11000")
                .build();
    }

    public List<State> getStates() {
        List<State> states = new ArrayList<>();
        states.add(State.builder().stateCode("NY").build());
        states.add(State.builder().stateCode("AL").build());
        states.add(State.builder().stateCode("AK").build());
        return states;
    }

    public DealerClaim getDealerClaim() {
        return DealerClaim.builder()
                .dealerClaimKey(44)
                .priorModelYear(2022)
                .submittedDate(new Date())
                .build();
    }

    public FinParentChildMap getFinParentChildMapping() {
        return FinParentChildMap.builder().childFin(Fin.builder().finCode("QA273").build()).build();
    }

    public FinParentChildMap getFinParentChildMapping2() {
        return FinParentChildMap.builder().childFin(Fin.builder().finCode("KA001").build()).build();
    }

    public List<DealerClaimVinMapping> getDealerClaimVinMappingList() {
        List<DealerClaimVinMapping> dealerClaimVinMappingList = new ArrayList<>();
        dealerClaimVinMappingList.add(getDealerClaimVinMapping());
        return dealerClaimVinMappingList;
    }

    public DealerClaimVinMapping getDealerClaimVinMapping() {
        return DealerClaimVinMapping.builder()
                .dealerClaimVinKey(UUID.fromString("71516fe0-e704-4104-86f6-4fa7131348a3"))
               .dealerClaim(DealerClaim.builder().dealerClaimKey(44).build())
                .approvalRejectFlag("P")
                .ctlFlag("P")
                .build();
    }

    public OptionClaimRequest getOptionClaimRequest() {
        List<OptionVinInfo> optionVinInfoList = new ArrayList<>();
        optionVinInfoList.add(getOptionVinInfo1());
        List<OptionVinInfo> deletedOptionVinInfoList = new ArrayList<>();
        OptionVinInfo deleteOptionVinInfo = getOptionVinInfo1();
        deleteOptionVinInfo.setDealerClaimVinMappingKey(UUID.fromString("71516fe0-e704-4104-86f6-4fa7131348b3"));
        deletedOptionVinInfoList.add(deleteOptionVinInfo);
        List<String> fileIds = List.of("1", "2", "3");
        return OptionClaimRequest.builder()
                .selectedStateCode("NY")
                .selectedFinKey(UUID.fromString("b1757035-8a54-4fee-a2a3-01f016909a76"))
                .selectedFinCode("KA001")
                .selectedVehicleLineKey(UUID.fromString("046f5b93-a9a0-4538-b0ec-fcd8cc456d2c"))
                .selectedGpcRequestNumber("173_R1-A R C OF  MADISON COUNTY")
                .operation("SAVE")
                .comment("option claim")
                .optionVinInfoList(optionVinInfoList)
                .deleteOptionVinInfoList(deletedOptionVinInfoList)
                .priceProtectionClaimKey(44)
                .dealerClaimStatus("R")
                .fileIdList(fileIds)
                .build();
    }

    public OptionVinInfo getOptionVinInfo1() {
        return OptionVinInfo.builder()
                .vinCode("1FTVW1EL3PWG32401")
                .newReferenceNumber("80892C")
                .priorReferenceNumberKey(UUID.fromString("7c51fab9-0acc-49ec-b85e-03ead8341c4c"))
                .endUserFin("KA001")
                .dealerClaimVinMappingKey(UUID.fromString("71516fe0-e704-4104-86f6-4fa7131348a3"))
                .primaryVinFlag("Y")
                .vinGroupId(1)
                .bodyKey(UUID.fromString("4ffe3476-6b9a-415e-8c0a-2988a8583c60"))
                .exceptionCodes(Arrays.asList("1", "2"))
                .build();
    }

    public OptionVinInfo getOptionVinInfo2() {
        return OptionVinInfo.builder()
                .vinCode("1FTVW1EL3PWG32401")
                .newReferenceNumber("80892C")
                .priorReferenceNumberKey(UUID.fromString("7c51fab9-0acc-49ec-b85e-03ead8341c4c"))
                .endUserFin("KA001")
                .primaryVinFlag("Y")
                .vinGroupId(1)
                .bodyKey(UUID.fromString("4ffe3476-6b9a-415e-8c0a-2988a8583c60"))
                .exceptionCodes(Arrays.asList("1", "2"))
                .build();
    }
}

 