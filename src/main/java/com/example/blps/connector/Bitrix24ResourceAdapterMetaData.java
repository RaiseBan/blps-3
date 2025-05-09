package com.example.blps.connector;

import jakarta.resource.cci.ResourceAdapterMetaData;

public class Bitrix24ResourceAdapterMetaData implements ResourceAdapterMetaData {

    @Override
    public String getAdapterVersion() {
        return "1.0";
    }

    @Override
    public String getAdapterVendorName() {
        return "Example BLPS";
    }

    @Override
    public String getAdapterName() {
        return "Bitrix24 JCA Connector";
    }

    @Override
    public String getAdapterShortDescription() {
        return "JCA connector for Bitrix24 CRM";
    }

    @Override
    public String getSpecVersion() {
        return "1.7";
    }

    @Override
    public String[] getInteractionSpecsSupported() {
        return new String[] { "com.example.blps.connector.record.BitrixInteractionSpec" };
    }

    @Override
    public boolean supportsExecuteWithInputAndOutputRecord() {
        return true;
    }

    @Override
    public boolean supportsExecuteWithInputRecordOnly() {
        return false;
    }

    @Override
    public boolean supportsLocalTransactionDemarcation() {
        return false;
    }
}