package com.techelevator.model;

import com.techelevator.tenmo.model.Transfer;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TransferTests {

    @Test
    public void getAndSetTransferId() {
        Transfer transfer = new Transfer();
        transfer.setTransferId(3008);
        int transferId = transfer.getTransferId();
        Assert.assertEquals(3008, transferId);
    }

    @Test
    public void getAndSetTransferAmount() {
        Transfer transfer = new Transfer();
        transfer.setTransferAmount(new BigDecimal("123"));
        BigDecimal transferAmount = transfer.getTransferAmount();
        Assert.assertEquals(new BigDecimal("123").setScale(2, RoundingMode.HALF_UP), transferAmount.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    public void getAndSetAccountFrom() {
        Transfer transfer = new Transfer();
        transfer.setAccountFrom(2008);
        int accountFrom = transfer.getAccountFrom();
        Assert.assertEquals(2008, accountFrom);
    }

    @Test
    public void getAndSetAccountTo() {
        Transfer transfer = new Transfer();
        transfer.setAccountTo(2012);
        int accountTo = transfer.getAccountTo();
        Assert.assertEquals(2012, accountTo);
    }

    @Test
    public void getAndSetStatus() {
        Transfer transfer = new Transfer();
        transfer.setStatus(3);
        int status = transfer.getStatus();
        Assert.assertEquals(3, status);
    }

}
