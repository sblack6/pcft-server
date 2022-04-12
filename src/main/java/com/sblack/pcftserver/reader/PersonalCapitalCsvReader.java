package com.sblack.pcftserver.reader;

import com.sblack.pcftserver.exception.PcftException;
import com.sblack.pcftserver.model.Transaction;
import com.sblack.pcftserver.model.TransactionType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

@Component("personalCapitalTransactionsReader")
public class PersonalCapitalCsvReader implements TransactionReader {

    private static final String[] HEADERS = { "Date", "Account", "Description", "Category", "Tags", "Amount"};

    public List<Transaction> readTransactions(Reader fileReader) throws PcftException {
        try {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withHeader(HEADERS)
                    .withFirstRecordAsHeader()
                    .parse(fileReader);

            List<Transaction> transactions = new LinkedList<>();
            for (CSVRecord record : records) {
                Transaction transactionFromRecord = Transaction.builder()
                        .date(record.get((HEADERS[0])))
                        .category(record.get(HEADERS[3]))
                        .tags(record.get(HEADERS[4]))
                        .amount(Float.parseFloat(record.get(HEADERS[5])))
                        .type(TransactionType.TRANSACTION)
                        .build();
                transactions.add(transactionFromRecord);
            }
            return transactions;
        } catch (IOException e) {
            throw new PcftException("Unable to parse 'Personal Capital' CSV file", e);
        }
    }
}
