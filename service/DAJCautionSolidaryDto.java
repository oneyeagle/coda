package ma.sg.its.octroicreditapi.dto.opc;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DAJCautionSolidaryDto {
    private String nom_complet_garant;
    private String adresse_caution;
    private String cin_garant;
}
