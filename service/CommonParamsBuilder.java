package ma.sg.its.octroicreditapi.service.impl.opc;

import lombok.extern.slf4j.Slf4j;
import ma.sg.its.octroicredit.common.dto.AttachmentDto;
import ma.sg.its.octroicredit.common.dto.IndividualCustomerDTO;
import ma.sg.its.octroicredit.common.util.DateUtils;
import ma.sg.its.octroicredit.common.util.LocationUtils;
import ma.sg.its.octroicredit.common.util.MoneyToWords;
import ma.sg.its.octroicredit.common.util.StringUtils;
import ma.sg.its.octroicreditapi.dto.DossierDataDto;
import ma.sg.its.octroicreditapi.dto.LoanDetailValidationResult;
import ma.sg.its.octroicreditapi.tools.ObjectUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
public class CommonParamsBuilder {

    /**
     * Build common parameters shared across all templates
     * (dates, customer info, loan terms, formatting)
     */
    public Map<String, Object> buildCommonParams(
            DossierDataDto dossier,
            LoanDetailValidationResult loanDetail) {

        Map<String, Object> params = new HashMap<>();
        IndividualCustomerDTO customerData = dossier.getCustomerData();

        // === GENERAL ===
        params.put("signedAt", DateUtils.convertDateToString(LocalDateTime.now(), "dd/MM/yyyy"));
        params.put("signedIn", LocationUtils.getCityFromTimezone());

        LocalDateTime decisionDate = getLatestNotificationDate(dossier);
        if (decisionDate != null) {
            params.put("decisionDate", DateUtils.convertDateToString(decisionDate, "dd/MM/yyyy"));
        }

        // === CUSTOMER INFO (safe extraction) ===
        params.put("customerFullName", constructCustomerFullName(dossier));

        if (customerData != null && customerData.getBalanceActivity() != null) {
            params.put("customerRib", customerData.getBalanceActivity().getAccountNumber());
            params.put("customerAgencyCode",
                    ObjectUtils.extractBranchCode(customerData.getBalanceActivity().getAccountNumber()));
        }

        // === LOAN DATA ===
        if (dossier.getLoanData() != null) {
            var loanData = dossier.getLoanData();
            putSafe(params, "loanAmount",
                    () -> MoneyToWords.formatConvertAmount(loanData.getLoanAmount()));
            putSafe(params, "rateType",
                    () -> loanData.getRateType().getDesignation());
            putSafe(params, "rate",
                    () -> ObjectUtils.formatPercentage(BigDecimal.valueOf(loanData.getRate())));
            putSafe(params, "rateNature",
                    () -> loanData.getRateNature().getDesignation());
            putSafe(params, "freeHandFee",
                    () -> MoneyToWords.formatAmount(loanData.getFreeHandFee()));
            putSafe(params, "loanDuration",
                    () -> formatLoanDuration(loanData.getDeadlineNumber(), true));
            putSafe(params, "deferredDuration",
                    () -> formatLoanDuration(loanData.getDelayDuration(), loanData.getDelayed()));
            putSafe(params, "deferredType",
                    () -> loanData.getDelayType().getDesignation());
            putSafe(params, "loanApplicationFee",
                    () -> MoneyToWords.freeAmount(loanData.getApplicationFee()));
            putSafe(params, "periodicity",
                    () -> loanData.getPeriodicity().getDesignation());
            putSafe(params, "loanObject",
                    () -> loanData.getLoanObject().getDesignation());
        }

        // === LOAN DETAIL ===
        if (loanDetail != null && loanDetail.getLoanDetail() != null) {
            var detail = loanDetail.getLoanDetail();
            putSafe(params, "globalEffectiveRate",
                    () -> ObjectUtils.formatPercentage(detail.getTeg()));
            putSafe(params, "firstInstallmentDate",
                    () -> DateUtils.convertLocalDateToString(detail.getFirstInstallmentDate(), "dd/MM/yyyy"));
            putSafe(params, "lastInstallmentDate",
                    () -> DateUtils.convertLocalDateToString(detail.getLastInstallmentDate(), "dd/MM/yyyy"));
            putSafe(params, "loanAmountAmplitude",
                    () -> MoneyToWords.formatConvertAmount(detail.getLoanAmount()));
            putSafe(params, "totalCreditCost",
                    () -> MoneyToWords.formatConvertAmount(detail.getTotalCreditCost()));
            putSafe(params, "insuranceCost",
                    () -> MoneyToWords.formatConvertAmount(detail.getInsuranceCost()));
            putSafe(params, "constantInstallmentAmount",
                    () -> MoneyToWords.formatAmount(detail.getConstantInstallmentAmount()));
            putSafe(params, "bonusRate",
                    () -> ObjectUtils.formatPercentage(detail.getInsuranceRate()));
        }

        return params;
    }

    /**
     * Get latest notification date from dossier attachments
     */
    private LocalDateTime getLatestNotificationDate(DossierDataDto dossier) {
        return Optional.ofNullable(dossier.getDossierAttachmentTypes())
                .orElse(Collections.emptyList())
                .stream()
                .filter(dat -> "NOTIFICATION".equals(dat.getCodeRefAttachmentType()))
                .flatMap(dat -> dat.getAttachments().stream())
                .map(AttachmentDto::getUploadedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    /**
     * Construct full customer name with null safety
     */
    private String constructCustomerFullName(DossierDataDto dossier) {
        if (dossier == null || dossier.getCustomerData() == null) {
            return "";
        }

        var info = dossier.getCustomerData().getPersonalInfo();
        if (info == null) {
            return "";
        }

        return String.format("MME/MR %s %s",
                StringUtils.defaultString(info.getLastName()),
                StringUtils.defaultString(info.getFirstName())).trim();
    }

    /**
     * Format loan duration with deferral flag
     */
    private String formatLoanDuration(Integer deadlineNumber, Boolean delayed) {
        if (delayed == null || !delayed || deadlineNumber == null) {
            return "";
        }
        return String.format("%d mois", deadlineNumber);
    }

    /**
     * Lazy evaluation wrapper for null-safe parameter setting
     */
    private void putSafe(Map<String, Object> map, String key, ParameterSupplier supplier) {
        try {
            Object value = supplier.get();
            if (value != null) {
                map.put(key, value);
            }
        } catch (Exception e) {
            log.warn("Failed to evaluate parameter '{}': {}", key, e.getMessage());
        }
    }

    @FunctionalInterface
    private interface ParameterSupplier {
        Object get() throws Exception;
    }
}