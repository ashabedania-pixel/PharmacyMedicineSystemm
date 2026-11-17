package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner sc;

    private static final Set<String> VALID_ROLES = Set.of("ADMIN", "PHARMACIST", "CASHIER");
    private static final Set<String> VALID_STATUSES = Set.of("APPROVED", "PENDING");

    public AdminMenu(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.sc = sc;
    }

    public void show(User admin) {
        boolean running = true;
        while (running) {
            System.out.println("\n===== ADMIN MENU (" + admin.username() + ") =====");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role and Status");
            System.out.println("0. Logout");
            System.out.print("Select an Option: ");

            switch (sc.nextLine().trim()) {
                case "1" -> printUserTable("ALL SYSTEM USERS", userDao.getAllUsers());
                case "2" -> printUserTable("PENDING REGISTRATIONS", userDao.getPendingUsers());
                case "3" -> approveUser();
                case "4" -> updateUser(admin);
                case "0" -> running = false;
                default -> System.out.println("Invalid option!");
            }
        }
    }

    private void approveUser() {
        List<User> pending = userDao.getPendingUsers();
        printUserTable("PENDING REGISTRATIONS", pending);
        if (pending.isEmpty()) {
            return;
        }

        while (true) {
            System.out.print("Enter Target User ID to approve (0 to cancel): ");
            String input = sc.nextLine().trim();

            int id;
            try {
                id = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
                continue;
            }

            if (id == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            boolean inPendingList = pending.stream().anyMatch(u -> u.id() == id);
            if (!inPendingList) {
                System.out.println("ID " + id + " is not in the pending list. Try again.");
                continue;
            }

            if (userDao.approveUser(id)) {
                System.out.println("User ID " + id + " has been APPROVED.");
            } else {
                System.out.println("Approval failed.");
            }
            return;
        }
    }

    /** Lets an admin change another user's role and status. Enter 0 at any prompt to cancel. */
    private void updateUser(User currentAdmin) {
        List<User> users = userDao.getAllUsers();
        printUserTable("ALL SYSTEM USERS", users);
        if (users.isEmpty()) {
            return;
        }

        int targetUserId;
        while (true) {
            targetUserId = readIntInput("Enter Target User ID to update (0 to cancel): ");

            if (targetUserId == 0) {
                System.out.println("Update cancelled.");
                return;
            }
            if (targetUserId == currentAdmin.id()) {
                System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                continue;
            }

            final int id = targetUserId;
            if (users.stream().noneMatch(u -> u.id() == id)) {
                System.out.println("ID " + id + " does not exist. Try again.");
                continue;
            }
            break;
        }

        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        if (userDao.updateUserRoleAndStatus(targetUserId, newRole, newStatus)) {
            System.out.println("User ID " + targetUserId + " updated: role=" + newRole + ", status=" + newStatus + ".");
            printUserTable("ALL SYSTEM USERS", userDao.getAllUsers());
        } else {
            System.out.println("Update failed.");
        }
    }

    /** Keeps asking until the user types a whole number, so letters or symbols never crash the app. */
    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
            }
        }
    }

    /** Keeps asking until a valid role is typed. Returns null if the admin enters 0 to cancel. */
    private String readValidRole() {
        while (true) {
            System.out.print("Enter new Role " + VALID_ROLES.stream().sorted().toList() + " (0 to cancel): ");
            String input = sc.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            if (VALID_ROLES.contains(input)) {
                return input;
            }
            System.out.println("Invalid role! Choose ADMIN, PHARMACIST, or CASHIER.");
        }
    }

    /** Keeps asking until a valid status is typed. Returns null if the admin enters 0 to cancel. */
    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new Status [APPROVED, PENDING] (0 to cancel): ");
            String input = sc.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            if (VALID_STATUSES.contains(input)) {
                return input;
            }
            System.out.println("Invalid status! Choose APPROVED or PENDING.");
        }
    }

    /** Renders any list of users as an aligned, bordered console table. */
    private static void printUserTable(String title, List<User> users) {
        String rowFormat = "| %-5s | %-15s | %-12s | %-10s |%n";
        String border = "+-------+-----------------+--------------+------------+";

        System.out.println("\n" + title);
        System.out.println(border);
        System.out.printf(rowFormat, "ID", "Username", "Role", "Status");
        System.out.println(border);

        if (users.isEmpty()) {
            System.out.printf("| %-51s |%n", "No records found.");
        } else {
            for (User u : users) {
                System.out.printf(rowFormat, u.id(), u.username(), u.role(), u.status());
            }
        }
        System.out.println(border);
    }
}