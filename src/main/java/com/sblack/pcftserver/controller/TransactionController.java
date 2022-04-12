package com.sblack.pcftserver.controller;

import com.sblack.pcftserver.exception.PcftException;
import com.sblack.pcftserver.model.Transaction;
import com.sblack.pcftserver.repositories.TransactionRepository;
import com.sblack.pcftserver.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionsRepo;

    @Autowired
    private FileUploadService fileUploadService;

    private static final String SUCCESS = "Success.";

    @GetMapping("/list")
    public List<Transaction> listAll() {
        return transactionsRepo.findAll();
    }

    @GetMapping("/{id}")
    public Transaction get(@PathVariable long id) {
        return transactionsRepo.getById(id);
    }

    @PostMapping
    public Transaction create(@RequestBody Transaction transaction) {
        Transaction createdTransaction = transactionsRepo.save(transaction);
        return createdTransaction;
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
}
