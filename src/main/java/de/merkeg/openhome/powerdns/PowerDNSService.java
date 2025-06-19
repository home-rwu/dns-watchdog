package de.merkeg.openhome.powerdns;

import de.merkeg.openhome.cname.CNameEntry;
import de.merkeg.openhome.config.AppConfig;
import de.merkeg.openhome.dns.DnsEntry;
import de.merkeg.openhome.powerdns.api.PowerDNSZoneService;
import de.merkeg.openhome.powerdns.api.RRSet;
import de.merkeg.openhome.powerdns.api.ZonePatchRequest;
import de.merkeg.openhome.telegram.TelegramActionWorker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.Set;

@ApplicationScoped
@Slf4j
public class PowerDNSService {

  @RestClient
  PowerDNSZoneService powerDNSZoneService;

  @Inject
  TelegramActionWorker telegramActionWorker;

  @Inject
  AppConfig appConfig;

  @Retry(maxRetries = 3, delay = 1000)
  public void createOrUpdateDnsEntry(DnsEntry entry) {
    log.info("Creating or updating dns entry: {} -> {} ({})", entry.getFqdn(), entry.getAddress(), entry.getMac());
    ZonePatchRequest request = ZonePatchRequest.builder().rrsets(Set.of(entry.toUpdateSet())).build();
    powerDNSZoneService.patchZone(appConfig.powerdns().serverId(), entry.getIface().getZone(), request);

    StringBuilder sb = new StringBuilder();
    sb.append("New infra dns entry created: \n");
    sb.append("FQDN: `").append(entry.getFqdn()).append("`\n");
    sb.append("Hostname: `").append(entry.getHostname()).append("`\n");
    sb.append("IP: `").append(entry.getAddress()).append("`\n");
    sb.append("MAC: `").append(entry.getMac()).append("`\n");
    sb.append("\n");
    sb.append("Please reply to this message to set a custom domain for this entry \\(CNAME\\)");

    SendMessage message = SendMessage.builder()
            .chatId(appConfig.telegram().notifyChat())
            .text(sb.toString())
            .parseMode("MarkdownV2")
            .build();

    telegramActionWorker.sendMessage(message);
  }

  @Retry(maxRetries = 3, delay = 1000)
  public void deleteDnsEntry(DnsEntry entry) {
    log.info("Deleting dns entry: {} -> {} ({})", entry.getFqdn(), entry.getAddress(), entry.getMac());
    ZonePatchRequest request = ZonePatchRequest.builder().rrsets(Set.of(entry.toDeletionSet())).build();
    powerDNSZoneService.patchZone(appConfig.powerdns().serverId(), entry.getIface().getZone(), request);
  }

  @Retry(maxRetries = 3, delay = 1000)
  public void createCNameEntry(CNameEntry entry) {
    log.info("Creating CNAME entry: {} -> {}", entry.getFqdn(), entry.getDns().getFqdn());
    ZonePatchRequest request = ZonePatchRequest.builder().rrsets(Set.of(entry.toUpdateSet())).build();
    String zoneId = getBestFittingZone(entry.getFqdn());
    powerDNSZoneService.patchZone(appConfig.powerdns().serverId(), zoneId, request);
  }

  private String getBestFittingZone(String fqdn) {
    String domainLower = fqdn.toLowerCase();
    String bestMatch = null;

    for (String zone : appConfig.dns().cnameZones()) {
      String zoneLower = zone.toLowerCase();

      if (domainLower.equals(zoneLower) || domainLower.endsWith("." + zoneLower)) {
        if (bestMatch == null || zoneLower.length() > bestMatch.length()) {
          bestMatch = zone;
        }
      }
    }
    return bestMatch;
  }




}
