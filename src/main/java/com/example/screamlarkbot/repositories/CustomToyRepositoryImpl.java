package com.example.screamlarkbot.repositories;

import com.example.screamlarkbot.models.kinder.Toy;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Random;

public class CustomToyRepositoryImpl implements CustomToyRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Toy getRandomToy() {
        Query countQuery = entityManager.createNativeQuery("select count(*) from Toy");
        BigInteger count = (BigInteger)countQuery.getSingleResult();

        if (Objects.equals(count, BigInteger.ZERO)) return null;

        Random random = new Random();
        int number = random.nextInt(count.intValue());

        Query selectQuery = entityManager.createQuery("select t from Toy t");
        selectQuery.setFirstResult(number);
        selectQuery.setMaxResults(1);
        return (Toy) selectQuery.getSingleResult();
    }
}
