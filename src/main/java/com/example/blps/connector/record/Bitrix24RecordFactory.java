package com.example.blps.connector.record;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.IndexedRecord;
import jakarta.resource.cci.MappedRecord;
import jakarta.resource.cci.RecordFactory;

import org.springframework.stereotype.Component;

/**
 * Фабрика записей для Битрикс24 коннектора.
 * Создает объекты записей для использования в запросах и ответах.
 */
@Component
public class Bitrix24RecordFactory implements RecordFactory {

    @Override
    public MappedRecord createMappedRecord(String recordName) throws ResourceException {
        return new BitrixMappedRecord(recordName);
    }

    @Override
    public IndexedRecord createIndexedRecord(String recordName) throws ResourceException {
        throw new ResourceException("IndexedRecord not supported by Bitrix24 connector");
    }
}