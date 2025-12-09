package com.aditya.learningspringboot._5TransactionManagement._4IsolationLevels;

import org.springframework.transaction.annotation.Transactional;

@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class Isolation {
}
