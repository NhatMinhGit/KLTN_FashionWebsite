package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    UserProfile findByUser_Id(Long userId);

    @Query("SELECT up FROM UserProfile up WHERE FUNCTION('DAY', up.dob) = :day AND FUNCTION('MONTH', up.dob) = :month")
    List<UserProfile> findByBirthdayToday(@Param("day") int day, @Param("month") int month);
}
