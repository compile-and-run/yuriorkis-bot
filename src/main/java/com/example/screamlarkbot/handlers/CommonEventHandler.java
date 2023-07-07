package com.example.screamlarkbot.handlers;

import com.example.screamlarkbot.lang.Translator;
import com.example.screamlarkbot.models.dancer.DanceEmotes;
import com.example.screamlarkbot.repositories.GptPersonalityRepository;
import com.example.screamlarkbot.services.ChatGptService;
import com.example.screamlarkbot.utils.Commands;
import com.example.screamlarkbot.utils.Emote;
import com.example.screamlarkbot.utils.Messages;
import com.example.screamlarkbot.utils.TwitchHelper;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.*;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.pubsub.domain.PollData;
import com.github.twitch4j.pubsub.events.PollsEvent;
import com.github.twitch4j.pubsub.events.PredictionCreatedEvent;
import com.github.twitch4j.pubsub.events.PredictionUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonEventHandler {

    private static final String HELP_COMMAND = "!help";
    private static final String LANG_COMMAND = "!lang";
    private static final String PERSONALITY_COMMAND = "!person";
    private static final int MIN_DAYS_AFTER_CREATION = 7;

    private final TwitchClient twitchClient;

    private final Translator translator;

    @Value("${screamlark-bot.channel-name}")
    private String channelName;

    private final Set<String> followerIds = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void init() {
        EventManager eventManager = twitchClient.getEventManager();
        eventManager.onEvent(ChannelMessageEvent.class, this::printChannelMessage);
        Commands.registerCommand(eventManager, HELP_COMMAND, this::processHelp);
        Commands.registerCommand(eventManager, LANG_COMMAND, this::processLang);
        Commands.registerCommand(eventManager, PERSONALITY_COMMAND, this::updatePersonality);
        eventManager.onEvent(ChannelMessageEvent.class, this::reactToDances);
        eventManager.onEvent(FollowEvent.class, this::handleFollow);
        eventManager.onEvent(SubscriptionEvent.class, this::handleSubscription);
        eventManager.onEvent(ChannelGoLiveEvent.class, this::handleGoLive);
        eventManager.onEvent(ChannelGoOfflineEvent.class, this::handleGoOffline);
        eventManager.onEvent(UserBanEvent.class, this::handleBan);
        eventManager.onEvent(ChannelJoinEvent.class, this::detectTurborium);
        eventManager.onEvent(PollsEvent.class, this::handlePoll);
        eventManager.onEvent(PredictionCreatedEvent.class, this::handlePredictionCreate);
        eventManager.onEvent(PredictionUpdatedEvent.class, this::handlePredictionUpdated);
        eventManager.onEvent(ChannelMessageEvent.class, this::handleBorodaLink);
    }

    private void handleBorodaLink(ChannelMessageEvent event) {
        String name = event.getUser().getName();
        String message = event.getMessage();

        // Он то просто youtube.com кидает, то youtu.be
        if (name.equalsIgnoreCase("lzheboroda") && message.contains("youtu")) {
            twitchClient.getChat().sendMessage(channelName, Messages.reply(name, Emote.VERY_JAM + " " + "muted"));
        }
    }

    private void printChannelMessage(ChannelMessageEvent event) {
        String permissions = event.getPermissions().toString();
        String username = event.getUser().getName();
        String message = event.getMessage();
        log.info("[" + channelName + "][" + permissions + "] " + username + ": " + message);
    }

    private void processHelp(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        String response = translator.toLocale("help");
        response = Messages.reply(username, response);
        twitchClient.getChat().sendMessage(event.getChannel().getName(), response);
    }

    private void processLang(ChannelMessageEvent event, String args) {
        String username = event.getUser().getName();
        boolean isMod = event.getPermissions().contains(CommandPermission.MODERATOR);
        if (isMod) {
            args = args.trim();
            if ("en".equals(args)) {
                translator.setLocale(Locale.forLanguageTag("en"));
                twitchClient.getChat().sendMessage(channelName, Messages.reply(username, "Bloody hell, mate! I speak English! VeryBased"));
            } else if ("ru".equals(args)) {
                translator.setLocale(Locale.forLanguageTag("ru"));
                twitchClient.getChat().sendMessage(channelName, Messages.reply(username, "Теперь я говорю по-русски VeryPog"));
            } else {
                String response = translator.toLocale("unknownLang");
                twitchClient.getChat().sendMessage(channelName, Messages.reply(username, response));
            }
        }
    }

    private void reactToDances(ChannelMessageEvent event) {
        var dancingEmotes = DanceEmotes.asStringList();

        String[] words = event.getMessage().split(" ");
        String danceText = Arrays.stream(words).filter(dancingEmotes::contains).collect(Collectors.joining(" "));

        if (danceText.length() > 0)
            twitchClient.getChat().sendMessage(channelName, danceText);
    }

    /*private void reactOnLizardPls(ChannelMessageEvent event) {
        String message = event.getMessage();

        String[] words = message.split(" ");

        long lizardNumber = Arrays.stream(words)
                .filter(w -> w.equals(Emote.LIZARD_PLS.toString()))
                .count();

        if (lizardNumber > 0) {
            String response = IntStream.range(0, (int) lizardNumber)
                    .mapToObj(n -> Emote.LIZARD_PLS.toString())
                    .collect(Collectors.joining(" "));
            twitchClient.getChat().sendMessage(channelName, response);
        }
    }*/

    private void handleFollow(FollowEvent event) {
        String username = event.getUser().getName();
        String id = event.getUser().getId();
        log.info("'{}' followed", username);

        if (followerIds.contains(id)) {
            return;
        }
        followerIds.add(id);

        LocalDateTime createdAt = TwitchHelper.getCreatedAt(twitchClient, id);

        if (Duration.between(createdAt, LocalDateTime.now()).toDays() > MIN_DAYS_AFTER_CREATION) {
            String response = translator.toLocale("follow");
            twitchClient.getChat().sendMessage(channelName, Messages.reply(username, response));
        }
    }

    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES)
    protected void clearFollowerCache() {
        followerIds.clear();
        log.info("follower cache has been cleared");
    }

    private void handleSubscription(SubscriptionEvent event) {
        String username = event.getUser().getName();
        log.info("'{}' subscribed", username);
        String response = translator.toLocale("sub");
        twitchClient.getChat().sendMessage(channelName, Messages.reply(username, response));
    }

    private void handleGoLive(ChannelGoLiveEvent event) {
        log.info("'{}' is live", channelName);
        String response = translator.toLocale("helloStreamer");
        twitchClient.getChat().sendMessage(channelName, Messages.reply(channelName, response));
    }

    private void handleGoOffline(ChannelGoOfflineEvent event) {
        log.info("'{}' is offline", channelName);
        String response = translator.toLocale("buyStreamer");
        twitchClient.getChat().sendMessage(channelName, Messages.reply(channelName, response));
    }

    private void handleBan(UserBanEvent event) {
        String username = event.getUser().getName();
        log.info("'{}' was banned", username);
        String response = translator.toLocale("ban");
        twitchClient.getChat().sendMessage(channelName, String.format(response, username));
    }

    private void detectTurborium(ChannelJoinEvent event) {
        String username = event.getUser().getName();
        log.info("'{}' joined the channel", username);
        if ("turborium".equals(username)) {
            twitchClient.getChat().sendMessage(channelName, Emote.OOOO + " Внимание! Турбориум зашел на стрим! " + Emote.OOOO);
        }
    }

    private void handlePoll(PollsEvent event) {
        String pollName = event.getData().getTitle();
        switch (event.getType()) {
            case POLL_CREATE:
                log.info("Poll {} has been started", pollName);
                var response = translator.toLocale("pollCreate");
                twitchClient.getChat().sendMessage(channelName, String.format(response, pollName));
                break;
            case POLL_COMPLETE:
                log.info("Poll {} has been completed", pollName);
                response = translator.toLocale("pollComplete");
                twitchClient.getChat().sendMessage(channelName, String.format(response, pollName, getPollResult(event.getData())));
                break;
            case POLL_TERMINATE:
                log.info("Poll {} has been terminated", pollName);
                response = translator.toLocale("pollTerminate");
                twitchClient.getChat().sendMessage(channelName, String.format(response, pollName));
                break;
            default:
        }
    }

    private String getPollResult(PollData pollData) {
        var max = pollData.getChoices().stream()
            .max(Comparator.comparingLong(pollChoice -> pollChoice.getVotes().getTotal()));
        if (max.isEmpty()) {
            throw new RuntimeException("Exception occurred while computing poll result.");
        }
        if (max.get().getVotes().getTotal() == 0) {
            return translator.toLocale("pollEmpty");
        }
        return pollData.getChoices()
            .stream()
            .filter(pollChoice -> Objects.equals(pollChoice.getTotalVoters(), max.get().getTotalVoters()))
            .map(choice -> choice.getTitle() + "(" + choice.getVotes().getTotal() + ")")
            .collect(Collectors.joining(", "));
    }

    private void handlePredictionCreate(PredictionCreatedEvent event) {
        String predictionName = event.getEvent().getTitle();
        log.info("Prediction {} has been started", predictionName);
        var response = translator.toLocale("predictionCreate");
        twitchClient.getChat().sendMessage(channelName, String.format(response, predictionName));
    }

    private void handlePredictionUpdated(PredictionUpdatedEvent event) {
        String predictionName = event.getEvent().getTitle();
        log.info("Prediction {} has been updated", predictionName);
        if ("RESOLVED".equals(event.getEvent().getStatus())) {
            var response = translator.toLocale("predictionComplete");
            var outcomeId = event.getEvent().getWinningOutcomeId();
            var outcome = event.getEvent().getOutcomes().stream()
                .filter(o -> Objects.equals(o.getId(), outcomeId))
                .findAny();
            if (outcome.isEmpty()) {
                throw new RuntimeException("cannot find prediction outcome");
            }
            twitchClient.getChat().sendMessage(channelName, String.format(response, predictionName, outcome.get().getTitle()));
        } else if ("CANCELED".equals(event.getEvent().getStatus())) {
            var response = translator.toLocale("predictionCancel");
            twitchClient.getChat().sendMessage(channelName, String.format(response, predictionName));
        }
    }

    private void updatePersonality(ChannelMessageEvent event, String args) {
        if (GptPersonalityRepository.isValidPersonality(args)) {
            boolean isMod = event.getPermissions().contains(CommandPermission.MODERATOR);
            String username = event.getUser().getName();

            if (isMod) {
                ChatGptService.setPersonality(GptPersonalityRepository.getPersonality(args));
                twitchClient.getChat().sendMessage(channelName, Messages.reply(username, "теперь я " + args + Emote.VERY_POG));
                return;
            }

            twitchClient.getChat().sendMessage(channelName, Messages.reply(username, "ты не модератор VeryWeird"));
            return;
        }

        twitchClient.getChat().sendMessage(channelName, "нет такой личности VeryWeird");
    }
}
