package org.pn;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static org.pn.Currency.PLN;
import static org.pn.Currency.USD;

@Slf4j
@Service
@RequiredArgsConstructor
class AccountService {

    private static final Set<Currency> SUPPORTED_CURRENCIES = EnumSet.of(PLN, USD);

    private final AccountRepository accountRepository;
    private final NbpExchangeRateService nbpExchangeRateService;

    Account createAccount(Account accountToCreate) {
        return accountRepository.save(accountToCreate);
    }

    Optional<Account> getAccountById(Long accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            log.error("Account with id {} not found", accountId);
            throw new RuntimeException("Account not found");
        }
        return accountOpt;
    }

    Account exchangeCurrency(Long accountId, double amount, Currency targetCurrency) {
        if (!SUPPORTED_CURRENCIES.contains(targetCurrency)) {
            log.error("Unsupported currency {}. accountId:{}", targetCurrency, accountId);
            throw new RuntimeException("Unsupported currency");
        }
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            log.error("Account with id {} not found", accountId);
            throw new RuntimeException("Account not found");
        }
        Account account = accountOpt.get();
        double exchangeRate = nbpExchangeRateService.getUSDExchangeRate();

        if (targetCurrency == PLN) {
            double exchangeCost = amount / exchangeRate;
            if (exchangeCost > account.getBalanceUSD()) {
                log.error("Not enough USD to perform currency exchange. amount:{}, targetCurrency:{}, account:{}", amount, targetCurrency, account);
                throw new RuntimeException("Not enough USD to exchange");
            }

            account.setBalancePLN(account.getBalancePLN() + amount);
            account.setBalanceUSD(account.getBalanceUSD() - exchangeCost);
            return accountRepository.save(account);
        } else if (targetCurrency == USD) {
            double exchangeCost = amount * exchangeRate;
            if (exchangeCost > account.getBalancePLN()) {
                log.error("Not enough PLN to perform currency exchange. amount:{}, targetCurrency:{}, account:{}", amount, targetCurrency, account);
                throw new RuntimeException("Not enough PLN to exchange");
            }
            account.setBalanceUSD(account.getBalanceUSD() + amount);
            account.setBalancePLN(account.getBalancePLN() - exchangeCost);
            return accountRepository.save(account);
        } else {
            log.error("Unexpected case - Currency was validated if supported, but passed validation and was not handled. amount:{}, targetCurrency:{}, account:{}", amount, targetCurrency, account);
            throw new IllegalStateException("Unhandled currency case");
        }
    }

}
