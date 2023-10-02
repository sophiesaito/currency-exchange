package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.math.BigDecimal;
import java.util.List;

public interface AccountDao {
    Account getAccountByUsername(String username);
    int getAccountIdByUsername(String username);
    BigDecimal getBalanceByAccountId(int accountId);
    boolean createTransfer(BigDecimal transferAmount, int fromAccountId, int toAccountId);
    void transfer(BigDecimal fromBalance, BigDecimal toBalance, BigDecimal transferAmount,
                  int fromAccountId, int toAccountId);
    List<Transfer> getTransfersByUser(String username);
    Transfer getTransferById(int id, String username);
    String requestTransfer(int fromAccountId, int toAccountId, BigDecimal transferAmount);
    List<Transfer> viewPendingTransfers(String username);
    void approve(Transfer transfer);
    void reject(Transfer transfer);

}
