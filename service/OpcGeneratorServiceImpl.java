package ma.sg.its.octroicreditapi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.sg.its.octroicredit.common.exception.TechnicalException;
import ma.sg.its.octroicreditapi.dto.DossierDataDto;
import ma.sg.its.octroicreditapi.dto.LoanDetailValidationResult;
import ma.sg.its.octroicreditapi.service.OpcGeneratorService;
import ma.sg.its.octroicreditapi.service.impl.opc.HtmlPdfRenderer;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpcGeneratorServiceImpl implements OpcGeneratorService {

    private final HtmlPdfRenderer htmlPdfRenderer;

    @Override
    public byte[] generateOpc(DossierDataDto dossierDataDto,
                              LoanDetailValidationResult documentGeneratorDto) {

        if (dossierDataDto == null) {
            throw new TechnicalException("Dossier data cannot be null");
        }

        if (dossierDataDto.getProduct() == null) {
            throw new TechnicalException("Product information is required");
        }

        String productCode = dossierDataDto.getProduct().getCode();
        String dossierCode = dossierDataDto.getCodeDossier();

        log.info("Start: generate OPC document for dossier: {} product: {}",
                dossierCode, productCode);

        try {
            byte[] pdfBytes = htmlPdfRenderer.generatePdf(productCode, dossierDataDto, documentGeneratorDto);

            if (pdfBytes == null || pdfBytes.length == 0) {
                throw new TechnicalException("PDF generation produced empty result");
            }

            return pdfBytes;

        } catch (TechnicalException e) {
            log.error("Technical error generating OPC for dossier: {} product: {}",
                    dossierCode, productCode, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error generating OPC for dossier: {} product: {}",
                    dossierCode, productCode, e);
            throw new TechnicalException(
                    "Failed to generate PDF for dossier " + dossierCode);
        }
    }
}