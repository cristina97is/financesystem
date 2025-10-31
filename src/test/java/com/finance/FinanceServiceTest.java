package com.finance;

import com.finance.model.Transaction;
import com.finance.model.User;
import com.finance.model.Wallet;
import com.finance.service.FinanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FinanceServiceTest {

    private FinanceService service;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        service = new FinanceService();
        wallet = new Wallet();
    }

    @Test
    void testAddTransactionAndTotals() {
        service.addTransaction(wallet, "Зарплата", 100000, true);
        service.addTransaction(wallet, "Еда", 2000, false);
        service.addTransaction(wallet, "Транспорт", 800, false);

        assertEquals(100000, service.getTotalIncome(wallet), 0.001);
        assertEquals(2800, service.getTotalExpense(wallet), 0.001);
        assertEquals(97200, wallet.getBalance(), 0.001);
    }

    @Test
    void testCategoryExpenseAndRemainingBudget() {
        wallet.addBudget("Еда", 5000.0);
        service.addTransaction(wallet, "Еда", 1200, false);
        service.addTransaction(wallet, "Еда", 3000, false);

        double spent = service.getCategoryExpense(wallet, "Еда");
        assertEquals(4200, spent, 0.001);

        double remaining = service.getRemainingBudget(wallet, "Еда");
        assertEquals(800, remaining, 0.001);
    }

    @Test
    void testGetTotalByCategories() {
        service.addTransaction(wallet, "A", 100, true);
        service.addTransaction(wallet, "B", 200, true);
        service.addTransaction(wallet, "A", 50, false);
        double incomes = service.getTotalByCategories(wallet, Set.of("A","B"), true);
        double expenses = service.getTotalByCategories(wallet, Set.of("A"), false);

        assertEquals(300, incomes, 0.001);
        assertEquals(50, expenses, 0.001);
    }

    @Test
    void testGetTotalByPeriod() {
        // create some transactions with a couple different dates by adding via Transaction constructor indirectly
        service.addTransaction(wallet, "X", 100, true);
        service.addTransaction(wallet, "X", 200, true);
        // Period covering today
        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now().plusDays(1);
        double incomes = service.getTotalByPeriod(wallet, from, to, true);
        assertEquals(300, incomes, 0.001);
    }

    @Test
    void testRenameCategory() {
        service.addTransaction(wallet, "Old", 100, true);
        wallet.addBudget("Old", 500.0);
        service.renameCategory(wallet, "Old", "New");

        assertEquals(1, wallet.getTransactions().stream().filter(t -> t.getCategory().equals("New")).count());
        assertTrue(wallet.getBudgets().containsKey("New"));
        assertFalse(wallet.getBudgets().containsKey("Old"));
    }

    @Test
    void testFormatAmount() {
        String s = service.formatAmount(12345.6);
        assertNotNull(s);
        assertTrue(s.contains("12345") || s.contains("12,345") || s.contains("12 345"));
    }
}
