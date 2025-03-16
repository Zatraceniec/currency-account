package org.pn;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pn.dto.CreateAccountDTO;
import org.pn.dto.ExchangeCurrenctyDTO;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create")
    public Account createAccount(@RequestBody CreateAccountDTO createAccountDTO) {
        validateMandatoryParameters(createAccountDTO);
        Account account = Account.builder()
                .firstName(createAccountDTO.firstName())
                .lastName(createAccountDTO.lastName())
                .balancePLN(createAccountDTO.initialBalancePLN())
                .balanceUSD(0)
                .build();
        return accountService.createAccount(account);
    }

    private void validateMandatoryParameters(CreateAccountDTO createAccountDTO) {
        validateMandatoryParameter(createAccountDTO.firstName(), "firstName");
        validateMandatoryParameter(createAccountDTO.lastName(), "lastName");
        if(createAccountDTO.initialBalancePLN() == null) {
            log.error("Missing initialBalancePLN parameter. createAccountDTO:{}", createAccountDTO);
            throw new RuntimeException("Missing initialBalancePLN parameter");
        }
    }

    private void validateMandatoryParameter(String parameter, String parameterName) {
        if(StringUtils.isEmpty(parameter)) {
            log.error("Missing mandatory parameter: {}", parameterName);
            throw new RuntimeException("Missing mandatory parameter: " + parameterName);
        }
    }

    @GetMapping("/{id}")
    public Optional<Account> getAccountById(@PathVariable("id") Long id) {
        return accountService.getAccountById(id);
    }

    @PostMapping("/{id}/exchange")
    public Account exchangeCurrency(@PathVariable("id") Long id, @RequestBody ExchangeCurrenctyDTO exchangeCurrenctyDTO) {
        if (!id.equals(exchangeCurrenctyDTO.id())) {
            log.error("Id from request body does not match the id from path. id:{}, exchangeCurrenctyDTO:{}", id, exchangeCurrenctyDTO);
            throw new RuntimeException("Id from request body does not match the id from path");
        }
        return accountService.exchangeCurrency(id, exchangeCurrenctyDTO.amount(), exchangeCurrenctyDTO.targetCurrency());
    }

}