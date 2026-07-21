package ma.sg.its.octroicreditapi.dto.opc;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpcCustomerDto {
    private String customerName;
    private String customerAddress;
    private String customerBirthDate;
    private String customerBirthPlace;
    private String customerCardId;
    private String customerCardIssuedDate;
    private String intitule;
    private String mandataire;

}

