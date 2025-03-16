package org.pn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AccountServiceExchangeCurrencyTest {


    @Mock
    private AccountRepository accountRepository;

    @Mock
    private NbpExchangeRateService nbpExchangeRateService;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExchangeCurrencyToPLN() {
        Account account = new Account(1L, "Jas", "Fasola", 1000.0, 100.0);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(nbpExchangeRateService.getUSDExchangeRate()).thenReturn(4.0);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account updatedAccount = accountService.exchangeCurrency(1L, 400.0, Currency.PLN);

        assertNotNull(updatedAccount);
        assertEquals(1400.0, updatedAccount.getBalancePLN());
        assertEquals(0.0, updatedAccount.getBalanceUSD());
    }

    @Test
    void testExchangeCurrencyToUSD() {
        Account account = new Account(1L, "Jas", "Fasola", 1000.0, 100.0);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(nbpExchangeRateService.getUSDExchangeRate()).thenReturn(4.0);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account updatedAccount = accountService.exchangeCurrency(1L, 100.0, Currency.USD);

        assertNotNull(updatedAccount);
        assertEquals(600.0, updatedAccount.getBalancePLN());
        assertEquals(200.0, updatedAccount.getBalanceUSD());
    }

    @Test
    void testExchangeCurrencyAccountNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.exchangeCurrency(1L, 100.0, Currency.USD);
        });

        assertEquals("Account not found", exception.getMessage());
    }

    @Test
    void testExchangeCurrencyNotEnoughUSD() {
        Account account = new Account(1L, "Jas", "Fasola", 1000.0, 50.0);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(nbpExchangeRateService.getUSDExchangeRate()).thenReturn(4.0);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.exchangeCurrency(1L, 400.0, Currency.PLN);
        });

        assertEquals("Not enough USD to exchange", exception.getMessage());
    }

    @Test
    void testExchangeCurrencyNotEnoughPLN() {
        Account account = new Account(1L, "Jas", "Fasola", 100.0, 100.0);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(nbpExchangeRateService.getUSDExchangeRate()).thenReturn(4.0);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.exchangeCurrency(1L, 100.0, Currency.USD);
        });

        assertEquals("Not enough PLN to exchange", exception.getMessage());
    }
}