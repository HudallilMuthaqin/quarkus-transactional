package org.testing.transactional.utils;

import java.security.SecureRandom;

public class Generator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int LENGTH = 15;

    /**
     * Generate Account Number fix length 15
     */
    public static String accountNumber() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(RANDOM.nextInt(10)); // angka 0-9
        }
        return sb.toString();
    }

}
