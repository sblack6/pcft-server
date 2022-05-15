package com.sblack.pcftserver.repositories;

import com.sblack.pcftserver.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>  {
    List<Transaction> findAllByDateBetween(LocalDate dateStart, LocalDate dateEnd);
}
