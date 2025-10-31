/**
 * Кошелёк пользователя
 */
package com.finance.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wallet implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<String, Double> budgets = new HashMap<>();

    public List<Transaction> getTransactions() { return transactions; }
    public Map<String, Double> getBudgets() { return budgets; }

    public void addBudget(String category, double amount) {
        budgets.put(category, amount);
    }

    public double getBalance() {
        double income = transactions.stream()
                .filter(Transaction::isIncome)
                .mapToDouble(Transaction::getAmount)
                .sum();
        double expense = transactions.stream()
                .filter(t -> !t.isIncome())
                .mapToDouble(Transaction::getAmount)
                .sum();
        return income - expense;
    }
}
