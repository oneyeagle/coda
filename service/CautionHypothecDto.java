package ma.sg.its.octroicreditapi.dto.opc;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class CautionHypothecDto {
    private String beneficiaryFullName;
    private String beneficiaryAddress;
    private String beneficiaryCardID;
    private String propertyName;
    private String propertyLandCertificate;
    private String propertyCity;
    private String propertyArea;
    private String enrichment;
    private Collection<OpcRangDto> propertyRanks;
}
