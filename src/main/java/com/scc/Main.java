package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. Make sure the database and tables exist
        DatabaseInitializer.initialize();

        // 2. Create the single shared instances
        Scanner sc = new Scanner(System.in);
        UserDao userDao = new UserDao();

        // 3. Hand control to the authentication UI
        new Authentication(userDao, sc).start();

        sc.close();
    }
}