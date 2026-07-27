# OPC — Offre Préalable de Crédit (PDF generator)

Spring Boot service that turns a loan dossier (`DossierDataDto` +
`LoanDetailValidationResultDto`) into a bilingual French/Arabic PDF —
the **Offre Préalable de Crédit** document required for Moroccan mortgage
loans. Thymeleaf renders the document as HTML; openhtmltopdf converts that
HTML to a PDF, with full Arabic RTL shaping/bidi support.

This README is the map. For the dot-leader rendering technique and the
openhtmltopdf-specific gotchas, see [`DOT-LEADER-RULES.md`](DOT-LEADER-RULES.md)
— read it before touching any `.html` fragment under `templates/opc/`.

## Stack

| Layer | Tech |
|---|---|
| Runtime | Java 17, Spring Boot 4.1.0 |
| Templating | Thymeleaf (`spring-boot-starter-thymeleaf`) |
| HTML → PDF | openhtmltopdf 1.1.41 (`-core` + `-pdfbox` + `-rtl-support`) |
| RTL/Arabic | ICU bidi splitter/reorderer (from `openhtmltopdf-rtl-support`), wired in `HtmlPdfRenderer` |
| Fonts | Noto Sans (Latin), Noto Naskh Arabic — embedded under `src/main/resources/fonts/` |
| JSON | Jackson (`jackson-databind` + `jackson-datatype-jsr310`) — currently test-only, for loading mock dossiers |
| Test PDF assertions | iText `kernel` (test scope only — not a runtime dependency) |

## Quick start

```bash
./mvnw test              # runs OpcGeneratorServiceTest, writes target/opc_ppi_classic.pdf
./mvnw spring-boot:run   # boots the Spring context (no HTTP endpoint exists yet - see "Gaps" below)
```

Windows: use `mvnw.cmd` instead of `./mvnw`.

There's no Docker/shell wrapper beyond the Maven wrapper — that's the whole
dev workflow today.

## How a PDF gets built

```
DossierDataDto + LoanDetailValidationResultDto
        │
        ▼
OpcGeneratorServiceImpl.generateOpc(...)      validates input, wraps errors as TechnicalException
        │
        ▼
HtmlPdfRenderer.generatePdf(productCode, dossier, loanDetail)
        │
        ├─▶ ProductDocumentRegistry.getApplicableTemplates(productCode, ...)
        │       looks up List<DocumentTemplate> for the product code,
        │       filters out any template whose isApplicable(...) is false
        │
        ├─▶ for each applicable DocumentTemplate: template.prepareData(...)
        │       → one Thymeleaf "section" = {fragmentName, displayName, data}
        │
        ├─▶ CommonParamsBuilder.buildCommonParams(...)
        │       → cross-cutting values every fragment can use: signedAt,
        │         customerFullName, loan amounts/rates (formatted via
        │         MoneyToWords/ObjectUtils/DateUtils), etc.
        │
        ├─▶ TemplateEngine.process("opc/main_report", {sections, commonParams, ...})
        │       → one big HTML string
        │
        └─▶ openhtmltopdf: PdfRendererBuilder + embedded fonts + ICU bidi
                → byte[] PDF
```

`main_report.html` is completely product-agnostic — it just loops over
whatever `sections` it's given and inserts each fragment by name. All
product-specific logic (which sections, in what order, under what
conditions) lives in `ProductDocumentRegistry`.

## Package layout (`src/main/java/opc/ma/`)

| Package | What's there |
|---|---|
| `OpcApplication` | `@SpringBootApplication` entry point |
| `service/` | `OpcGeneratorService`/`Impl` (public facade), `HtmlPdfRenderer` (orchestrator + PDF conversion), `ProductDocumentRegistry` (product → template list), `CommonParamsBuilder` (shared template params) |
| `template/` | `DocumentTemplate` interface + one implementation per document section (see below) |
| `dto/` | Input DTOs (deserialized dossier/loan graph) and view DTOs (assembled by `template/*` for Thymeleaf) |
| `util/` | `StringUtils`, `ObjectUtils`, `MoneyToWords` (French number-to-words), `DateUtils`, `LocationUtils` |
| `exception/` | `TechnicalException` — the one error type thrown across the service layer |
| `config/` | `PdfTemplateConfig` — **currently dead code, entirely commented out**; Thymeleaf is actually wired via Spring Boot's `ThymeleafAutoConfiguration` |

### `DocumentTemplate` implementations (one per PDF section)

| Class | Fragment name | Applicable when | Renders |
|---|---|---|---|
| `GeneralConditionsTemplate` | `general_conditions` | always | Static bilingual legal boilerplate, no dossier data |
| `ParticularConditionTemplate` | fragment name passed to constructor (`"pc_ppi_classic"` or `"pc_ppi_ppr"`) | `dossier.getCustomerData() != null` | Customer/caution/property/warranty details — the actual "Conditions Particulières" |
| `HypothecTemplate` | `attach_hypothec` | ≥1 borrower-beneficiary with properties | Mortgage annex |
| `CautionHypothecTemplate` | `attach_caut_hypothecaire` | ≥1 non-borrower beneficiary | Mortgage-guarantee annex |
| `CautionSolidaireTemplate` | `attach_caut_solidaire` | ≥1 non-borrower guarantor | Joint-guarantee annex |

## Templates (`src/main/resources/templates/opc/`)

```
main_report.html               document shell — iterates ${sections}
fragments/
  general_conditions.html      static FR/AR legal text
  pc_ppi_classic.html          Conditions Particulières (WIP - see Gaps)
  attach_hypothec.html
  attach_caut_hypothecaire.html
  attach_caut_solidaire.html
  shared/
    styles.html                @page size/margin, base FR/AR table styles
    id_card_row.html           reusable row(labelFr, value, labelAr) / tagRow / mandataireRow fragments
```

**Every fragment file needs a `<style th:fragment="styles">` block**, and
`main_report.html` pulls it via `~{|opc/fragments/${section.fragmentName}| :: styles}`
— but that only works for *top-level* section fragments. A shared fragment
like `id_card_row.html` is **not** auto-discovered; any consumer must
explicitly `th:replace` its `:: styles` fragment into its own (see
`pc_ppi_classic.html`'s `<head>` for the pattern). Forgetting this is a real
trap: the symptoms look like unrelated bugs (Arabic renders as `#######`,
rows wrap onto extra lines, borders vanish) with no CSS error to point at
the cause. Full story in `DOT-LEADER-RULES.md`.

### Dot-leader rows (`labelFr : value ..........  labelAr`)

Dots are painted as a small repeating **background-image tile**, not
literal `.` characters — openhtmltopdf 1.1.41 has a line-breaking bug that
corrupts long single text runs, which literal dot-leader text is. See
`div.row.lead`/`.dotsfill`/`.lbl`/`.arlbl` in `pc_ppi_classic.html` and
`div.cdr-row`/`.cdr-dotsfill`/`.cdr-lbl`/`.cdr-arlbl` in `id_card_row.html`.
**Read `DOT-LEADER-RULES.md` before changing any of this** — it documents
several openhtmltopdf-specific rendering bugs (line-breaking, `position:
absolute` stacking order, `unicode-bidi` not being implemented at all) that
will silently reintroduce themselves if the CSS is "simplified."

## Adding a new product

1. Add a fragment file under `fragments/` for its Conditions Particulières
   (or reuse `pc_ppi_classic.html`/`pc_ppi_ppr.html` if the layout is
   shared).
2. In `ProductDocumentRegistry`, add a `Map.entry("NEW_CODE", List.of(...))`
   — the list order is the page order in the output PDF.
3. If the section needs new data, add/extend a `DocumentTemplate`
   implementation (`prepareData` builds whatever object the fragment's
   `${data}` expects) and whatever DTOs it needs under `dto/`.
4. Add a mock-JSON test case (see Testing below) — there is currently no
   test for `PPI_PPR_FONC` or `PPI_MRE`, so a broken wiring for those
   product codes won't be caught by CI.

## Testing

`OpcGeneratorServiceTest` boots a **narrow** Spring context — just
`ThymeleafAutoConfiguration`, `MessageSourceAutoConfiguration`, and the four
service beans it needs — not the full `@SpringBootApplication`. It loads
`src/test/resources/mock/dossier.json` and `mock/loan-detail.json` through
Jackson (`JavaTimeModule` registered for `LocalDate`/`LocalDateTime`) into
`DossierDataDto`/`LoanDetailValidationResultDto`, calls
`generateOpc(...)`, and asserts on the resulting PDF bytes (size, page
count via iText). The generated PDF is also written to
`target/opc_ppi_classic.pdf` for manual inspection — open it after any
template change.

To add a fixture for a new scenario, add a JSON file under
`src/test/resources/mock/` shaped like `dossier.json` and load it the same
way.

## Gaps / known issues for new devs

- **`pc_ppi_ppr.html` doesn't exist.** `ProductDocumentRegistry` wires
  `PPI_PPR_FONC` to `ParticularConditionTemplate("pc_ppi_ppr")`, but no such
  fragment file exists under `fragments/`. Generating that product today
  throws a `TechnicalException` at template resolution. No test covers it.
- **`pc_ppi_classic.html` is incomplete.** Only "Article 1: Identification
  des Parties" is implemented; the file has explicit placeholder comments
  for Articles 2–6 and the Signatures section. The passing test only checks
  the PDF is non-empty and valid, not that it's *complete* — don't take a
  green test as proof the document is finished.
- **`ProductDocumentRegistry`'s `PPI_CLASSIQUE` entry only renders
  Conditions Particulières** — `GeneralConditionsTemplate`,
  `HypothecTemplate`, `CautionHypothecTemplate`, `CautionSolidaireTemplate`
  are commented out for that product (asymmetric vs. `PPI_MRE`/
  `PPI_PPR_FONC`, which include all five). Confirm with whoever owns the
  product spec before uncommenting or leaving as-is.
- **No HTTP entry point.** `OpcGeneratorService` is only ever called from
  the test class — there's no controller, queue consumer, or other caller
  wired up yet.
- **`config/PdfTemplateConfig.java` is dead code** (fully commented out).
  Don't assume it does anything; Thymeleaf resolution goes through Spring
  Boot's auto-configuration instead.
- **`dto/DAJHypothecDto.java` appears unused** — no template or service
  references it.
- **`LocationUtils.getCityFromTimezone()` is a stub** — always returns the
  literal string `"Casablanca"`, despite the name.
- A test comment references `HARD-RULE.md` for context on the mock-JSON
  testing approach; that file doesn't currently exist in the repo.

## Further reading

- [`DOT-LEADER-RULES.md`](DOT-LEADER-RULES.md) — the authoritative reference
  for anything involving dot-leader rows, RTL/bidi rendering, or
  openhtmltopdf CSS support gaps. Required reading before editing template
  CSS.