package com.database.societyproject.staff.repository;
import com.database.societyproject.staff.entity.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {
    java.util.Optional<StaffProfile> findByUserId(Long userId);
}
