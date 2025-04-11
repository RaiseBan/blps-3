package com.example.blps.config;

import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component // Добавляем аннотацию компонента
public class AtomikosJtaPlatform extends AbstractJtaPlatform {

    private final UserTransaction userTransaction;
    private final TransactionManager transactionManager;

    // Внедряем зависимости через конструктор
    @Autowired
    public AtomikosJtaPlatform(
            UserTransaction userTransaction,
            TransactionManager transactionManager
    ) {
        this.userTransaction = userTransaction;
        this.transactionManager = transactionManager;
    }

    @Override
    protected TransactionManager locateTransactionManager() {
        return transactionManager;
    }

    @Override
    protected UserTransaction locateUserTransaction() {
        return userTransaction;
    }
}