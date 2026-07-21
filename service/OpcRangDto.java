package ma.sg.its.octroicreditapi.dto.opc;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpcRangDto {
    private String amount;
    private String rang;
}
