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
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
        List<Transaction> createdTransactions = new LinkedList<>();
        transactions.forEach(transaction -> {
            createdTransactions.add(transactionsRepo.save(transaction));
        });
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

    // TODO: Temporary search method impl to support frontend POC.
    //  Refactor search leveraging Spring JPA Criteria and/or specifications.
    @GetMapping("/search")
    public ResponseEntity search(@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate, @RequestParam(required = false) String type, @RequestParam(required = false) String category, @RequestParam(required = false) String tag) {
        List<Transaction> dateSearchResults;
        try {
            DateSearchConstraints dateSearch = this.validateSearchByDateParams(startDate, endDate);
            dateSearchResults = dateSearch(dateSearch);
            log.info("Date search results: {}", dateSearchResults);
        } catch (PcftException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }

        List<Transaction> exampleSearchResults = exampleSearch(type, category);
        log.info("Example search results: {}", exampleSearchResults);

        List<Transaction> combinedResults = new LinkedList<>();
        if (!exampleSearchResults.isEmpty()) {
            if (!dateSearchResults.isEmpty()) {
                exampleSearchResults.retainAll(dateSearchResults);
            }
            combinedResults = exampleSearchResults;
        } else if (type == null && category == null) {
            combinedResults = dateSearchResults;
        }
        List<Transaction> finalResults = tagSearch(combinedResults, tag);
        return ResponseEntity.status(HttpStatus.OK).body(finalResults);
    }

    // TODO: Temporary method to support frontend POC
    private List<Transaction> tagSearch(List<Transaction> transactionList, String tag) {
        if (tag != null && transactionList != null) {
            transactionList = transactionList.stream()
                    .filter(transaction -> transaction.getTags().toLowerCase().contains(tag.toLowerCase()))
                    .collect(Collectors.toList());
        }
        return transactionList;
    }

    // TODO: temporary method to support frontend POC.
    private List<Transaction> dateSearch(DateSearchConstraints searchConstraints) throws PcftException {
        List<Transaction> searchResults = new LinkedList<>();
        if (searchConstraints.isDateSearch()) {
            if (searchConstraints.isValid()) {
                searchResults = this.transactionsRepo.findAllByDateBetween(searchConstraints.getStart(), searchConstraints.getEnd());
            } else {
                throw new PcftException(searchConstraints.getErrorMessage());
            }
        }
        return searchResults;
    }

    // TODO: temporary method to support frontend POC.
    private List<Transaction> exampleSearch(String type, String category) {
        if (type != null || category != null) {
            Transaction.TransactionBuilder exampleBuilder = Transaction.builder()
                    .category(category);
            if (type != null) {
                TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
                exampleBuilder.type(transactionType);
            }
            return transactionsRepo.findAll(Example.of(exampleBuilder.build()));
        }
        return new LinkedList<>();
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
