package com.sblack.pcftserver.controller;

import com.sblack.pcftserver.model.Transaction;
import com.sblack.pcftserver.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionsRepo;

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


}
