package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.exception.InvalidAmountException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.EmailNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

    @MockBean
    EmailNotificationService emailNotificationService;
    @Autowired
    private AccountsService accountsService;

    @Test
    void addAccount() {
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);

        assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
    }

    @Test
    void addAccount_failsOnDuplicateId() {
        String uniqueId = "Id-" + System.currentTimeMillis();
        Account account = new Account(uniqueId);
        this.accountsService.createAccount(account);

        try {
            this.accountsService.createAccount(account);
            fail("Should have failed when adding duplicate account");
        } catch (DuplicateAccountIdException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
        }
    }

    @Test
    void transfer() {
        String originId = createAccount("Id-100", new BigDecimal(1000));
        String destinationId = createAccount("Id-101", new BigDecimal(0));

        BigDecimal amount = new BigDecimal(1000);

        try {
            this.accountsService.transfer(originId, destinationId, amount);
            assertThat(this.accountsService.getAccount(destinationId).getBalance()).isEqualTo(amount);
        } catch (InvalidAmountException e) {
            fail("The amount is valid");
        } catch (InsufficientBalanceException e) {
            fail("The origin account has enough balance");
        }
    }

    @Test
    void transfer_failsInvalidAmount() {
        String originId = createAccount("Id-200", new BigDecimal(1000));
        String destinationId = createAccount("Id-201", new BigDecimal(0));


        try {
            this.accountsService.transfer(originId, destinationId, new BigDecimal(-1000));
            fail("The amount is not valid");
        } catch (InvalidAmountException e) {
            // OK
        } catch (InsufficientBalanceException e) {
            fail("The origin account has enough balance");
        }
    }

    @Test
    void transfer_failsInsufficientBalance() {
        String originId = createAccount("Id-300", new BigDecimal(0));
        String destinationId = createAccount("Id-301", new BigDecimal(0));

        BigDecimal amount = new BigDecimal(1000);

        try {
            this.accountsService.transfer(originId, destinationId, amount);
            fail("The origin account doesn't have enough balance");
        } catch (InvalidAmountException e) {
            fail("The amount is valid");
        } catch (InsufficientBalanceException e) {
            // OK
        }
    }

    private String createAccount(String id, BigDecimal balance) {
        Account account = new Account(id);
        account.setBalance(balance);
        this.accountsService.createAccount(account);
        return account.getAccountId();
    }
}
