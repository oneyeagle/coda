package ma.sg.its.octroicreditapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import ma.sg.its.octroicredit.common.dto.*;
import ma.sg.its.octroicredit.common.validation.constraint.*;
import ma.sg.its.octroicredit.common.validation.enums.ValidatorOperationType;
import ma.sg.its.octroicredit.common.validation.group.FunctionalValidationGroup;
import ma.sg.its.octroicredit.common.validation.group.MandatoryValidationGroup;
import ma.sg.its.octroicreditapi.constant.ApplicationConstants;
import ma.sg.its.octroicreditapi.dto.core.DossierOrganization;
import ma.sg.its.octroicreditapi.validator.ProspectValidationGroup;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@FieldNameConstants
@ExpectUniqueFieldValue(fieldName = "guarantors", equalsFieldName = "idCardNumber", groups = {FunctionalValidationGroup.class, ProspectValidationGroup.class})
@ExpectUniqueFieldValue(fieldName = "beneficiaries", equalsFieldName = "idCardNumber", groups = {FunctionalValidationGroup.class, ProspectValidationGroup.class})
@ExpectedIfAnotherFieldHasValues(fieldName = "product.code", expectedFieldValue = {
		ApplicationConstants.LOAN_TYPE_ADL_SAKANE_PPR_CODE, ApplicationConstants.LOAN_TYPE_ADL_SAKANE_CODE,
		ApplicationConstants.LOAN_TYPE_IMTILAK_PPR_CODE, ApplicationConstants.LOAN_TYPE_IMTILAK_CODE,
		ApplicationConstants.LOAN_TYPE_SALAF_BAYTI_SANTE_PPR_CODE, ApplicationConstants.LOAN_TYPE_SALAF_BAYTI_SANTE_CODE,
		ApplicationConstants.LOAN_TYPE_FOGARIM_CODE, ApplicationConstants.LOAN_TYPE_FOGALOGE_CODE,
		ApplicationConstants.LOAN_TYPE_PPI_PPR_CODE }, dependingFieldName = "loanData.rateType.code", dependingFieldExpectedValue = ApplicationConstants.LOAN_RATE_TYPE_FIXE_CODE, groups = {FunctionalValidationGroup.class, ProspectValidationGroup.class})

@NotNullIfAnotherFieldHasValue(fieldName = "product.code", expectedFieldValue = ApplicationConstants.LOAN_TYPE_ADL_SAKANE_CODE
		+ "," + ApplicationConstants.LOAN_TYPE_ADL_SAKANE_PPR_CODE + "," + ApplicationConstants.LOAN_TYPE_IMTILAK_CODE
		+ ","+ ApplicationConstants.LOAN_TYPE_IMTILAK_PPR_CODE + ApplicationConstants.LOAN_TYPE_SALAF_BAYTI_SANTE_CODE
		+ ","+ ApplicationConstants.LOAN_TYPE_SALAF_BAYTI_SANTE_PPR_CODE,
		dependFieldName = "loanData.mechanisms", operationType = ValidatorOperationType.CONTAINS, groups = {MandatoryValidationGroup.class, ProspectValidationGroup.class} )

@NotNullIfAnotherFieldHasValue(fieldName = "product.code", expectedFieldValue = ApplicationConstants.LOAN_TYPE_FOGALOGE_CODE
		+ ","
		+ ApplicationConstants.LOAN_TYPE_FOGARIM_CODE, operationType = ValidatorOperationType.INTO, dependFieldName = "loanData.socialHousing", groups = {MandatoryValidationGroup.class, ProspectValidationGroup.class})
@NotNullIfAnotherFieldHasValue(fieldName = "product.code", expectedFieldValue = ApplicationConstants.LOAN_TYPE_FOGALOGE_CODE
		+ ","
		+ ApplicationConstants.LOAN_TYPE_FOGARIM_CODE, operationType = ValidatorOperationType.INTO, dependFieldName = "loanData.ccgCommissionChargeType.code", groups = {MandatoryValidationGroup.class, ProspectValidationGroup.class})
@NotNullIfAnotherFieldHasValue(fieldName = "product.code", expectedFieldValue = ApplicationConstants.LOAN_TYPE_FOGALOGE_CODE
		+ ","
		+ ApplicationConstants.LOAN_TYPE_FOGARIM_CODE, operationType = ValidatorOperationType.INTO, dependFieldName = "loanData.area", groups = {MandatoryValidationGroup.class, ProspectValidationGroup.class})

@NotNullIfAnotherFieldHasValue(fieldName = "product.code", expectedFieldValue = ApplicationConstants.LOAN_TYPE_FOGALOGE_CODE
		+ ","
		+ ApplicationConstants.LOAN_TYPE_FOGARIM_CODE, operationType = ValidatorOperationType.INTO, dependFieldName = "loanData.monthlyCoefficient", groups = {MandatoryValidationGroup.class, ProspectValidationGroup.class})

@NotNullIfAnotherFieldHasValue(fieldName = "product.code", expectedFieldValue = ApplicationConstants.LOAN_TYPE_ADL_SAKANE_CODE
		+ "," +ApplicationConstants.LOAN_TYPE_IMTILAK_CODE
		+ "," + ApplicationConstants.LOAN_TYPE_IMTILAK_PPR_CODE
		+ "," +ApplicationConstants.LOAN_TYPE_SALAF_BAYTI_SANTE_CODE
		+ "," + ApplicationConstants.LOAN_TYPE_SALAF_BAYTI_SANTE_PPR_CODE
		+ "," + ApplicationConstants.LOAN_TYPE_ADL_SAKANE_PPR_CODE, operationType = ValidatorOperationType.NOT_CONTAINS, dependFieldName = "insuranceData.insuredPercentage", groups ={ MandatoryValidationGroup.class,ProspectValidationGroup.class})
@NotNullIfAnotherFieldHasValue(fieldName = "product.code", expectedFieldValue = ApplicationConstants.LOAN_TYPE_ADL_SAKANE_CODE
		+ "," +ApplicationConstants.LOAN_TYPE_IMTILAK_CODE
		+ "," + ApplicationConstants.LOAN_TYPE_IMTILAK_PPR_CODE
		+ "," +ApplicationConstants.LOAN_TYPE_SALAF_BAYTI_SANTE_CODE
		+ "," + ApplicationConstants.LOAN_TYPE_SALAF_BAYTI_SANTE_PPR_CODE
		+ "," + ApplicationConstants.LOAN_TYPE_ADL_SAKANE_PPR_CODE, operationType = ValidatorOperationType.NOT_CONTAINS, dependFieldName = "insuranceData.insuranceCoefficient",  groups ={ MandatoryValidationGroup.class,ProspectValidationGroup.class})

@NotNullIfAnotherFieldHasValue(fieldName = "loanData.mechanisms", expectedFieldValue = ApplicationConstants.MECHANISM_TYPE_A_CODE , dependFieldName = "insuranceData.typeAInsuredPercentage", operationType = ValidatorOperationType.CONTAINS, groups = MandatoryValidationGroup.class)

@NotNullIfAnotherFieldHasValue(fieldName = "loanData.mechanisms", expectedFieldValue = ApplicationConstants.MECHANISM_TYPE_A_CODE , dependFieldName = "insuranceData.typeAInsuranceCoefficient", operationType = ValidatorOperationType.CONTAINS, groups = MandatoryValidationGroup.class)

@NotNullIfAnotherFieldHasValue(fieldName = "loanData.mechanisms", expectedFieldValue = ApplicationConstants.MECHANISM_TYPE_B_CODE, dependFieldName = "insuranceData.typeBInsuredPercentage", operationType = ValidatorOperationType.CONTAINS, groups = MandatoryValidationGroup.class)

@NotNullIfAnotherFieldHasValue(fieldName = "loanData.mechanisms", expectedFieldValue = ApplicationConstants.MECHANISM_TYPE_B_CODE , dependFieldName = "insuranceData.typeBInsuranceCoefficient", operationType = ValidatorOperationType.CONTAINS, groups = MandatoryValidationGroup.class)

@NotNullIfAnotherFieldHasValue(fieldName = "loanData.mechanisms", expectedFieldValue = ApplicationConstants.MECHANISM_TYPE_A_CODE
		+ ","
		+ ApplicationConstants.MECHANISM_TYPE_B_CODE, operationType = ValidatorOperationType.CONTAINS, dependFieldName = "insuranceData.aditionalCreditInsuredPercentage", groups = MandatoryValidationGroup.class)

@NotNullIfAnotherFieldHasValue(fieldName = "loanData.mechanisms", expectedFieldValue = ApplicationConstants.MECHANISM_TYPE_A_CODE
		+ ","
		+ ApplicationConstants.MECHANISM_TYPE_B_CODE, operationType = ValidatorOperationType.CONTAINS, dependFieldName = "insuranceData.aditionalCreditInsuranceCoefficient", groups = MandatoryValidationGroup.class)



@NotNullIfAnotherFieldHasValue(fieldName = "loanData.mechanisms", expectedFieldValue = ApplicationConstants.MECHANISM_MECHANISM1_CODE , dependFieldName = "insuranceData.subsidizedInsuredPercentage", operationType = ValidatorOperationType.CONTAINS, groups = MandatoryValidationGroup.class)

@NotNullIfAnotherFieldHasValue(fieldName = "loanData.mechanisms", expectedFieldValue = ApplicationConstants.MECHANISM_MECHANISM1_CODE , dependFieldName = "insuranceData.subsidizedInsuranceCoefficient", operationType = ValidatorOperationType.CONTAINS, groups = MandatoryValidationGroup.class)

@NotNullIfAnotherFieldHasValue(fieldName = "loanData.mechanisms", expectedFieldValue = ApplicationConstants.MECHANISM_MECHANISM2_CODE, dependFieldName = "insuranceData.bonusInsuredPercentage", operationType = ValidatorOperationType.CONTAINS, groups = MandatoryValidationGroup.class)

@NotNullIfAnotherFieldHasValue(fieldName = "loanData.mechanisms", expectedFieldValue = ApplicationConstants.MECHANISM_MECHANISM2_CODE , dependFieldName = "insuranceData.bonusInsuranceCoefficient", operationType = ValidatorOperationType.CONTAINS, groups = MandatoryValidationGroup.class)

@NotNullIfAnotherFieldHasValue(fieldName = "loanData.mechanisms", expectedFieldValue = ApplicationConstants.MECHANISM_MECHANISM3_CODE, dependFieldName = "insuranceData.suportedInsuredPercentage", operationType = ValidatorOperationType.CONTAINS, groups = MandatoryValidationGroup.class)

@NotNullIfAnotherFieldHasValue(fieldName = "loanData.mechanisms", expectedFieldValue = ApplicationConstants.MECHANISM_MECHANISM3_CODE , dependFieldName = "insuranceData.suportedInsuranceCoefficient", operationType = ValidatorOperationType.CONTAINS, groups = MandatoryValidationGroup.class)

@NotNullIfAnotherFieldHasValue(fieldName = "loanData.mechanisms", expectedFieldValue = ApplicationConstants.MECHANISM_MECHANISM1_CODE
		+ ","
		+ ApplicationConstants.MECHANISM_MECHANISM2_CODE+ ","+ ApplicationConstants.MECHANISM_MECHANISM3_CODE, operationType = ValidatorOperationType.CONTAINS, dependFieldName = "insuranceData.aditionalCreditInsuredPercentage", groups = MandatoryValidationGroup.class)

@NotNullIfAnotherFieldHasValue(fieldName = "loanData.mechanisms", expectedFieldValue = ApplicationConstants.MECHANISM_MECHANISM1_CODE
		+ ","
		+ ApplicationConstants.MECHANISM_MECHANISM2_CODE+ ","+ ApplicationConstants.MECHANISM_MECHANISM3_CODE, operationType = ValidatorOperationType.CONTAINS, dependFieldName = "insuranceData.aditionalCreditInsuranceCoefficient", groups = MandatoryValidationGroup.class)

@NotNullIfAnotherFieldHasValue(fieldName = "product.code", expectedFieldValue = ApplicationConstants.LOAN_TYPE_MOULKIA_CODE
		+ "," + ApplicationConstants.PPI_VEFA_RESIDENT, operationType = ValidatorOperationType.NOT_CONTAINS,
		dependFieldName = "notary.name",  groups = MandatoryValidationGroup.class)

@NotNullIfAnotherFieldHasValue(fieldName = "product.code", expectedFieldValue = ApplicationConstants.LOAN_TYPE_MOULKIA_CODE
		+ "," + ApplicationConstants.PPI_VEFA_RESIDENT, operationType = ValidatorOperationType.NOT_CONTAINS,
		dependFieldName = "notary.address",  groups = MandatoryValidationGroup.class)

@NotNullIfAnotherFieldHasValue(fieldName = "product.code", expectedFieldValue = ApplicationConstants.LOAN_TYPE_MOULKIA_CODE
		+ "," + ApplicationConstants.PPI_VEFA_RESIDENT, operationType = ValidatorOperationType.NOT_CONTAINS,
		dependFieldName = "notary.phone", groups = MandatoryValidationGroup.class)

@NotNullIfAnotherFieldHasValue(fieldName = "product.code", expectedFieldValue = ApplicationConstants.LOAN_TYPE_MOULKIA_CODE
		+ "," + ApplicationConstants.PPI_VEFA_RESIDENT, operationType = ValidatorOperationType.NOT_CONTAINS,
		dependFieldName = "notary.email", groups = MandatoryValidationGroup.class)


public class DossierDataDto {

	@UnexpectedReferentialValue(referentialClass = RefProductDto.class, groups = FunctionalValidationGroup.class)
	private CodeLabelDto product;
	@Valid
	private IndividualCustomerDTO customerData;
	private List<DebtDto> debts;

	private List<DebtInfnDto> debtsinfon;
	@Valid
	private FinancialDataDto financialData;
	@Valid
	private LoanDataDto loanData;
	@Valid
	private EmployerDto employer;
	@Valid
	private List<GuarantorDto> guarantors;
	@Valid
	private List<BeneficiaryDto> beneficiaries;
	private List<DossierAttachmentTypeDto> dossierAttachmentTypes;
	@Valid
	private PropertyDataDto propertyData;
	@Valid
	private NotaryDto notary;
	@Valid
	private List<RepresentativeDto> representatives;
	@Valid
	private InsuranceDataDto insuranceData;
	private Set<DossierUserDto> dossierUsers;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String uuid;
	private String codeStatus;
	private String codeStage;
	private String status;
	private String stage;
	private String codeDossier;
	private BigDecimal mounthlyInstallment;
	private List<CommentDto> comments;
	private List<RestrictionDto> restrictions;
	private List<WarrantyDto> warranties;
	private Boolean poolCandidate;
	private Boolean assignedToMe;
	private LocalDate opcDeliveryDate;
	private LocalDate dateOfReceiptOpcSigned;
	private LocalDate minuteRequestCommitmentDate;
	private LocalDate dateOfReceiptMinuteAndCommitment;
	private LocalDate dateOfReceiptPhysicalFile;
	private Boolean returnDecisionInstance;
	private String assignee;
	private DossierOrganization dossierOrganization;
	private LocalDateTime firstReleasedDate;
	private CcgCommessionMatrix ccgCommessionMatrix;
	private String taskStatus;
	private Boolean hasTaskExpertise;
	private AccordType accord;
	private String prospectUuid;
	private String flowStatus;
    private LocalDateTime validationOpcDate;
    private LocalDateTime approvalDate;
    private LocalDateTime dscTransferDate;
	private String expertiseTaskAssignee;
}
