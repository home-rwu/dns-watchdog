# DNS-Watchdog
Eine Anwendung zur Erstellung von automatisierten DNS-Einträgen auf der Basis von DHCP leases von OPNsense.
Damit soll eine einfachere Designierung der IP-Adressen ermöglicht werden.

## Konfiguration

```yaml
# Beispielkonfigurationsdatei für die Anwendung

app:
  opnsense:
    api-key: "<API-SCHLÜSSEL>"          # Der API-Schlüssel für die Verbindung zu OPNsense. Bitte durch den echten Schlüssel ersetzen.
    secret-key: "<GEHEIMSCHLÜSSEL>"      # Der geheime Schlüssel für die Authentifizierung bei OPNsense. Bitte nicht öffentlich zugänglich machen.
    host: "https://<opnsense-host>/"    # Die URL der OPNsense-Instanz.

  powerdns:
    host: "http://<dns-host>:<port>/"   # Der Host und Port der PowerDNS-Verbindung.
    api-key: "<POWERDNS-API-SCHLÜSSEL>" # Der API-Schlüssel für die Authentifizierung bei PowerDNS.
    server-id: "<SERVER-ID>"            # Die Server-ID der PowerDNS-Instanz.

  dns:
    interfaces:                         # Liste der Netzwerkereignisse und deren DNS-Konfigurationszuordnungen.
      - iface: <schnittstelle1>         # Netzwerkinterface (z.B. 'opt3').
        zone: "<zone1>"                 # DNS-Zone, die zugeordnet wird (z.B. 'infra.home.rwu.de').
        prefix: "<präfix1>"             # DNS-Präfix für die Zone (z.B. 'dmz.vl-30').

      - iface: <schnittstelle2>         # Ein weiteres Netzwerkinterface, Zone und Präfix.
        zone: "<zone2>"
        prefix: "<präfix2>"
```