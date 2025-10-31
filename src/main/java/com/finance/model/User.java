/**
 * Пользователь
 */
package com.finance.model;

import java.io.Serializable;
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String login;
    private final String password;
    private final Wallet wallet;

    public User(String login, String password) {
        if (login == null || login.isBlank()) throw new IllegalArgumentException("Логин не может быть пустым");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Пароль не может быть пустым");
        this.login = login;
        this.password = password;
        this.wallet = new Wallet();
    }

    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public Wallet getWallet() { return wallet; }
}
