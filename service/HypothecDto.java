package ma.sg.its.octroicreditapi.dto.opc;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class HypothecDto {
    private Collection<OpcRangDto> rangs;
    private Collection<OpcPropertyDto> properties;

}
