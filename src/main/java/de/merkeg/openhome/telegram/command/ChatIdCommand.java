package de.merkeg.openhome.telegram.command;

import de.merkeg.openhome.config.AppConfig;
import de.merkeg.openhome.telegram.TelegramActionWorker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@ApplicationScoped
@Slf4j
public class ChatIdCommand implements TelegramCommand {

    @Inject
    TelegramActionWorker telegramActionWorker;

    @Inject
    AppConfig appConfig;

    @Override
    public Ability command() {
        return Ability.builder()
                .name("chat_id")
                .input(0)
                .action(this)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .build();
    }

    @Override
    public boolean enabled() {
        return appConfig.telegram().features().enableCommandChatId();
    }

    @Override
    public void accept(MessageContext messageContext) {
        Long chatId = messageContext.chatId();

        telegramActionWorker.sendMessage(SendMessage.builder().chatId(chatId).text("Chat ID: `" + chatId + "`").parseMode("MarkdownV2").build());
    }
}
