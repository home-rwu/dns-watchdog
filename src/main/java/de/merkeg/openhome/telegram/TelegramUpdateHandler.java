package de.merkeg.openhome.telegram;


import de.merkeg.openhome.cname.CNameEntry;
import de.merkeg.openhome.config.AppConfig;
import de.merkeg.openhome.dns.DnsEntry;
import de.merkeg.openhome.powerdns.PowerDNSService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@ApplicationScoped
@Slf4j
public class TelegramUpdateHandler {

    @Inject
    TelegramActionWorker telegramActionWorker;

    @Inject
    PowerDNSService powerDNSService;

    @Inject
    AppConfig appConfig;

    public void handleUpdate(Update update) {
        if(update.hasMessage()) {
            handleMessage(update);
        }
    }

    @Transactional
    public void handleMessage(Update update) {
        Message message = update.getMessage();
        User from = message.getFrom();
        Boolean groupChat = message.getChat().isGroupChat();

        if(!message.isReply()) return;

        Message replyMessage = message.getReplyToMessage();
        Pattern macPattern = Pattern.compile("([0-9a-fA-F]{2}(:[0-9a-fA-F]{2}){5})");
        Matcher matcher = macPattern.matcher(replyMessage.getText());

        if(!matcher.find()) {
            telegramActionWorker.sendMessage(message.getChatId(), "Internal error: could not find MAC");
            return;
        }

        String mac = matcher.group(1);
        DnsEntry entry = DnsEntry.findById(mac);

        if(entry == null) {
            telegramActionWorker.sendMessage(message.getChatId(), "Internal error: could not find dns entry");
            return;
        }

        String pattern = "^(?=.{1,253}$)(?!-)[A-Za-z0-9-]{1,63}(?<!-)"
                + "(\\.(?!-)[A-Za-z0-9-]{1,63}(?<!-))*$";

        if(!message.getText().matches(pattern)) {
            telegramActionWorker.sendMessage(message.getChatId(), "Error: Invalid fqdn supplied");
            return;
        }
        String fqdn = message.getText();
        log.debug(fqdn);

        if(CNameEntry.findById(fqdn) != null) {
            telegramActionWorker.sendMessage(message.getChatId(), "Error: FQDN already exists");
            return;
        }

        CNameEntry cNameEntry = CNameEntry.builder()
                .fqdn(fqdn)
                .dns(entry).build();

        cNameEntry.persist();

        powerDNSService.createCNameEntry(cNameEntry);

        telegramActionWorker.sendReaction(String.valueOf(message.getChatId()), message.getMessageId(), "\uD83D\uDC4D");
    }
}
