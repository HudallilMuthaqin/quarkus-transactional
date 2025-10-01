package org.testing.transactional.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Validation {

    private static final Logger LOGGER = LoggerFactory.getLogger(Validation.class);

    /**
     * check length Card Number
     */
    public static boolean checkLength(String cardNo){
        LOGGER.error("Panjang Card Number : " + cardNo);
        return cardNo != null && cardNo.length() == 15;
    }
}
