package com.quantumrift;

import com.quantumrift.content.ModContent;

public class CommonClass {

    public static void init() {
        ModContent.init();
        Constants.LOG.info("Quantum Rift content registered");
    }
}
