# Sicherheitsrichtlinie

## Unterstützte Versionen

Wir bieten derzeit Sicherheitsupdates für die folgenden Versionen von FileVault:

| Version | Unterstützt          |
| ------- | -------------------- |
| 1.0.x   | :white_check_mark:   |
| < 1.0   | :x:                  |

## Meldung einer Sicherheitslücke

Wir nehmen die Sicherheit von FileVault sehr ernst. Da es sich um eine Anwendung zur Dateiverschlüsselung handelt, ist die Sicherheit ein zentraler Aspekt unserer Software.

Wenn Sie eine Sicherheitslücke entdecken, möchten wir diese Information so schnell wie möglich erhalten, um angemessene Maßnahmen ergreifen zu können.

### Bitte folgen Sie diesen Schritten:

1. **Vertrauliche Meldung**: Senden Sie eine E-Mail an [SICHERHEITS-E-MAIL] mit Details zur Sicherheitslücke
   - Beschreiben Sie die Sicherheitslücke so detailliert wie möglich
   - Geben Sie Schritte zur Reproduktion an, wenn möglich
   - Fügen Sie alle technischen Details hinzu, die uns helfen könnten, das Problem zu verstehen und zu beheben

2. **Vertraulichkeit**: Bitte veröffentlichen Sie keine Information über die Sicherheitslücke öffentlich, bevor wir Zeit hatten, sie zu beheben

### Was Sie erwarten können:

- Wir werden innerhalb von 48 Stunden antworten und den Empfang Ihrer Meldung bestätigen
- Wir werden mit Ihnen kommunizieren, während wir die Sicherheitslücke untersuchen
- Wir werden Sie über unseren Zeitplan für die Behebung informieren
- Nach Behebung der Sicherheitslücke werden wir Ihnen eine Danksagung zukommen lassen, sofern Sie dies wünschen

## Sicherheitsmerkmale

FileVault verwendet folgende Sicherheitsmaßnahmen:

- AES-256-GCM Verschlüsselung 
- PBKDF2 mit HMAC-SHA256 für Schlüsselableitung
- 65.536 Iterationen für die Schlüsselableitung
- 96-Bit Initialisierungsvektoren
- 128-Bit Authentifizierungs-Tags
- Sichere Zufallszahlengenerierung für kryptografische Operationen

## Sicherheitsaudit

Das FileVault-Projekt wird regelmäßig einem Sicherheitsaudit unterzogen. Die Ergebnisse des letzten Audits finden Sie in unserem GitHub Repository unter [docs/security-audit-results.md]. 