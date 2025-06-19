package de.merkeg.openhome.telegram;

import de.merkeg.openhome.config.AppConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@ApplicationScoped
public class TelegramClientProducer {

    @Inject
    AppConfig appConfig;

    @Produces
    public TelegramClient getTelegramClient() {
        return new OkHttpTelegramClient(appConfig.telegram().token());
    }
}
