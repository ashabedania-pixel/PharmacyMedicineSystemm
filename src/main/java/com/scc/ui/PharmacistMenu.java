package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class PharmacistMenu {
    private final UserDao userDao;
    private final Scanner sc;

    public PharmacistMenu(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.sc = sc;
    }

    public void show(User user) {
        boolean running = true;
        while (running) {
            System.out.println("\n===== PHARMACIST MENU (" + user.username() + ") =====");
            // TODO: add Pharmacist features here
            System.out.println("0. Logout");
            System.out.print("Select an Option: ");
            if (sc.nextLine().trim().equals("0")) {
                running = false;
            } else {
                System.out.println("Not implemented yet.");
            }
        }
    }
}