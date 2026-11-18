package com.database.societyproject.visitor.repository;
import com.database.societyproject.visitor.entity.Visitor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VisitorRepository extends JpaRepository<Visitor, Long> { List<Visitor> findByFullNameContainingIgnoreCase(String name); List<Visitor> findByPhoneNumber(String phoneNumber); }
