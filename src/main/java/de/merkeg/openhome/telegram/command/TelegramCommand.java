package de.merkeg.openhome.telegram.command;


import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import java.util.function.Consumer;

public interface TelegramCommand extends Consumer<MessageContext>, AbilityExtension {
    Ability command();
    boolean enabled();
}
