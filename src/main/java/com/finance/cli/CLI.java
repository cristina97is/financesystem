package com.finance.cli; /** говорит, что этот файл находится в пакете cli внутри проекта finance. пакет используется для организации кода, чтобы не было конфликта имён классов */

/** подключаем классы из других частей проекта для использования нами **/
import com.finance.model.User; /** данные о пользователях, логины, пароли, кошельки **/
import com.finance.model.Wallet; /** сам кошелек, доходы, расходы, бюджеты **/
import com.finance.service.FinanceService; /** сервис для работы с финансами, подсчёты, транзакции **/
import com.finance.util.ExportImport; /** импорт экспорт данных**/

import java.io.*; /** сохраняем и загружаем пользователей**/
import java.time.LocalDate; /** для работы с датами **/
import java.util.*; /** для работы  с коллекциями map, string, list, set, scanner(класс для чтения ввода) **/
import java.util.stream.Collectors; /** для работы с потоками данных, фильтрация и объединение **/

public class CLI { /** отвечает за интерфейс командной строки **/
    private Map<String, User> users = new HashMap<>(); /** создаем коллекцию всех пользователей,
                                                       где stringключ -логин пользователя, user -объект с данными
                                                       пользователя, hashmap позволяет нам быстро искать по логину**/

    private FinanceService service = new FinanceService(); /** создаем сервис для работы с финансами **/
    private User currentUser = null; /** переменная для текущего вошедшего пользователя, если никто--null **/

    public CLI() { /**  отвечает за интефейс командной строки **/
        loadUsers(); /**  загружаем всех пользователей из файла users.dat **/
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nВведите команду (help для списка):");
            String command = scanner.nextLine().trim();

            // commands with arguments
            if (command.startsWith("show category")) {
                String[] parts = command.split("\\s+", 3);
                if (parts.length < 3) {
                    System.out.println("Укажите категории: show category Еда Транспорт");
                } else {
                    handleShowCategory(parts[2]);
                }
                continue;
            }
            if (command.startsWith("show period")) {
                String[] parts = command.split("\\s+");
                if (parts.length != 4) {
                    System.out.println("Формат: show period YYYY-MM-DD YYYY-MM-DD");
                } else {
                    handleShowPeriod(parts[2], parts[3]);
                }
                continue;
            }
            if (command.startsWith("export csv") || command.startsWith("export json")
                    || command.startsWith("import csv") || command.startsWith("import json")) {
                handleExportImport(command);
                continue;
            }

            switch (command) {
                case "help":
                    showHelp();
                    break;
                case "register":
                    registerUser(scanner);
                    break;
                case "login":
                    loginUser(scanner);
                    break;
                case "add_income":
                    addTransaction(scanner, true);
                    break;
                case "add_expense":
                    addTransaction(scanner, false);
                    break;
                case "add_budget":
                    addBudget(scanner);
                    break;
                case "edit_budget":
                    editBudget(scanner);
                    break;
                case "rename_category":
                    renameCategory(scanner);
                    break;
                case "show":
                    showStats();
                    break;
                case "transfer":
                    transferMoney(scanner);
                    break;
                case "exit":
                    System.out.println("Выход из программы");
                    saveUsers();
                    return;
                default:
                    System.out.println("Неизвестная команда, используйте help");
            }
        }
    }

    private void showHelp() {
        System.out.println("Список команд:");
        System.out.println("register           - Зарегистрировать нового пользователя");
        System.out.println("login              - Войти в систему");
        System.out.println("add_income         - Добавить доход");
        System.out.println("add_expense        - Добавить расход");
        System.out.println("add_budget         - Установить бюджет на категорию расходов");
        System.out.println("edit_budget        - Изменить бюджет категории");
        System.out.println("rename_category    - Переименовать категорию (транзакции и бюджет)");
        System.out.println("show               - Показать общий доход, расходы, баланс и бюджеты");
        System.out.println("show category      - Показать статистику по одной или нескольким категориям");
        System.out.println("show period        - Показать статистику за период (yyyy-MM-dd yyyy-MM-dd)");
        System.out.println("export csv/json    - Экспортировать данные текущего пользователя");
        System.out.println("import csv/json    - Импортировать данные в текущий аккаунт");
        System.out.println("transfer           - Перевести деньги другому пользователю");
        System.out.println("exit               - Выйти из программы");
        System.out.println("help               - Показать эту справку");
        System.out.println("Примеры:");
        System.out.println("  show category Еда Транспорт");
        System.out.println("  show period 2025-10-01 2025-10-31");
        System.out.println("  export csv user.csv");
    }

    private void registerUser(Scanner scanner) {
        System.out.print("Логин: ");
        String login = scanner.nextLine().trim();
        System.out.print("Пароль: ");
        String password = scanner.nextLine().trim();

        if (login.isEmpty() || password.isEmpty()) {
            System.out.println("Логин и пароль не могут быть пустыми!");
            return;
        }
        if (users.containsKey(login)) {
            System.out.println("Пользователь уже существует!");
            return;
        }
        users.put(login, new User(login, password));
        saveUsers();
        System.out.println("Пользователь зарегистрирован!");
    }

    private void loginUser(Scanner scanner) {
        System.out.print("Логин: ");
        String login = scanner.nextLine().trim();
        System.out.print("Пароль: ");
        String password = scanner.nextLine().trim();

        User user = users.get(login);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            System.out.println("Вы вошли в систему!");
        } else {
            System.out.println("Неверный логин/пароль");
        }
    }

    private void addTransaction(Scanner scanner, boolean isIncome) {
        if (currentUser == null) {
            System.out.println("Сначала войдите в систему!");
            return;
        }

        System.out.print("Категория: ");
        String category = scanner.nextLine().trim();
        if (category.isEmpty()) { System.out.println("Категория не может быть пустой!"); return; }

        System.out.print("Сумма: ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine());
            if (amount <= 0) { System.out.println("Сумма должна быть больше 0!"); return; }
        } catch (NumberFormatException e) {
            System.out.println("Некорректная сумма!");
            return;
        }

        service.addTransaction(currentUser.getWallet(), category, amount, isIncome);
        saveUsers();
        System.out.println((isIncome ? "Доход" : "Расход") + " добавлен: " + service.formatAmount(amount) + " в категории " + category);
    }

    private void addBudget(Scanner scanner) {
        if (currentUser == null) { System.out.println("Сначала войдите в систему!"); return; }

        System.out.print("Категория: ");
        String category = scanner.nextLine().trim();
        if (category.isEmpty()) { System.out.println("Категория не может быть пустой!"); return; }

        System.out.print("Сумма бюджета: ");
        double budget;
        try {
            budget = Double.parseDouble(scanner.nextLine());
            if (budget <= 0) { System.out.println("Бюджет должен быть больше 0!"); return; }
        } catch (NumberFormatException e) {
            System.out.println("Некорректная сумма бюджета!");
            return;
        }

        currentUser.getWallet().addBudget(category, budget);
        saveUsers();
        System.out.println("Бюджет установлен: " + service.formatAmount(budget) + " для категории " + category);
    }

    private void editBudget(Scanner scanner) {
        if (currentUser == null) { System.out.println("Сначала войдите в систему!"); return; }
        System.out.print("Категория: ");
        String cat = scanner.nextLine().trim();
        if (!currentUser.getWallet().getBudgets().containsKey(cat)) { System.out.println("Категория не найдена."); return; }
        System.out.print("Новая сумма бюджета: ");
        try {
            double nb = Double.parseDouble(scanner.nextLine());
            if (nb <= 0) { System.out.println("Сумма должна быть больше 0"); return; }
            currentUser.getWallet().getBudgets().put(cat, nb);
            saveUsers();
            System.out.println("Бюджет обновлён: " + service.formatAmount(nb));
        } catch (NumberFormatException e) {
            System.out.println("Некорректная сумма");
        }
    }

    private void renameCategory(Scanner scanner) {
        if (currentUser == null) { System.out.println("Сначала войдите в систему!"); return; }
        System.out.print("Старая категория: ");
        String oldCat = scanner.nextLine().trim();
        System.out.print("Новая категория: ");
        String newCat = scanner.nextLine().trim();
        if (oldCat.isEmpty() || newCat.isEmpty()) { System.out.println("Категории не могут быть пустыми"); return; }
        service.renameCategory(currentUser.getWallet(), oldCat, newCat);
        saveUsers();
        System.out.println("Категория переименована: " + oldCat + " -> " + newCat);
    }

    private void showStats() {
        if (currentUser == null) { System.out.println("Сначала войдите в систему!"); return; }
        Wallet wallet = currentUser.getWallet();
        System.out.println("\n=== Статистика для пользователя: " + currentUser.getLogin() + " ===");
        System.out.println("Общий доход: " + service.formatAmount(service.getTotalIncome(wallet)));
        System.out.println("Общие расходы: " + service.formatAmount(service.getTotalExpense(wallet)));
        System.out.println("Баланс: " + service.formatAmount(wallet.getBalance()));
        System.out.println("Бюджеты по категориям:");
        for (String cat : wallet.getBudgets().keySet()) {
            double remaining = service.getRemainingBudget(wallet, cat);
            System.out.println("  " + cat + ": " +
                    service.formatAmount(wallet.getBudgets().get(cat)) +
                    ", остаток: " + service.formatAmount(remaining));
        }
        System.out.println("====================================\n");
    }

    private void transferMoney(Scanner scanner) {
        if (currentUser == null) { System.out.println("Сначала войдите в систему!"); return; }
        System.out.print("Логин получателя: ");
        String recipientLogin = scanner.nextLine().trim();
        User recipient = users.get(recipientLogin);
        if (recipient == null) { System.out.println("Пользователь не найден!"); return; }

        System.out.print("Сумма перевода: ");
        double transferAmount;
        try {
            transferAmount = Double.parseDouble(scanner.nextLine());
            if (transferAmount <= 0) { System.out.println("Сумма перевода должна быть больше 0!"); return; }
        } catch (NumberFormatException e) {
            System.out.println("Некорректная сумма перевода!"); return;
        }

        Wallet senderWallet = currentUser.getWallet();
        if (senderWallet.getBalance() < transferAmount) { System.out.println("Недостаточно средств!"); return; }

        service.addTransaction(senderWallet, "Перевод to " + recipientLogin, transferAmount, false);
        service.addTransaction(recipient.getWallet(), "Перевод from " + currentUser.getLogin(), transferAmount, true);
        saveUsers();
        System.out.println("Перевод выполнен: " + service.formatAmount(transferAmount));
    }

    private void handleShowCategory(String catsLine) {
        if (currentUser == null) { System.out.println("Сначала войдите!"); return; }
        String[] cats = catsLine.split("\\s+");
        Set<String> set = Arrays.stream(cats).collect(Collectors.toSet());
        double incomes = service.getTotalByCategories(currentUser.getWallet(), set, true);
        double expenses = service.getTotalByCategories(currentUser.getWallet(), set, false);
        System.out.println("Доходы по выбранным категориям: " + service.formatAmount(incomes));
        System.out.println("Расходы по выбранным категориям: " + service.formatAmount(expenses));
    }

    private void handleShowPeriod(String from, String to) {
        if (currentUser == null) { System.out.println("Сначала войдите!"); return; }
        try {
            LocalDate fromDate = LocalDate.parse(from);
            LocalDate toDate = LocalDate.parse(to);
            double incomes = service.getTotalByPeriod(currentUser.getWallet(), fromDate, toDate, true);
            double expenses = service.getTotalByPeriod(currentUser.getWallet(), fromDate, toDate, false);
            System.out.println("Период: " + from + " — " + to);
            System.out.println("Доходы: " + service.formatAmount(incomes));
            System.out.println("Расходы: " + service.formatAmount(expenses));
        } catch (Exception e) {
            System.out.println("Ошибка разбора дат. Формат YYYY-MM-DD");
        }
    }

    private void handleExportImport(String command) {
        if (currentUser == null) { System.out.println("Сначала войдите!"); return; }
        String[] parts = command.split("\\s+");
        if (parts.length < 3) {
            System.out.println("Укажите файл: export/import csv filename.csv");
            return;
        }
        String action = parts[0];
        String type = parts[1];
        String path = parts[2];
        try {
            if (action.equals("export") && type.equals("csv")) {
                ExportImport.exportCsv(currentUser, path);
                System.out.println("Экспортировано в " + path);
            } else if (action.equals("import") && type.equals("csv")) {
                ExportImport.importCsv(currentUser, path);
                saveUsers();
                System.out.println("Импортировано из " + path);
            } else {
                System.out.println("Только csv поддерживается сейчас: export csv <file>, import csv <file>");
            }
        } catch (Exception e) {
            System.out.println("Ошибка при экспорте/импорте: " + e.getMessage());
        }
    }

    private void saveUsers() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("users.dat"))) {
            out.writeObject(users);
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении данных: " + e.getMessage());
        }
    }

    private void loadUsers() {
        File file = new File("users.dat");
        if (file.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                users = (Map<String, User>) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Ошибка при загрузке данных: " + e.getMessage());
            }
        }
    }
}
