package ma.sg.its.octroicreditapi.dto.opc;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class OpcWarrantyDto {
    private final Integer rang;
    private final String amount;
    private final String landCertificate;

}
