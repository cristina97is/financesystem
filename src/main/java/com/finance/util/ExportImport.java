package com.finance.util;

import com.finance.model.Transaction;
import com.finance.model.User;

import java.io.*;
import java.util.Map;

public class ExportImport {

    public static void exportCsv(User user, String filePath) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(filePath))) {
            out.println("dateTime,category,amount,isIncome");
            for (Transaction t : user.getWallet().getTransactions()) {
                out.printf("%s,%s,%.2f,%b%n", t.getDateTime(), escapeCsv(t.getCategory()), t.getAmount(), t.isIncome());
            }
        }
    }

    public static void importCsv(User user, String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 4);
                if (parts.length < 4) continue;
                String category = unescapeCsv(parts[1]);
                double amount = Double.parseDouble(parts[2]);
                boolean isIncome = Boolean.parseBoolean(parts[3]);
                user.getWallet().getTransactions().add(new Transaction(category, amount, isIncome));
            }
        }
    }

    private static String escapeCsv(String s) {
        // Заменяем " на ""
        return s.replace("\"", "\"\"");
    }

    private static String unescapeCsv(String s) {
        return s;
    }

    public static void exportAllSerialized(Map<String, User> users, String path) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path))) {
            out.writeObject(users);
        }
    }

    public static void importAllSerialized(Map<String, User> users, String path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path))) {
            Map<String, User> loaded = (Map<String, User>) in.readObject();
            users.putAll(loaded);
        }
    }
}
