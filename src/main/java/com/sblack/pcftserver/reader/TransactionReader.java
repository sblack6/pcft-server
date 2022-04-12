package com.sblack.pcftserver.reader;

import com.sblack.pcftserver.exception.PcftException;
import com.sblack.pcftserver.model.Transaction;

import java.io.Reader;
import java.util.List;

public interface TransactionReader {

    List<Transaction> readTransactions(Reader fileReader) throws PcftException;
}
