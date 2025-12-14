package com.aditya.learningspringboot._8TransactionManagement._1Declarative;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class User {

    /**
     * 1. Based on the underline datasource spring boot will pick up appropriate Transaction manager. This is hidden from us
     * 2. but if we want we can override it. like we did in AppConfig
     */
    @Transactional(transactionManager = "userTransactionManager")
    public void updateUser() {
        System.out.println("Update query to update the user DB values");
    }
}
