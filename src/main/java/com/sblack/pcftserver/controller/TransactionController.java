package com.sblack.pcftserver.controller;

import com.sblack.pcftserver.exception.PcftException;
import com.sblack.pcftserver.model.DateSearchConstraints;
import com.sblack.pcftserver.model.Transaction;
import com.sblack.pcftserver.model.TransactionType;
import com.sblack.pcftserver.repositories.TransactionRepository;
import com.sblack.pcftserver.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionsRepo;

    @Autowired
    private FileUploadService fileUploadService;

    private static final String SUCCESS = "\"Success.\"";

    @GetMapping("/list")
    public List<Transaction> listAll() {
        return transactionsRepo.findAll();
    }

    @GetMapping("/{id}")
    public Transaction get(@PathVariable long id) {
        return transactionsRepo.getById(id);
    }

    @PostMapping
    public List<Transaction> create(@RequestBody List<Transaction> transactions) {
        List<Transaction> createdTransactions = transactionsRepo.saveAll(transactions);
        return createdTransactions;
    }

    @PutMapping("/{id}")
    public Transaction update(@PathVariable long id, @RequestBody Transaction transaction) {
        transaction.setId(id);
        Transaction updatedTransaction = transactionsRepo.save(transaction);
        return updatedTransaction;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable long id) {
        transactionsRepo.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body(SUCCESS);
    }

    @GetMapping("/search")
    public ResponseEntity search(@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate, @RequestParam(required = false) String type, @RequestParam(required = false) String category, @RequestParam(required = false) String tags) {
        log.info("Search request got params: {}, {}", startDate, endDate);
        DateSearchConstraints dateSearch = this.validateSearchByDateParams(startDate, endDate);
        log.info("Date search constraints: {}", dateSearch);
        List<Transaction> dateSearchResults = null;
        List<Transaction> exampleSearchResults = null;
        if (dateSearch.isDateSearch()) {
            if (dateSearch.isValid()) {
                log.info("Searching by date...");
                dateSearchResults = this.transactionsRepo.findAllByDateBetween(dateSearch.getStart(), dateSearch.getEnd());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dateSearch.getErrorMessage());
            }
        }
        if (type != null || category != null || tags != null) {
            Transaction.TransactionBuilder exampleBuilder = Transaction.builder()
                    .category(category)
                    .tags(tags);
            if (type != null) {
                TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
                exampleBuilder.type(transactionType);
            }
            exampleSearchResults = transactionsRepo.findAll(Example.of(exampleBuilder.build()));
        }
        if (exampleSearchResults != null) {
            if (dateSearchResults != null) {
                exampleSearchResults.retainAll(dateSearchResults);
                return ResponseEntity.status(HttpStatus.OK).body(exampleSearchResults);
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(exampleSearchResults);
            }
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(dateSearchResults);
        }
    }

    @PostMapping("/upload-transactions")
    public ResponseEntity uploadTransactions(@RequestParam String source,
                                             @RequestBody MultipartFile file) {
        List<Transaction> transactions;
        try {
            transactions = fileUploadService.readTransactions(file, source);
            transactions = transactionsRepo.saveAll(transactions);
        } catch (PcftException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(transactions);
    }

    private DateSearchConstraints validateSearchByDateParams(String startDate, String endDate) {
        DateSearchConstraints.DateSearchConstraintsBuilder statusBuilder = DateSearchConstraints.builder();
        boolean isDateSearch = true;
        if (startDate == null && endDate == null) {
            isDateSearch = false;
        }
        if (isDateSearch) {
            try {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                statusBuilder.isValid(true).start(start).end(end);
            } catch (DateTimeParseException e) {
                statusBuilder.isValid(false).errorMessage("Unable to parse either startDate or endDate.  Dates must be in YYYY-MM-DD format.");
            }
        }
        return statusBuilder.isDateSearch(isDateSearch).build();
    }

}
