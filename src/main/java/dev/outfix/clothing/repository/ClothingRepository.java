package dev.outfix.clothing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.outfix.clothing.entity.Clothing;
import dev.outfix.user.entity.User;

public interface ClothingRepository extends JpaRepository<Clothing, Long> {

    List<Clothing> findByUser(User user);

    @Query("SELECT c FROM Clothing c WHERE c.user = :user " +
           "AND c.tokenFormalitas >= :token " +
           "AND c.tags LIKE %:tag% " +
           "AND (:dresscode IS NULL OR c.tags LIKE %:dresscode%)")
    List<Clothing> findMatchingItems(@Param("user") User user,
                                     @Param("token") int token,
                                     @Param("tag") String tag,
                                     @Param("dresscode") String dresscode);
}
