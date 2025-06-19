# DNS-Watchdog
Eine Anwendung zur Erstellung von automatisierten DNS-Einträgen auf der Basis von DHCP leases von OPNsense.
Damit soll eine einfachere Designierung der IP-Adressen ermöglicht werden.

## Konfiguration

```yaml
# Beispielkonfigurationsdatei für die Anwendung

app:
  opnsense:
    api-key: 
    secret-key: 
    host: 
  powerdns:
    host: 
    api-key: 
    server-id: 
  telegram:
    token: 
    name: 
    allowed-chats:
      - "<chat-id>"
    notify-chat: "<chat-id>"
  dns:
    cname-zones:
      - home.rwu.de
      - intern.home.rwu.de
  hibernate-orm:
    database:
      generation: update
  datasource:
    db-kind: mariadb
    username: 
    password: 
    jdbc:
      url: jdbc:mariadb://<url>:3306/<db>
```