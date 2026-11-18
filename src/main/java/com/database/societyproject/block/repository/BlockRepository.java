package com.database.societyproject.block.repository;

import com.database.societyproject.block.entity.Block;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block, Long> {
    List<Block> findBySocietyId(Long societyId);
    boolean existsBySocietyIdAndName(Long societyId, String name);
    boolean existsBySocietyIdAndNameAndIdNot(Long societyId, String name, Long id);
}
