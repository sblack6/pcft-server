package com.sblack.pcftserver.service;

import com.sblack.pcftserver.exception.PcftException;
import com.sblack.pcftserver.model.Transaction;
import com.sblack.pcftserver.reader.TransactionReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Service
public class FileUploadService {

    static final String PERSONAL_CAPITAL_SOURCE = "personal-capital";
    static final String SOURCE_ERROR_MSG = "Source must be 'personal-capital'.  This endpoint currently only supports parsing csv files exported from personal capital";
    static final String FILE_PARSE_EXCEPTION = "Unable to parse file.";

    @Autowired
    private TransactionReader personalCapitalTransactionsReader;

    public List<Transaction> readTransactions(MultipartFile file, String source) throws PcftException {
        if (PERSONAL_CAPITAL_SOURCE.equals(source)) {
            return personalCapitalTransactionsReader.readTransactions(this.getFileReader(file));
        } else {
            throw new PcftException(SOURCE_ERROR_MSG);
        }
    }

    private Reader getFileReader(MultipartFile file) throws PcftException {
        try {
            return new InputStreamReader(file.getInputStream());
        } catch (IOException e) {
            throw new PcftException(FILE_PARSE_EXCEPTION, e);
        }

    }
}
