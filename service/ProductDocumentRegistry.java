package ma.sg.its.octroicreditapi.service.impl.opc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class ProductDocumentRegistry {

    private final Map<String, List<DocumentTemplate>> productTemplates;

    public ProductDocumentRegistry() {
        this.productTemplates = Map.ofEntries(
                Map.entry("PPI_CLASSIQUE", List.of(
                        new GeneralConditionsTemplate(),
                        new ParticularConditionTemplate("pc_ppi_classic"),
                        new HypothecTemplate(),
                        new CautionHypothecTemplate(),
                        new CautionSolidaireTemplate()
                )),
                Map.entry("PPI_PPR_FONC", List.of(
                        new GeneralConditionsTemplate(),
                        new ParticularConditionTemplate("pc_ppi_ppr"),
                        new HypothecTemplate(),
                        new CautionHypothecTemplate(),
                        new CautionSolidaireTemplate()
                )),
                Map.entry("PPI_MRE", List.of(
                        new GeneralConditionsTemplate(),
                        new ParticularConditionTemplate("pc_ppi_classic"),
                        new HypothecTemplate(),
                        new CautionHypothecTemplate(),
                        new CautionSolidaireTemplate()
                ))
        );
    }

    /**
     * Get all templates applicable for a given product and dossier
     */
    public List<DocumentTemplate> getApplicableTemplates(
            String productCode,
            ma.sg.its.octroicreditapi.dto.DossierDataDto dossier,
            ma.sg.its.octroicreditapi.dto.LoanDetailValidationResult loanDetail) {

        return productTemplates
                .getOrDefault(productCode, List.of())
                .stream()
                .peek(t -> {
                    if (!t.isApplicable(dossier, loanDetail)) {
                        log.debug("Template {} not applicable for product {}",
                                t.getDisplayName(), productCode);
                    }
                })
                .filter(t -> t.isApplicable(dossier, loanDetail))
                .toList();
    }

    /**
     * Get all registered products
     */
    public Set<String> getRegisteredProducts() {
        return productTemplates.keySet();
    }
}