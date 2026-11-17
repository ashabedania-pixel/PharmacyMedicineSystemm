package com.scc.model;

public record SaleReceipt(int saleId, String medicineName, int quantity, double unitPrice, double total) { }