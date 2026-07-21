package ma.sg.its.octroicreditapi.dto.opc;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpcPropertyDto {
    private final String titreFoncierBien;
    private final String villeBien;
    private final String typeBien;
    private final String descriptionBien;
    private final String nomBien;
    private final String areaBien;
}
