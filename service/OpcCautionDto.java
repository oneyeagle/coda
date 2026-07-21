package ma.sg.its.octroicreditapi.dto.opc;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class OpcCautionDto {
        private String nameCaution;
        private String addressCaution;
        private String dateBirthCaution;
        private String placeBirthCaution;
        private String nationalIdentityCaution;
        private String issuedCaution;
        private String natureCaution;
        private String representative;

}
