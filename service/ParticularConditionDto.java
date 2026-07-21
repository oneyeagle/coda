package ma.sg.its.octroicreditapi.dto.opc;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Getter
@Builder
public class ParticularConditionDto {
    private final List<OpcCustomerDto> customers;
    private final List<OpcCautionDto> cautionBeneficiaries;
    private final List<OpcPropertyDto> properties;
    private final List<String> reserves;
    private final Integer warrantyRang;
    private final String warrantyAmount;
    private final String warrantyLandCertificate;
    private final List<String> warranties;

}
