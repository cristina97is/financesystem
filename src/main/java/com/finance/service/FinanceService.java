/**
 * Сервис для работы с финансами: добавление транзакций, подсчёт, фильтры, уведомления.
 */
package com.finance.service;

import com.finance.model.Transaction;
import com.finance.model.Wallet;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class FinanceService {
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    public void addTransaction(Wallet wallet, String category, double amount, boolean isIncome) {
        Transaction transaction = new Transaction(category, amount, isIncome);
        wallet.getTransactions().add(transaction);

        if (!isIncome && wallet.getBudgets().containsKey(category)) {
            double budget = wallet.getBudgets().get(category);
            double spent = getCategoryExpense(wallet, category);

            if (spent > budget) {
                double over = spent - budget;
                System.out.println("Категория \"" + category + "\" превышена на " + formatAmount(over) + "!");
            } else if (spent >= 0.8 * budget) {
                System.out.println("Внимание! Вы достигли 80% бюджета по категории \"" + category + "\" (" + formatAmount(spent) + " из " + formatAmount(budget) + ")");
            }
        }

        if (wallet.getBalance() < 0) {
            System.out.println("Внимание! Общий баланс отрицательный!");
        }
    }

    public double getTotalIncome(Wallet wallet) {
        return wallet.getTransactions().stream()
                .filter(Transaction::isIncome)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpense(Wallet wallet) {
        return wallet.getTransactions().stream()
                .filter(t -> !t.isIncome())
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getCategoryExpense(Wallet wallet, String category) {
        return wallet.getTransactions().stream()
                .filter(t -> !t.isIncome() && t.getCategory().equals(category))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getRemainingBudget(Wallet wallet, String category) {
        double budget = wallet.getBudgets().getOrDefault(category, 0.0);
        double spent = getCategoryExpense(wallet, category);
        return budget - spent;
    }

    public double getTotalByCategories(Wallet wallet, Set<String> categories, boolean incomes) {
        return wallet.getTransactions().stream()
                .filter(t -> (t.isIncome() == incomes) && categories.contains(t.getCategory()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalByPeriod(Wallet wallet, LocalDate fromDateInclusive, LocalDate toDateInclusive, boolean incomes) {
        LocalDateTime from = fromDateInclusive.atStartOfDay();
        LocalDateTime to = toDateInclusive.atTime(LocalTime.MAX);
        return wallet.getTransactions().stream()
                .filter(t -> t.isIncome() == incomes)
                .filter(t -> !t.getDateTime().isBefore(from) && !t.getDateTime().isAfter(to))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public List<String> listCategories(Wallet wallet) {
        return wallet.getTransactions().stream().map(Transaction::getCategory).distinct().collect(Collectors.toList());
    }

    public void renameCategory(Wallet wallet, String oldCat, String newCat) {
        if (oldCat.equals(newCat)) return;
        if (wallet.getBudgets().containsKey(oldCat)) {
            Double b = wallet.getBudgets().remove(oldCat);
            wallet.getBudgets().put(newCat, b);
        }
        List<Transaction> updated = wallet.getTransactions().stream()
                .map(t -> t.getCategory().equals(oldCat) ? new Transaction(newCat, t.getAmount(), t.isIncome()) : t)
                .collect(Collectors.toList());
        wallet.getTransactions().clear();
        wallet.getTransactions().addAll(updated);
    }

    public String formatAmount(double amount) {
        return df.format(amount) + " ₽";
    }
}
