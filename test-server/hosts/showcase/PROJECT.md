---

title: Nordlicht Outdoor
description: Showcase-Projekt für CondationCMS
----------------------------------------------

# Nordlicht Outdoor

**Nordlicht Outdoor** ist ein fiktiver Anbieter für nachhaltige Outdoor-Reisen, geführte Wanderungen und Survival-Kurse in Nordeuropa und den Alpen.

Das Projekt dient als umfassende Showcase-Website für CondationCMS. Es zeigt sowohl klassische Unternehmensinhalte als auch strukturierte Inhalte, dynamische Übersichten, Taxonomien, Formulare, Medienverwaltung, Mehrsprachigkeit und API-Anbindungen.

## Ziele des Showcase-Projekts

Mit dem Projekt sollen die wichtigsten Funktionen von CondationCMS in einem realistischen Anwendungsszenario präsentiert werden.

Das Showcase richtet sich an zwei Zielgruppen:

### Entwickler

Entwickler erhalten einen Einblick in:

* die Struktur eines CondationCMS-Projekts
* Templates und Layouts
* Content Types
* Sections und Section Items
* dynamische Abfragen
* Taxonomien
* Module und Extensions
* die Headless API
* mehrsprachige Websites
* Medienverarbeitung

### Redakteure

Redakteure können nachvollziehen, wie sie:

* neue Seiten erstellen
* Reisen und Kurse verwalten
* Termine planen
* Inhalte zeitgesteuert veröffentlichen
* Bilder und Galerien pflegen
* Landingpages aus Sections zusammenstellen
* SEO-Metadaten bearbeiten
* Inhalte suchen und filtern
* Übersetzungen verwalten

# Über Nordlicht Outdoor

Nordlicht Outdoor organisiert kleine, geführte Outdoor-Abenteuer abseits überfüllter Touristenrouten.

Das Angebot reicht von entspannten Wanderreisen bis zu anspruchsvollen Winterexpeditionen. Ergänzt wird das Programm durch Survival-Kurse, Orientierungstrainings und Workshops zur Vorbereitung auf längere Touren.

Das Unternehmen legt besonderen Wert auf:

* kleine Gruppen
* erfahrene Guides
* nachhaltiges Reisen
* regionale Partner
* Sicherheit
* authentische Naturerlebnisse

# Seitenstruktur

```text
/
├── reisen/
│   ├── norwegen/
│   ├── schweden/
│   ├── island/
│   └── alpen/
├── kurse/
│   ├── survival-grundkurs/
│   ├── navigation-und-orientierung/
│   ├── erste-hilfe-outdoor/
│   └── winter-survival/
├── termine/
├── reiseziele/
│   ├── norwegen/
│   ├── schweden/
│   ├── island/
│   └── alpen/
├── magazin/
│   ├── ausruestung/
│   ├── reiseberichte/
│   ├── outdoor-wissen/
│   └── sicherheit/
├── ueber-uns/
│   ├── team/
│   ├── nachhaltigkeit/
│   └── partner/
├── faq/
└── kontakt/
```

# Inhaltstypen

## Standardseite

Die Standardseite wird für allgemeine Unternehmensseiten und Landingpages verwendet.

Mögliche Felder:

```yaml
title:
description:
navigationTitle:
featuredImage:
layout:
sections:
seo:
```

Typische Einsatzbereiche:

* Startseite
* Über uns
* Nachhaltigkeit
* Kontakt
* allgemeine Landingpages

## Reise

Eine Reise beschreibt ein buchbares Outdoor-Angebot.

```yaml
title:
description:
destination:
region:
country:
duration:
difficulty:
minimumAge:
groupSize:
price:
currency:
featuredImage:
gallery:
includedServices:
excludedServices:
equipment:
requirements:
highlights:
itinerary:
availableDates:
guide:
categories:
tags:
featured:
```

Beispiel:

```yaml
title: Winterabenteuer in Nordnorwegen
description: Sieben Tage zwischen Fjorden, Schnee und Polarlichtern.
destination: Tromsø
region: Nordnorwegen
country: Norwegen
duration: 7
difficulty: mittel
minimumAge: 16
groupSize:
  minimum: 4
  maximum: 10
price: 1890
currency: EUR
featured: true
categories:
  - winterreisen
  - wanderreisen
tags:
  - norwegen
  - polarlichter
  - schneeschuhwandern
```

## Kurs

Ein Kurs vermittelt praktische Fähigkeiten für Outdoor-Aktivitäten.

```yaml
title:
description:
location:
duration:
level:
minimumAge:
groupSize:
price:
currency:
featuredImage:
gallery:
topics:
equipmentRequired:
includedEquipment:
requirements:
availableDates:
instructor:
categories:
tags:
featured:
```

Beispiel:

```yaml
title: Survival-Grundkurs
description: Die wichtigsten Grundlagen für Notfälle in der Natur.
location: Harz
duration: 2
level: einsteiger
minimumAge: 16
price: 249
currency: EUR
topics:
  - Feuer machen
  - Notunterkunft bauen
  - Wasser aufbereiten
  - Orientierung
  - Verhalten in Notsituationen
categories:
  - survival
tags:
  - einsteiger
  - wochenendkurs
```

## Termin

Ein Termin repräsentiert die konkrete Durchführung einer Reise oder eines Kurses.

```yaml
title:
startDate:
endDate:
location:
relatedContent:
availablePlaces:
maximumPlaces:
bookingStatus:
price:
currency:
guide:
registrationDeadline:
```

Mögliche Statuswerte:

```yaml
bookingStatus:
  - available
  - limited
  - sold-out
  - cancelled
  - completed
```

## Reiseziel

Ein Reiseziel bündelt Reisen, Magazinartikel und Informationen zu einer Region.

```yaml
title:
description:
country:
region:
featuredImage:
gallery:
climate:
bestTravelTime:
language:
currency:
travelInformation:
categories:
featured:
```

## Magazinartikel

Magazinartikel liefern Inspiration, Wissen und aktuelle Informationen.

```yaml
title:
description:
author:
publishDate:
featuredImage:
gallery:
category:
tags:
readingTime:
relatedContent:
featured:
```

Mögliche Kategorien:

* Ausrüstung
* Reiseberichte
* Outdoor-Wissen
* Sicherheit
* Nachhaltigkeit
* Neuigkeiten

## Teammitglied

Ein Teammitglied kann Guide, Trainer oder Mitarbeiter sein.

```yaml
name:
role:
portrait:
description:
specializations:
languages:
certifications:
experience:
email:
socialLinks:
featured:
```

Beispiel:

```yaml
name: Lena Bergmann
role: Outdoor-Guide
specializations:
  - Wintertouren
  - Navigation
  - Erste Hilfe
languages:
  - Deutsch
  - Englisch
  - Norwegisch
certifications:
  - Wilderness First Responder
  - International Mountain Leader
experience: 12 Jahre
```

## FAQ

```yaml
question:
answer:
category:
position:
```

Mögliche Kategorien:

* Buchung
* Bezahlung
* Ausrüstung
* Anreise
* Sicherheit
* Stornierung

## Testimonial

```yaml
name:
location:
quote:
rating:
relatedContent:
image:
published:
```

# Taxonomien

Taxonomien ermöglichen dynamische Übersichten und Filter.

## Länder

```text
Norwegen
Schweden
Island
Deutschland
Österreich
Schweiz
```

## Aktivitäten

```text
Wandern
Schneeschuhwandern
Trekking
Survival
Orientierung
Kanufahren
Wintercamping
```

## Schwierigkeitsgrade

```text
Einfach
Mittel
Anspruchsvoll
Experte
```

## Jahreszeiten

```text
Frühling
Sommer
Herbst
Winter
```

## Zielgruppen

```text
Einsteiger
Fortgeschrittene
Familien
Alleinreisende
Gruppen
Unternehmen
```

# Sections

Landingpages können aus wiederverwendbaren Sections zusammengestellt werden.

## Hero

```yaml
type: hero
headline:
text:
image:
primaryAction:
secondaryAction:
alignment:
```

## Text mit Bild

```yaml
type: text-image
headline:
text:
image:
imagePosition:
action:
```

## Kartenübersicht

```yaml
type: card-grid
headline:
text:
source:
limit:
columns:
action:
```

Mögliche Quellen:

```text
featured-trips
featured-courses
latest-articles
upcoming-events
team-members
destinations
```

## Kommende Termine

```yaml
type: upcoming-events
headline:
limit:
contentType:
showAvailability:
showPrice:
```

## Bildergalerie

```yaml
type: gallery
headline:
images:
layout:
lightbox:
```

## Testimonials

```yaml
type: testimonials
headline:
items:
layout:
```

## FAQ

```yaml
type: faq
headline:
category:
items:
```

## Call to Action

```yaml
type: call-to-action
headline:
text:
backgroundImage:
primaryAction:
secondaryAction:
```

## Newsletter

```yaml
type: newsletter
headline:
text:
form:
privacyText:
```

## Statistiken

```yaml
type: statistics
headline:
items:
  - value:
    label:
```

Beispiel:

```yaml
type: statistics
headline: Abenteuer mit Erfahrung
items:
  - value: 12
    label: Jahre Erfahrung
  - value: 850
    label: zufriedene Teilnehmer
  - value: 24
    label: Reiseziele
  - value: 8
    label: erfahrene Guides
```

# Startseite

Die Startseite soll die wichtigsten Funktionen des CMS sichtbar machen.

## Empfohlene Sections

1. Hero mit großem Hintergrundbild
2. Hervorgehobene Reisen
3. Unternehmensvorstellung
4. Kommende Termine
5. Beliebte Reiseziele
6. Survival- und Outdoor-Kurse
7. Vorteile von Nordlicht Outdoor
8. Testimonials
9. Aktuelle Magazinartikel
10. Newsletter-Anmeldung
11. Call to Action

## Beispielinhalt

```yaml
title: Nordlicht Outdoor
description: Geführte Outdoor-Reisen und Survival-Kurse in kleinen Gruppen.
template: home
sections:
  - type: hero
    headline: Draußen beginnt das echte Abenteuer
    text: Geführte Reisen, Survival-Kurse und unvergessliche Naturerlebnisse.
    image: /media/home/hero-norway.jpg
    primaryAction:
      label: Reisen entdecken
      url: /reisen/
    secondaryAction:
      label: Kurse ansehen
      url: /kurse/

  - type: card-grid
    headline: Unsere beliebtesten Reisen
    source: featured-trips
    limit: 3
    columns: 3

  - type: text-image
    headline: Kleine Gruppen. Große Erlebnisse.
    text: Wir reisen bewusst, persönlich und abseits der bekannten Wege.
    image: /media/home/group-hiking.jpg
    imagePosition: right

  - type: upcoming-events
    headline: Die nächsten Abenteuer
    limit: 5
    showAvailability: true
    showPrice: true

  - type: card-grid
    headline: Wissen, das draußen zählt
    source: featured-courses
    limit: 3
    columns: 3

  - type: testimonials
    headline: Was unsere Teilnehmer sagen

  - type: card-grid
    headline: Neues aus dem Magazin
    source: latest-articles
    limit: 3
    columns: 3

  - type: newsletter
    headline: Inspiration für dein nächstes Abenteuer
    text: Neue Reisen, Kurse und Outdoor-Tipps direkt in dein Postfach.
```

# Beispielreisen

## Winterabenteuer in Nordnorwegen

Eine siebentägige Reise mit Schneeschuhwanderungen, Wintercamping und Polarlichtbeobachtung.

Schwerpunkte:

* Nordnorwegische Fjorde
* Polarlichter
* Schneeschuhwandern
* kleine Gruppe
* lokale Unterkünfte
* Einführung in Winter-Survival

## Trekking durch den Sarek-Nationalpark

Eine anspruchsvolle Trekkingtour durch eine der letzten großen Wildnisregionen Europas.

Schwerpunkte:

* mehrtägige Trekkingtour
* Übernachtung im Zelt
* anspruchsvolles Gelände
* Flussüberquerungen
* Navigation ohne markierte Wege

## Hüttenwanderung in den Alpen

Eine geführte Wanderung von Hütte zu Hütte für Teilnehmer mit normaler Grundkondition.

Schwerpunkte:

* alpine Landschaften
* regionale Küche
* Übernachtung auf Berghütten
* Gepäcktransport optional
* geeignet für ambitionierte Einsteiger

# Beispielkurse

## Survival-Grundkurs

Die Teilnehmer lernen die wichtigsten Grundlagen für Notfälle in der Natur.

Inhalte:

* Feuer ohne Feuerzeug
* Wasser finden und aufbereiten
* Notunterkunft bauen
* Orientierung
* Erste Hilfe
* Notruf und Rettung

## Navigation und Orientierung

Ein praxisorientierter Kurs zum sicheren Navigieren mit Karte, Kompass und GPS.

Inhalte:

* topografische Karten lesen
* Kompass verwenden
* Standort bestimmen
* Routen planen
* GPS und Smartphone sinnvoll einsetzen
* Navigation bei schlechter Sicht

## Erste Hilfe Outdoor

Ein Erste-Hilfe-Kurs für Situationen, in denen professionelle Hilfe nicht sofort verfügbar ist.

Inhalte:

* Versorgung typischer Verletzungen
* Unterkühlung
* Hitzeschäden
* improvisierter Transport
* Notfallkommunikation
* Entscheidungsfindung in abgelegenen Regionen

# Dynamische Abfragen

## Hervorgehobene Reisen

```text
type == "trip" and featured == true
```

## Kommende Termine

```text
type == "event" and startDate >= now()
```

## Reisen nach Norwegen

```text
type == "trip" and country == "Norwegen"
```

## Einsteigerangebote

```text
difficulty == "Einfach" or level == "einsteiger"
```

## Winterangebote

```text
season contains "Winter"
```

## Magazinartikel zu Ausrüstung

```text
type == "article" and category == "Ausrüstung"
```

# Suche

Die Volltextsuche sollte Inhalte aus mehreren Inhaltstypen erfassen.

Durchsuchbare Inhalte:

* Seitentitel
* Beschreibungen
* Fließtexte
* Reiseziele
* Aktivitäten
* Tags
* Namen von Teammitgliedern

Beispielhafte Suchanfragen:

```text
Norwegen
Survival
Winter
Einsteiger
Polarlichter
Navigation
Alpen
```

Suchergebnisse können nach Inhaltstyp gefiltert werden:

```text
Alle
Reisen
Kurse
Termine
Magazin
Reiseziele
```

# Formulare

## Kontaktformular

Felder:

```text
Name
E-Mail-Adresse
Telefonnummer
Betreff
Nachricht
Datenschutzeinwilligung
```

## Reiseanfrage

Felder:

```text
Name
E-Mail-Adresse
Reise
Gewünschter Termin
Anzahl der Teilnehmer
Erfahrungsniveau
Nachricht
```

## Kursanmeldung

Felder:

```text
Name
E-Mail-Adresse
Kurs
Termin
Anzahl der Teilnehmer
Besondere Anforderungen
```

## Newsletter

Felder:

```text
E-Mail-Adresse
Interessengebiete
Datenschutzeinwilligung
```

# Medien

Das Showcase soll die Medienfunktionen von CondationCMS umfassend demonstrieren.

Verwendete Medientypen:

* Hero-Bilder
* Reisebilder
* Bildergalerien
* Teamfotos
* Karten
* PDF-Packlisten
* Reiseunterlagen
* Vorschaubilder für Magazinartikel

Zu demonstrierende Funktionen:

* automatisches Skalieren
* verschiedene Bildgrößen
* Cropping
* WebP-Ausgabe
* responsive Bilder
* Alt-Texte
* Bildunterschriften
* zentrale Medienauswahl
* Austausch bestehender Bilder

# Downloads

Mögliche Downloads:

```text
Packliste für Winterreisen
Packliste für Hüttentouren
Vorbereitung auf den Survival-Kurs
Allgemeine Reisebedingungen
Sicherheitsinformationen
Nachhaltigkeitsbericht
```

# Mehrsprachigkeit

Das Showcase sollte mindestens auf Deutsch und Englisch verfügbar sein.

Beispielstruktur:

```text
/de/reisen/winterabenteuer-norwegen/
/en/trips/northern-norway-winter-adventure/
```

Zu demonstrierende Funktionen:

* übersetzte Seiten
* sprachabhängige Navigation
* lokalisierte URLs
* lokalisierte Taxonomien
* unterschiedliche Datumsformate
* unterschiedliche Währungen
* sprachabhängige Suche
* `hreflang`
* Canonical URLs
* Übersetzungsstatus im Manager

# SEO

Jede Seite kann eigene SEO-Daten erhalten.

```yaml
seo:
  title:
  description:
  canonical:
  index: true
  follow: true
  image:
```

Zusätzliche SEO-Funktionen:

* XML-Sitemap
* strukturierte Daten
* Open Graph
* Social-Media-Bilder
* Canonical URLs
* `hreflang`
* zeitgesteuerte Veröffentlichung
* automatische Metadaten als Fallback

Mögliche strukturierte Daten:

```text
Organization
Article
Person
Event
Course
TouristTrip
BreadcrumbList
FAQPage
```

# Workflow und Veröffentlichung

Das Showcase kann verschiedene Veröffentlichungszustände darstellen.

```text
Draft
In Review
Scheduled
Published
Archived
```

Beispielszenarien:

* Ein neuer Magazinartikel wird als Entwurf gespeichert.
* Ein Redakteur reicht den Artikel zur Prüfung ein.
* Der Artikel wird für einen späteren Zeitpunkt geplant.
* Eine Reise wird nach dem letzten Termin archiviert.
* Ein ausgebuchter Termin bleibt sichtbar, kann aber nicht mehr gebucht werden.

# Headless API

Strukturierte Inhalte können zusätzlich über die API bereitgestellt werden.

Beispiele:

```http
GET /api/trips
GET /api/trips/winter-adventure-norway
GET /api/courses
GET /api/events
GET /api/events?country=norway
GET /api/articles
GET /api/destinations
```

Mögliche API-Verwendung:

* mobile App
* externer Veranstaltungskalender
* Buchungssystem
* Partnerwebsite
* Digital-Signage-Anzeige
* Newsletter-System

# Module und Integrationen

Das Showcase bietet sinnvolle Anwendungsfälle für Module und Extensions.

## Kartenintegration

Darstellung von:

* Reisezielen
* Treffpunkten
* Wanderrouten
* Kursorten
* Unterkünften

## Wetterintegration

Anzeige von:

* aktuellem Wetter
* Reisezeit-Empfehlungen
* Schneelage
* Tageslichtdauer

## Newsletter

Anbindung an einen externen Newsletter-Dienst.

## Buchungssystem

Übergabe von Reise- und Termindaten an ein externes Buchungssystem.

## Bewertungen

Teilnehmer können Reisen oder Kurse bewerten.

## SEO-Modul

Automatische Ausgabe von:

* Meta-Tags
* Open Graph
* strukturierten Daten
* Canonical URLs
* XML-Sitemaps

# Beispielnavigation

```yaml
items:
  - label: Reisen
    url: /reisen/

  - label: Kurse
    url: /kurse/

  - label: Termine
    url: /termine/

  - label: Reiseziele
    url: /reiseziele/

  - label: Magazin
    url: /magazin/

  - label: Über uns
    url: /ueber-uns/

  - label: Kontakt
    url: /kontakt/
```

# Designrichtung

Das Design soll modern, hochwertig und naturverbunden wirken.

## Stil

* große Naturbilder
* klare Typografie
* großzügige Abstände
* übersichtliche Karten
* dezente Animationen
* gute Lesbarkeit
* klare Call-to-Actions

## Farbwelt

Empfohlene Grundfarben:

```text
Dunkles Waldgrün
Gedämpftes Moosgrün
Steingrau
Sand
Off-White
Akzentfarbe Orange oder Rostrot
```

## Komponenten

Benötigte UI-Komponenten:

* Header
* Hauptnavigation
* Sprachumschalter
* Hero
* Breadcrumb
* Karten
* Filter
* Suche
* Terminliste
* Verfügbarkeitsanzeige
* Preisbox
* Galerie
* Akkordeon
* Tabs
* Formulare
* Pagination
* Newsletter-Box
* Footer

# Showcase-Szenarien

## Für Entwickler

1. Projekt lokal starten
2. neues Template hinzufügen
3. eigene Section erstellen
4. Reise über eine Query laden
5. eigene Template-Funktion verwenden
6. Extension registrieren
7. Modul aktivieren
8. Inhalte über die API abrufen

## Für Redakteure

1. neue Reise erstellen
2. Bilder auswählen
3. Termin hinzufügen
4. Seite in der Vorschau prüfen
5. SEO-Daten bearbeiten
6. Veröffentlichung planen
7. englische Übersetzung erstellen
8. Inhalt veröffentlichen

# Fazit

Nordlicht Outdoor verbindet klassische Unternehmenskommunikation mit strukturierten Inhalten, redaktionellen Workflows und technischen Integrationen.

Das Thema ist umfangreich genug, um nahezu alle Funktionen von CondationCMS realistisch zu demonstrieren, bleibt aber gleichzeitig verständlich und visuell attraktiv.

Es eignet sich damit sowohl als technische Referenzimplementierung als auch als öffentliches Showcase für Entwickler, Redakteure und potenzielle Nutzer von CondationCMS.
