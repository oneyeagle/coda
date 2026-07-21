package ma.sg.its.octroicreditapi.dto.opc;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class DAJHypothecDto {
    private Collection<HypothecDto> listeTitresFonciers;
    private String nom_bien;
    private String numero_titre_foncier;
    private String ville_propriete;
    private String superficie;
    private String enrichissement;

}
