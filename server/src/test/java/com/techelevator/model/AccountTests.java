package com.techelevator.model;

import com.techelevator.tenmo.model.Account;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AccountTests {

    @Test
    public void getAndSetAccountId() {
        Account account = new Account();
        account.setAccountId(2008);
        int accountId = account.getAccountId();
        Assert.assertEquals(2008, accountId);
    }

    @Test
    public void getAndSetUsername() {
        Account account = new Account();
        account.setUsername("TEST_USER");
        String username = account.getUsername();
        Assert.assertEquals("TEST_USER", username);
    }

    @Test
    public void getAndSetBalance() {
        Account account = new Account();
        account.setBalance(new BigDecimal("500"));
        BigDecimal balance = account.getBalance();
        Assert.assertEquals(new BigDecimal("500").setScale(2, RoundingMode.HALF_UP), balance.setScale(2, RoundingMode.HALF_UP));
    }
}
