package com.scc.ui;

import com.scc.model.Medicine;

import java.util.List;

/** Shared console table for medicines, used by both the Pharmacist and Cashier menus. */
final class MedicineTable {
    private MedicineTable() { }

    static void print(String title, List<Medicine> medicines) {
        String rowFormat = "| %-5s | %-25s | %-10s | %-7s |%n";
        String border = "+-------+---------------------------+------------+---------+";

        System.out.println("\n" + title);
        System.out.println(border);
        System.out.printf(rowFormat, "ID", "Name", "Price", "Stock");
        System.out.println(border);

        if (medicines.isEmpty()) {
            System.out.printf("| %-54s |%n", "No medicines found.");
        } else {
            for (Medicine m : medicines) {
                System.out.printf(rowFormat, m.id(), m.name(), String.format("%.2f", m.price()), m.stock());
            }
        }
        System.out.println(border);
    }
}