package com.example.screamlarkbot.repositories;

import com.example.screamlarkbot.models.kinder.Toy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ToyRepository extends JpaRepository<Toy, Long>, CustomToyRepository {

    @Transactional
    @Modifying
    @Query(value = "DELETE from TOY WHERE id IN (SELECT id FROM TOY ORDER BY createdAt LIMIT :limit)", nativeQuery = true)
    void deleteOldToys(long limit);
}
