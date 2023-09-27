package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.exception.InvalidAmountException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsService {

  @Autowired
  NotificationService notificationService;

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public synchronized void transfer(String originAccountId, String destinationAccountId, BigDecimal amount) throws InvalidAmountException, InsufficientBalanceException {
    Account origin = getAccount(originAccountId);
    Account destination = getAccount(destinationAccountId);

    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new InvalidAmountException("Not possible to transfer negative values");
    }

    if (origin.getBalance().compareTo(amount) < 0) {
      throw new InsufficientBalanceException("Insufficient balance");
    }

    BigDecimal originNewBalance = origin.getBalance().subtract(amount);
    origin.setBalance(originNewBalance);
    BigDecimal destinationNewBalance = destination.getBalance().add(amount);
    destination.setBalance(destinationNewBalance);

    this.accountsRepository.updateAccount(origin);
    this.accountsRepository.updateAccount(destination);
    notificationService.notifyAboutTransfer(origin, destinationAccountId);
    notificationService.notifyAboutTransfer(destination, originAccountId);
  }
}
