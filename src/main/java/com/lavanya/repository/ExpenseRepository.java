package com.lavanya.repository;

import com.lavanya.domain.Expense;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Expense entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {}
