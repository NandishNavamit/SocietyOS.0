package com.database.societyproject.finance.repository;
import com.database.societyproject.finance.entity.SocietyExpense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SocietyExpenseRepository extends JpaRepository<SocietyExpense, Long> { List<SocietyExpense> findBySocietyId(Long societyId); }
