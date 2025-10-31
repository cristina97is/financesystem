/**
 * Транзакция: категория, сумма, тип и дата.
 */
package com.finance.model; /** этот класс находится в папке com/finance/model ,

                           это помогает организовать код, чтобы классы с разным функционалом
                           были в разных коробках**/

/**подключаем внешние классы**/
import java.io.Serializable; /**сохраняет объект в файл**/
import java.time.LocalDateTime; /** дата и время создания**/
import java.time.format.DateTimeFormatter; /**форматирование в читаемый вид**/
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String category;
    private final double amount;
    private final boolean isIncome;
    private final LocalDateTime dateTime;

    public Transaction(String category, double amount, boolean isIncome) {
        if (category == null || category.isBlank()) throw new IllegalArgumentException("Категория не может быть пустой");
        if (amount < 0) throw new IllegalArgumentException("Сумма не может быть отрицательной");

        this.category = category;
        this.amount = amount;
        this.isIncome = isIncome;
        this.dateTime = LocalDateTime.now();
    }

    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public boolean isIncome() { return isIncome; }
    public LocalDateTime getDateTime() { return dateTime; }

    @Override
    public String toString() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String type = isIncome ? "Доход" : "Расход";
        return "[" + dateTime.format(f) + "] " + type + ": " + amount + " (" + category + ")";
    }
}
