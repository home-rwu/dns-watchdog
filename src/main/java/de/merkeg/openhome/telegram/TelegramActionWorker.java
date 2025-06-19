package de.merkeg.openhome.telegram;

import de.merkeg.openhome.config.AppConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.reactions.SetMessageReaction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji;

import java.util.List;

@ApplicationScoped
@Slf4j
public class TelegramActionWorker {

    @Inject
    TelegramBot telegramBot;

    @Inject
    Event<BotApiMethod<?>> methodPipeline;

    @Inject
    AppConfig appConfig;

    @SneakyThrows
    public void processMessage(@Observes BotApiMethod<?> method) {
        if(method instanceof SendMessage) {
            SendMessage message = (SendMessage) method;
            log.debug("Sending message [{}] to chat with id [{}]", message.getText(), message.getChatId());
        }

        this.telegramBot.sendMethod(method);
    }

    public void sendMethod(BotApiMethod<?> answerCallbackQuery) {
        methodPipeline.fire(answerCallbackQuery);
    }

    public void sendMessage(SendMessage message) {
        methodPipeline.fire(message);
    }

    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        sendMessage(message);
    }


    public void sendMessage(Long chatId, String text) {
        sendMessage(String.valueOf(chatId), text);
    }

    public void sendReaction(String chatId, int messageId, String emoji) {

        SetMessageReaction reaction = SetMessageReaction.builder()
                .chatId(chatId)
                .messageId(messageId)
                .reactionTypes(List.of(ReactionTypeEmoji.builder().type(ReactionTypeEmoji.EMOJI_TYPE).emoji(emoji).build()))
                .build();
        methodPipeline.fire(reaction);
    }
}
