package org.pn.dto;

import org.pn.Currency;

public record ExchangeCurrenctyDTO(Long id, double amount, Currency targetCurrency){
}
