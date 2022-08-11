package com.example.screamlarkbot.repositories;

import com.example.screamlarkbot.models.kinder.Toy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToyRepository extends JpaRepository<Toy, Long>, CustomToyRepository {
}
