package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

public class JdbcAccountDao implements AccountDao {
    private JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Account getAccountByUsername(String username) {
        Account account = null;
        String sql = "SELECT username, balance FROM account " +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                "WHERE tenmo_user.username = ?;";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, username);

            if (results.next()) {
                account = new Account();
                account.setUsername(results.getString("username"));
                account.setBalance(results.getBigDecimal("balance"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting account");
        }

        return account;
    }

    public int getAccountIdByUsername(String username) {
        int accountId = 0;
        String sql = "SELECT account_id FROM account " +
            "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
            "WHERE username = ?;";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, username);
            if (results.next()) {
                accountId = results.getInt("account_id");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting account ID");
        }

        return accountId;
    }

    public BigDecimal getBalanceByAccountId(int accountId) {
        BigDecimal balance = new BigDecimal(0);
        String sql = "SELECT balance FROM account " +
                "WHERE account_id = ?;";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId);
            if (results.next()) {
                balance = results.getBigDecimal("balance");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting balance");
        }

        return balance;
    }

    public boolean createTransfer(BigDecimal transferAmount, int fromAccountId, int toAccountId){
        String sql = "INSERT INTO transfers (transfer_amount, account_from, account_to, status) " +
                    "VALUES (?, ?, ?, 1);";

        try {
            int rowsInserted = jdbcTemplate.update(sql, transferAmount, fromAccountId, toAccountId);
            if (rowsInserted > 0) {
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating transfer");
        }

        return false;
        }

    public void transfer(BigDecimal fromBalance, BigDecimal toBalance, BigDecimal transferAmount,
                        int fromAccountId, int toAccountId) {
        fromBalance = fromBalance.subtract(transferAmount);
        toBalance = toBalance.add(transferAmount);

        String sqlTransferFrom = "UPDATE account SET balance = ?  WHERE account_id = ?;";

        try {
            jdbcTemplate.update(sqlTransferFrom, fromBalance, fromAccountId);
        } catch (Exception e) {
            throw new RuntimeException("Error subtracting transfer amount");
        }

        String sqlTransferTo = "UPDATE account SET balance = ? WHERE account_id = ?;";

        try {
            jdbcTemplate.update(sqlTransferTo, toBalance, toAccountId);
        } catch (Exception e) {
            throw new RuntimeException("Error adding transfer amount");
        }
    }

    public List<Transfer> getTransfersByUser(String username) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT transfer_id, transfer_amount, account_from, account_to, status FROM transfers " +
                "JOIN account ON (account.account_id = transfers.account_from OR account.account_id = transfers.account_to) " +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                "WHERE username = ?;";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, username);

            while (results.next()) {
                transfers.add(mapRowToTransfer(results));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting transfers");
        }

        return transfers;
    }

    public Transfer getTransferById(int id, String username) {
        Transfer transfer = new Transfer();
        String sql = "SELECT transfer_id, transfer_amount, account_from, account_to, status FROM transfers " +
                "JOIN account ON (account.account_id = transfers.account_from OR account.account_id = transfers.account_to) " +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                "WHERE transfer_id = ? AND username = ?;";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id, username);

            if (results.next()) {
                transfer = mapRowToTransfer(results);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting transfer");
        }

        return transfer;
    }

    public String requestTransfer(int fromAccountId, int toAccountId, BigDecimal transferAmount) {
        String sql = "INSERT INTO transfers (transfer_amount, account_from, account_to, status) " +
                " VALUES (?,?,?,2)";

        try {
            int rowsInserted = jdbcTemplate.update(sql,transferAmount,fromAccountId,toAccountId);
            if (rowsInserted < 1) {
                throw new RuntimeException("Error requesting transfer");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error requesting transfer");
        }

        // #8.v  A Request Transfer has an initial status of Pending.
        // #8.vi  No account balance changes until the request is approved.
        return "Pending";
    }

    public List<Transfer> viewPendingTransfers(String username) {
        List<Transfer> pendingTransfers = new ArrayList<>();
        String sql = "SELECT transfer_id, transfer_amount, account_from, account_to, status FROM transfers " +
                "JOIN account ON (account.account_id = transfers.account_from OR account.account_id = transfers.account_to) " +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                "WHERE status = 2 AND username = ?;";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, username);

            while (results.next()) {
                pendingTransfers.add(mapRowToTransfer(results));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting pending transfers");
        }

        return pendingTransfers;
    }

    public void approve(Transfer transfer) {
        String sql = "UPDATE transfers SET status = 1 " +
                "WHERE transfer_id = ?;";

        try {
            jdbcTemplate.update(sql, transfer.getTransferId());
        } catch (Exception e) {
            throw new RuntimeException("Error approving transfer request");
        }
    }

    public void reject(Transfer transfer) {
        String sql = "UPDATE transfers SET status = 3 " +
                "WHERE transfer_id = ?;";

        try {
            jdbcTemplate.update(sql, transfer.getTransferId());
        } catch (Exception e) {
            throw new RuntimeException("Error rejecting transfer request");
        }
    }

    private Transfer mapRowToTransfer(SqlRowSet rs) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(rs.getInt("transfer_id"));
        transfer.setTransferAmount(rs.getBigDecimal("transfer_amount"));
        transfer.setAccountFrom(rs.getInt("account_from"));
        transfer.setAccountTo(rs.getInt("account_to"));
        transfer.setStatus(rs.getInt("status"));
        return transfer;
    }

}




