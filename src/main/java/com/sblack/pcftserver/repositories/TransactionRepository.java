package com.sblack.pcftserver.repositories;

import com.sblack.pcftserver.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long>  {

}
