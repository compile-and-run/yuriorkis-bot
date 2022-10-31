package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.lang.Translator;
import com.example.screamlarkbot.models.fight.DuelRequest;
import com.example.screamlarkbot.models.fight.Fighter;
import com.example.screamlarkbot.services.FightService;
import com.example.screamlarkbot.utils.Emote;
import com.example.screamlarkbot.utils.Messages;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class FightMessageHandler {

    private static final String DUEL_COMMAND = "!duel";

    private final TwitchClient twitchClient;

    private final FightService fightService;

    @Value("${screamlark-bot.channel-name}")
    private String channelName;

    @Value("${screamlark-bot.bot-name}")
    private String botName;

    private final Translator translator;

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        eventManager.onEvent(ChannelMessageEvent.class, this::processDuelCommand);
        eventManager.onEvent(ChannelMessageEvent.class, this::processPunchCommand);
        fightService.setOnTimeUp(() -> twitchClient.getChat().sendMessage(channelName, "Время дуэли вышло " + Emote.FEELS_WEAK_MAN));
    }

    private void processDuelCommand(ChannelMessageEvent event) {
        String username = event.getUser().getName();
        String message = event.getMessage();

        if (message.startsWith(DUEL_COMMAND)) {
            String[] words = message.split(" ");
            if (words.length != 2) return;
            String opponent = words[1].trim().toLowerCase();
            if (opponent.startsWith("@")) {
                opponent = opponent.substring(1);
            }

            if (username.equals(opponent)) {
                twitchClient.getChat().sendMessage(channelName, Messages.reply(username, "MMMM"));
                return;
            }

            if (opponent.equals(botName.toLowerCase())) {
                String response = translator.toLocale("duelWithBot");
                twitchClient.getChat().sendMessage(channelName, Messages.reply(username, response));
                return;
            }

            fightService.acceptRequestOrAdd(new DuelRequest(username, opponent),
                    request -> onAccept(channelName, request),
                    request -> onAdd(channelName, request));
        }
    }

    private void onAccept(String channelName, DuelRequest duelRequest) {
        String response = translator.toLocale("duelAccept");
        response = String.format(response, duelRequest.getOpponent(), duelRequest.getRequester());
        twitchClient.getChat().sendMessage(channelName, response);

        response = translator.toLocale("duelRules");
        response = String.format(response, Emote.FIGHT, Emote.FIGHT2, Emote.STREAML_SMASH);
        twitchClient.getChat().sendMessage(channelName, response);
    }

    private void onAdd(String channelName, DuelRequest duelRequest) {
        String response = translator.toLocale("duelRequest");
        response = String.format(response, duelRequest.getRequester(), DUEL_COMMAND, duelRequest.getRequester());
        twitchClient.getChat().sendMessage(channelName, Messages.reply(duelRequest.getOpponent(), response));
    }

    private void processPunchCommand(ChannelMessageEvent event) {
        String username = event.getUser().getName();
        String message = event.getMessage();

        if (!fightService.isFighter(username)) {
            return;
        }

        if (message.contains(Emote.FIGHT.toString()) || message.contains(Emote.FIGHT2.toString())
                || message.contains(Emote.STREAML_SMASH.toString())) {
            boolean isMod = event.getPermissions().contains(CommandPermission.MODERATOR);
            fightService.punch(username,
                    (f1, f2, d) -> onDamage(channelName, f1, f2, d),
                    (f1, f2) -> onKnockout(channelName, f1, f2, isMod));
        }
    }

    private void onDamage(String channelName, Fighter puncher, Fighter fighter, int damage) {
        String response = translator.toLocale("duelDamage");
        response = String.format(response, puncher.getUsername(), puncher.getHp(), fighter.getUsername(), fighter.getHp(), damage);
        twitchClient.getChat().sendMessage(channelName, response);
    }

    private void onKnockout(String channelName, Fighter winner, Fighter looser, boolean isMod) {
        String response = translator.toLocale("duelKnockOut");
        response = String.format(response, winner.getUsername(), looser.getUsername());
        twitchClient.getChat().sendMessage(channelName, response);

        // mods shouldn't time out regular viewers, it's not fair
        if (!isMod) {
            int time = ThreadLocalRandom.current().nextInt(10, 60 * 3);
            twitchClient.getChat().timeout(channelName, looser.getUsername(), Duration.ofSeconds(time), "");
        }
    }
}
