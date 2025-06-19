package de.merkeg.openhome.config;

import io.smallrye.config.WithDefault;

public interface TelegramFeatures {

  @WithDefault("true")
  boolean enableCommandChatId();
}
