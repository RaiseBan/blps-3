package com.example.blps.connector;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.LocalTransaction;
import jakarta.resource.cci.MappedRecord;
import jakarta.resource.cci.ConnectionMetaData;
import jakarta.resource.cci.ResultSetInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * Реализация соединения для Битрикс24 коннектора.
 * Обеспечивает взаимодействие с API Битрикс24 через JCA.
 */
@Slf4j
public class Bitrix24Connection implements Connection {

    private boolean closed = false;

    @Override
    public Interaction createInteraction() throws ResourceException {
        checkIfClosed();
        return new Bitrix24Interaction();
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        checkIfClosed();
        // В нашей реализации не поддерживаем локальные транзакции
        throw new ResourceException("Local transactions are not supported");
    }

    @Override
    public ConnectionMetaData getMetaData() throws ResourceException {
        checkIfClosed();
        return new Bitrix24ConnectionMetaData();
    }

    @Override
    public ResultSetInfo getResultSetInfo() throws ResourceException {
        checkIfClosed();
        // Не поддерживаем ResultSet
        throw new ResourceException("ResultSet is not supported");
    }

    @Override
    public void close() throws ResourceException {
        log.debug("Closing Bitrix24 connection");
        closed = true;
    }

    private void checkIfClosed() throws ResourceException {
        if (closed) {
            throw new ResourceException("Connection is already closed");
        }
    }
}