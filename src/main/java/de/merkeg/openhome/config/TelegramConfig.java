package de.merkeg.openhome.config;

import java.util.List;

public interface TelegramConfig {
  String token();
  String name();
  List<String> allowedChats();
  String notifyChat();
  TelegramFeatures features();
}
