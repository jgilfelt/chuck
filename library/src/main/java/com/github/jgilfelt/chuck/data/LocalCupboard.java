package com.github.jgilfelt.chuck.data;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardBuilder;

public class LocalCupboard {
    private static Cupboard cupboard;

    public static Cupboard getInstance() {
        if (cupboard == null) {
            cupboard = new CupboardBuilder().build();
        }
        return cupboard;
    }

    private LocalCupboard() {
    }
}
