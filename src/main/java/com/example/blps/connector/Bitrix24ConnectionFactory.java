package com.example.blps.connector;

import javax.naming.NamingException;
import javax.naming.Reference;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.ConnectionFactory;
import jakarta.resource.cci.ConnectionSpec;
import jakarta.resource.cci.RecordFactory;
import jakarta.resource.cci.ResourceAdapterMetaData;

import org.springframework.stereotype.Component;

@Component
public class Bitrix24ConnectionFactory implements ConnectionFactory {

    private final RecordFactory recordFactory;

    public Bitrix24ConnectionFactory(RecordFactory recordFactory) {
        this.recordFactory = recordFactory;
    }

    @Override
    public Connection getConnection() throws ResourceException {
        return new Bitrix24Connection();
    }

    @Override
    public Connection getConnection(ConnectionSpec properties) throws ResourceException {
        
        return getConnection();
    }

    @Override
    public RecordFactory getRecordFactory() throws ResourceException {
        return recordFactory;
    }

    @Override
    public ResourceAdapterMetaData getMetaData() throws ResourceException {
        return new Bitrix24ResourceAdapterMetaData();
    }

    @Override
    public Reference getReference() throws NamingException {
        
        throw new UnsupportedOperationException("JNDI lookup not supported");
    }

    @Override
    public void setReference(Reference reference) {
        
        throw new UnsupportedOperationException("JNDI lookup not supported");
    }
}