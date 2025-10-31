package com.finance;

import com.finance.model.User;
import com.finance.model.Wallet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserWalletTest {

    @Test
    void userHasWallet() {
        User u = new User("test","p");
        Wallet w = u.getWallet();
        assertNotNull(w);
        assertEquals(0, w.getTransactions().size());
    }

    @Test
    void walletBudgetOperations() {
        User u = new User("t2","p2");
        Wallet w = u.getWallet();
        w.addBudget("A", 1000.0);
        assertTrue(w.getBudgets().containsKey("A"));
        assertEquals(1000.0, w.getBudgets().get("A"));
    }
}
