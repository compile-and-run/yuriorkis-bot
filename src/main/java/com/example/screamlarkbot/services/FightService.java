package com.example.screamlarkbot.services;

import com.example.screamlarkbot.models.fight.DamageHandler;
import com.example.screamlarkbot.models.fight.DuelRequest;
import com.example.screamlarkbot.models.fight.Fighter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
@Service
public class FightService {
    private static final int REQUEST_TIME = 3;
    private static final int DUEL_TIME = 3;

    private static final int MIN_DAMAGE = 10;
    private static final int MAX_DAMAGE = 50;
    private static final int COOL_DOWN = 3000;

    private final Set<DuelRequest> duelRequests = ConcurrentHashMap.newKeySet();

    private Fighter fighter1;
    private Fighter fighter2;

    private boolean isDuelStarted;
    private LocalDateTime startedAt;

    @Setter
    private Runnable onTimeUp = () -> {};

    public synchronized void acceptRequestOrAdd(DuelRequest duelRequest, Consumer<DuelRequest> onAccept, Consumer<DuelRequest> onAdd) {
        if (isDuelStarted) return;
        DuelRequest opponentRequest = new DuelRequest(duelRequest.getOpponent(), duelRequest.getRequester());
        if (duelRequests.contains(opponentRequest)) {
            isDuelStarted = true;
            startedAt = LocalDateTime.now();
            onAccept.accept(opponentRequest);
            duelRequests.remove(opponentRequest);
            fighter1 = new Fighter(duelRequest.getRequester());
            fighter2 = new Fighter(duelRequest.getOpponent());
        } else {
            duelRequests.add(duelRequest);
            onAdd.accept(duelRequest);
        }
    }

    public synchronized void punch(String requester, DamageHandler damageHandler, BiConsumer<Fighter, Fighter> onKnockout) {
        if (!isDuelStarted) return;
        Fighter puncher;
        Fighter fighter;
        if (requester.equals(fighter1.getUsername())) {
            puncher = fighter1;
            fighter = fighter2;
        } else if (requester.equals(fighter2.getUsername())) {
            puncher = fighter2;
            fighter = fighter1;
        } else {
            return;
        }
        if (Duration.between(puncher.getLastPunch(), LocalDateTime.now()).toMillis() < COOL_DOWN) {
            return;
        }
        int damage = ThreadLocalRandom.current().nextInt(MIN_DAMAGE, MAX_DAMAGE + 1);
        fighter.decreaseHp(damage);
        puncher.updateLastPunch();
        damageHandler.onDamage(puncher, fighter, damage);
        if (fighter.getHp() == 0) {
            onKnockout.accept(puncher, fighter);
            isDuelStarted = false;
        }
    }

    public synchronized boolean isFighter(String username) {
        if (fighter1 == null || fighter2 == null) {
            return false;
        }
        return username.equals(fighter1.getUsername()) || username.equals(fighter2.getUsername());
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public synchronized void cleanOldRequests() {
        duelRequests.removeIf(request -> Duration.between(request.getCreatedAt(), LocalDateTime.now()).toMinutes() >= REQUEST_TIME);
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public synchronized void checkDuelTime() {
        if (isDuelStarted && Duration.between(startedAt, LocalDateTime.now()).toMinutes() >= DUEL_TIME) {
            isDuelStarted = false;
            onTimeUp.run();
        }
    }
}
