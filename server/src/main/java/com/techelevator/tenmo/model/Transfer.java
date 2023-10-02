package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Transfer {

    private int transferId;

    private BigDecimal transferAmount;

    private int accountFrom;

    private int accountTo;

    private int status;


    // #5.iii and #8.iv  A transfer includes the username of the recipient and the amount of TE Bucks. (Modified because of #16)
    public Transfer(BigDecimal transferAmount, int accountTo) {
        this.transferAmount = transferAmount;
        this.accountTo = accountTo;
    }

    public Transfer(int transferId, BigDecimal transferAmount, int accountFrom, int accountTo, int status) {
        this.transferId = transferId;
        this.transferAmount = transferAmount;
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.status = status;
    }

    public Transfer() {
    }

    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
    }

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public int getAccountFrom() {
        return accountFrom;
    }

    public void setAccountFrom(int accountFrom) {
        this.accountFrom = accountFrom;
    }

    public int getAccountTo() {
        return accountTo;
    }

    public void setAccountTo(int accountTo) {
        this.accountTo = accountTo;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
