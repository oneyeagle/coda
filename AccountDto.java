package ma.gov.acaps.common;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AccountDto(
        String accountHolder,
        String iban,
        String currency,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal openingBalance,
        BigDecimal closingBalance,
        List<TransactionDto> transactions
) {}

 record TransactionDto(
        LocalDate bookingDate,
        String description,
        String reference,
        BigDecimal debit,      // null when credit entry
        BigDecimal credit,     // null when debit entry
        BigDecimal runningBalance
) {}