package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.awt.font.TransformAttribute;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
public class AppController {

    private AccountDao accountDao;
    private UserDao userDao;

    // #4  As an authenticated user of the system, I need to be able to see my Account Balance.
    @RequestMapping(path = "/accountbalance", method = RequestMethod.GET)
    public Account getAccountBalance(Principal principal) {
        return accountDao.getAccountByUsername(principal.getName());
    }

    // #5.i  I need an endpoint that shows the users I can send money to.
    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> findAll() {
        return userDao.findAll();
    }

    // #5  As an authenticated user of the system, I need to be able to send a transfer of a specific amount of TE Bucks to a registered user.
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/sendtransfer", method = RequestMethod.POST)
    public String sendTransfer(@RequestBody Transfer transfer, Principal principal) {
        BigDecimal transferAmount = transfer.getTransferAmount();
        int accountFromId = accountDao.getAccountIdByUsername(principal.getName());
        int accountToId = transfer.getAccountTo();
        BigDecimal fromBalance = accountDao.getBalanceByAccountId(accountFromId);
        BigDecimal toBalance = accountDao.getBalanceByAccountId(accountToId);

        // #5.ii  I must not be allowed to send money to myself.
        if (accountFromId == accountToId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't transfer money to yourself");
        }

        // #5.vi  I can't send more TE Bucks than I have in my account.
        if (transferAmount.compareTo(fromBalance) == 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't transfer more money than you have");
        }

        // #5.vii  I can't send a zero or negative amount.
        if (transferAmount.compareTo(new BigDecimal("0")) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't send a zero or negative amount");
        }

        // #5.iv  The receiver's account balance is increased by the amount of the transfer.
        // #5.v   The sender's account balance is decreased by the amount of the transfer.
        accountDao.transfer(fromBalance, toBalance, transferAmount, accountFromId, accountToId);
        accountDao.createTransfer(transferAmount, accountFromId, accountToId);

        // #5.viii  A Sending Transfer has an initial status of Approved.
        return "Approved";
    }

    // #6  As an authenticated user of the system, I need to be able to see transfers I have sent or received.
    // #8.vii  The transfer request should appear in both users' list of transfers
    @RequestMapping(path = "/transfers", method = RequestMethod.GET)
    public List<Transfer> viewTransfers(Principal principal) {
        return accountDao.getTransfersByUser(principal.getName());
    }

    // #7  As an authenticated user of the system, I need to be able to retrieve the details of any transfer based upon the transfer ID.
    @RequestMapping(path = "/transfers/{id}", method = RequestMethod.GET)
    public Transfer getTransferById(@RequestParam int id, Principal principal) {
        return accountDao.getTransferById(id, principal.getName());
    }

    // #8  As an authenticated user of the system, I need to be able to request a transfer of a specific amount of TE Bucks from another registered user.
    @RequestMapping(path = "/requesttransfer", method = RequestMethod.POST)
    public String requestTransfer(@RequestBody Transfer transfer, Principal principal) {
        int accountFromId = accountDao.getAccountIdByUsername(principal.getName());
        int accountToId = transfer.getAccountTo();
        BigDecimal transferAmount = transfer.getTransferAmount();

        // #8.ii  I must not be allowed to request money from myself.
        if (accountFromId == accountToId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't request money from yourself");
        }

        // #8.iii  I can't request a zero or negative amount.
        if (transferAmount.compareTo(new BigDecimal("0")) <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot request a zero or negative amount");
        }

        // #8.i  I should be able to choose from a list of users to request TE Bucks from.
        List<User> userList = userDao.findAll();
        boolean userFound = false;
        for(User user: userList){
            if(user.getId() == transfer.getAccountTo()){
                userFound = true;
            }
        }
        if (!userFound) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
        }
        return accountDao.requestTransfer(accountFromId,accountToId,transferAmount);
    }

    // #9  As an authenticated user of the system, I need to be able to see my Pending transfers.
    @RequestMapping(path = "/pendingtransfers", method = RequestMethod.GET)
    public List<Transfer> viewPendingTransfers(Principal principal) {
        return accountDao.viewPendingTransfers(principal.getName());
    }

    // #10  As an authenticated user of the system, I need to be able to either approve or reject a Request Transfer.
    @RequestMapping(path = "/approvetransfers", method = RequestMethod.PUT)
    public void approveTransfers(@RequestBody String statusUpdate, Principal principal, Transfer transfer) {
        BigDecimal transferAmount = transfer.getTransferAmount();
        int accountFromId = accountDao.getAccountIdByUsername(principal.getName());
        int accountToId = transfer.getAccountTo();
        BigDecimal fromBalance = accountDao.getBalanceByAccountId(accountFromId);
        BigDecimal toBalance = accountDao.getBalanceByAccountId(accountToId);

        // #11  I can't "approve" a given Request Transfer for more TE Bucks than I have in my account.
        if (transferAmount.compareTo(fromBalance) == 1) {
            statusUpdate = "Rejected";
        }

        // #12  The Request Transfer status is Approved if I approve, or Rejected if I reject the request.
        if (statusUpdate.equalsIgnoreCase("Approved")) {
            accountDao.approve(transfer);
            // #13  If the transfer is approved, the requester's account balance is increased by the amount of the request.
            // #14  If the transfer is approved, the requestee's account balance is decreased by the amount of the request.
            accountDao.transfer(fromBalance, toBalance, transferAmount, accountFromId, accountToId);
        }
        else {
            // #15  If the transfer is rejected, no account balance changes.
            accountDao.reject(transfer);
        }

    }




}
