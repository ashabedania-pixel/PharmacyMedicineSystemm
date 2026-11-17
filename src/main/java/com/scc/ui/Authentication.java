package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {
    private final UserDao userDao;
    private final Scanner sc;

    public Authentication(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.sc = sc;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("================================");
            System.out.println(" PHARMACY MEDICINE SALES SYSTEM ");
            System.out.println("================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Select an Option: ");

            switch (sc.nextLine().trim()) {
                case "1" -> login();
                case "2" -> register();
                case "3" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }

    private void register() {
        System.out.print("Enter new Username: ");
        String user = sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String pass = sc.nextLine();

        if (user.isEmpty() || pass.isEmpty()) {
            System.out.println("Username and password cannot be empty!");
            return;
        }

        System.out.print("Select Role [1] Admin [2] Pharmacist [3] Cashier : ");
        String role = switch (sc.nextLine().trim()) {
            case "1" -> "ADMIN";
            case "2" -> "PHARMACIST";
            case "3" -> "CASHIER";
            default -> null;
        };

        if (role == null) {
            System.out.println("Invalid Role Selection!");
            return;
        }
        userDao.registerUser(user, pass, role);
    }

    private void login() {
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine();

        User user = userDao.login(username, password);
        if (user == null) {
            System.out.println("Invalid username or password!");
            return;
        }
        if (!"APPROVED".equalsIgnoreCase(user.status())) {
            System.out.println("Account is " + user.status() + ". Please wait for admin approval.");
            return;
        }
        routeByRole(user);
    }

    private void routeByRole(User user) {
        switch (user.role()) {
            case "ADMIN"      -> new AdminMenu(userDao, sc).show(user);
            case "PHARMACIST" -> new PharmacistMenu(userDao, sc).show(user);
            case "CASHIER"    -> new CashierMenu(userDao, sc).show(user);
            default -> System.out.println("Unknown role: " + user.role());
        }
    }
}