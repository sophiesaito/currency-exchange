package com.techelevator.dao;


import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class JdbcAccountDaoTests extends BaseDaoTests {

    private JdbcTemplate jdbcTemplate;
    private JdbcAccountDao sut;

    @Before
    public void setup() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcAccountDao(jdbcTemplate);
    }

    @Test
    public void getAccountByUsername() {
        Account account = sut.getAccountByUsername("TEST_USER1");

        Account expectedAccount = new Account();
        expectedAccount.setUsername("TEST_USER1");
        expectedAccount.setBalance(new BigDecimal("1000"));

        assertAccountsMatch(expectedAccount, account);
    }

    @Test
    public void getAccountIdByUsername() {
        int accountId = sut.getAccountIdByUsername("TEST_USER1");
        Assert.assertEquals(2003, accountId);
    }

    @Test
    public void getBalanceByAccountId() {
        BigDecimal balance = sut.getBalanceByAccountId(2003);
        Assert.assertEquals(new BigDecimal("1000").setScale(2, RoundingMode.HALF_UP), balance.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    public void createTransfer() {
        boolean transferCreated = sut.createTransfer(new BigDecimal("30"),2003,2004);
        Assert.assertTrue(transferCreated);
    }

    @Test
    public void transfer() {
        sut.transfer(new BigDecimal("20"), new BigDecimal("1000"), new BigDecimal("20"),2004,2003);

        Assert.assertEquals(new BigDecimal("0").setScale(2,RoundingMode.HALF_UP), sut.getBalanceByAccountId(2004).setScale(2,RoundingMode.HALF_UP));
        Assert.assertEquals(new BigDecimal("1020").setScale(2,RoundingMode.HALF_UP), sut.getBalanceByAccountId(2003).setScale(2,RoundingMode.HALF_UP));
    }

    @Test
    public void getTransfersByUser() {
        List<Transfer> transfers = sut.getTransfersByUser("TEST_USER1");

        Assert.assertEquals(4,transfers.size());

        assertTransfersMatch(sut.getTransferById(3003, "TEST_USER1"), transfers.get(0));
        assertTransfersMatch(sut.getTransferById(3004, "TEST_USER1"), transfers.get(1));
        assertTransfersMatch(sut.getTransferById(3005, "TEST_USER1"), transfers.get(2));
        assertTransfersMatch(sut.getTransferById(3006, "TEST_USER1"), transfers.get(3));
    }

    @Test
    public void getTransferById() {
        Transfer transfer = sut.getTransferById(3003, "TEST_USER1");

        Transfer expectedTransfer = new Transfer();
        expectedTransfer.setTransferId(3003);
        expectedTransfer.setTransferAmount(new BigDecimal("20"));
        expectedTransfer.setAccountFrom(2004);
        expectedTransfer.setAccountTo(2003);
        expectedTransfer.setStatus(1);

        assertTransfersMatch(expectedTransfer, transfer);
    }

    @Test
    public void requestTransfer() {
        String status = sut.requestTransfer(2003,2004, new BigDecimal("100"));
        Assert.assertEquals("Pending", status);
    }

    @Test
    public void viewPendingTransfers() {
        List<Transfer> pendingTransfers = sut.viewPendingTransfers("TEST_USER1");

        Assert.assertEquals(2, pendingTransfers.size());

        assertTransfersMatch(sut.getTransferById(3005, "TEST_USER1"), pendingTransfers.get(0));
        assertTransfersMatch(sut.getTransferById(3006, "TEST_USER1"), pendingTransfers.get(1));
    }

    @Test
    public void approve() {
        sut.approve(sut.getTransferById(3003, "TEST_USER1"));
        Transfer requestedTransfer = sut.getTransferById(3003, "TEST_USER1");
        Assert.assertEquals(1, requestedTransfer.getStatus());
    }

    @Test
    public void reject() {
        sut.reject(sut.getTransferById(3003, "TEST_USER1"));
        Transfer requestedTransfer = sut.getTransferById(3003, "TEST_USER1");
        Assert.assertEquals(3, requestedTransfer.getStatus());
    }

    private void assertAccountsMatch(Account expected, Account actual) {
        Assert.assertEquals(expected.getAccountId(), actual.getAccountId());
        Assert.assertEquals(expected.getUsername(), actual.getUsername());
        Assert.assertEquals(expected.getBalance().setScale(2, RoundingMode.HALF_UP), actual.getBalance().setScale(2, RoundingMode.HALF_UP));
    }

    private void assertTransfersMatch(Transfer expected, Transfer actual) {
        Assert.assertEquals(expected.getTransferId(), actual.getTransferId());
        Assert.assertEquals(expected.getAccountTo(), actual.getAccountTo());
        Assert.assertEquals(expected.getAccountFrom(), actual.getAccountFrom());
        Assert.assertEquals(expected.getStatus(), actual.getStatus());
        Assert.assertEquals(expected.getTransferAmount().setScale(2, RoundingMode.HALF_UP), actual.getTransferAmount().setScale(2, RoundingMode.HALF_UP));
    }

}
