package de.merkeg.openhome.telegram;

import de.merkeg.openhome.config.AppConfig;
import de.merkeg.openhome.telegram.command.TelegramCommand;
import io.quarkus.arc.All;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.toggle.BareboneToggle;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Singleton
@Startup
public class TelegramBot extends AbilityBot {

    @Inject
    TelegramUpdateHandler updateHandler;

    @Inject
    AppConfig appConfig;

    @Inject
    @All
    List<TelegramCommand> commands;

    private static final BareboneToggle toggle = new BareboneToggle();

    @Inject
    protected TelegramBot(AppConfig config, TelegramClient client) {
        super(client, config.telegram().name(), toggle);
    }


    @PostConstruct
    @SneakyThrows
    public void init() {
        log.info("Initializing Telegram bot commands");
        log.info("Got {} commands", commands.size());

        List<TelegramCommand> filteredCommands = commands.stream().filter(TelegramCommand::enabled).toList();
        List<AbilityExtension> list = new ArrayList<>(filteredCommands);
        log.info("{} commands are enabled", list.size());
        addExtensions(list);

        TelegramBotsLongPollingApplication api = new TelegramBotsLongPollingApplication();
        api.registerBot(appConfig.telegram().token(), this);
        this.onRegister();
    }

    @SneakyThrows
    public <T extends Serializable, Method extends BotApiMethod<T>> T sendMethod(Method method) {
        return super.getTelegramClient().execute(method);
    }

    @Override
    public long creatorId() {
        return 0;
    }



    @Override
    public void consume(List<Update> updates) {
        updates.forEach(update -> {
            updateHandler.handleUpdate(update);
            super.consume(update);
        });
    }

    @Override
    public void onRegister() {
        log.info("Registering Telegram bot commands");
        super.onRegister();
    }
}
