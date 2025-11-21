# 📚 Vokabeltrainer - Java Edition

Ein vollständiger Vokabeltrainer in **EINER einzigen Java-Datei** mit SQLite-Datenbank!

## ✨ Features

### 📋 2 Screens
- **Verwaltung:** Vokabelsets erstellen, Vokabeln hinzufügen/löschen
- **Prüfung/Quiz:** 3 Modi für effektives Lernen!

### 🎯 3 Prüfungsmodi
1. **📖 Original → Übersetzung** (z.B. Englisch → Deutsch)
2. **🔄 Übersetzung → Original** (z.B. Deutsch → Englisch) 
3. **🎲 Gemischt** (beide Richtungen zufällig)

### 💾 Datenbank
- **SQLite** - Professionelle, dateibasierte Datenbank
- Alle Daten in `vokabeltrainer.db`
- Robust und zuverlässig

## 🚀 Installation & Start

### Einfachster Weg (Windows):

**Doppelklick auf `start.bat`** - Fertig! 🎉

Das Skript:
- Lädt automatisch SQLite JDBC Treiber herunter
- Kompiliert das Programm
- Startet den Vokabeltrainer

### Manuell:

```bash
# 1. SQLite JDBC Treiber herunterladen
# https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.0.0/sqlite-jdbc-3.45.0.0.jar
# Als "sqlite-jdbc.jar" speichern

# 2. Kompilieren
javac -encoding UTF-8 -cp ".;sqlite-jdbc.jar" Vokabeltrainer.java

# 3. Starten
java -cp ".;sqlite-jdbc.jar" Vokabeltrainer
```

## 📖 Verwendung

1. **Set erstellen:** Klicke "➕ Neues Set"
2. **Vokabeln hinzufügen:** Wähle Set aus, klicke "➕ Vokabel hinzufügen"
3. **Prüfungsmodus wählen:** 
   - Original → Übersetzung (klassisch)
   - Übersetzung → Original (umgekehrt)
   - Gemischt (beides zufällig)
4. **Quiz starten:** Übersetzungen eingeben und Enter drücken
5. **Score verfolgen:** Sieh deine Fortschritte in Echtzeit!

## 📁 Dateien

- **`Vokabeltrainer.java`** - Komplettes Programm in EINER Datei! 🎯
- **`start.bat`** - Automatischer Start (lädt SQLite herunter)
- **`vokabeltrainer.db`** - SQLite Datenbank (wird automatisch erstellt)
- **`sqlite-jdbc.jar`** - JDBC Treiber (wird automatisch heruntergeladen)

## 🛠 Technologie

- **GUI:** Java Swing
- **Datenbank:** SQLite (embedded)
- **Alles in EINER Datei:** Einfach zu verwenden und zu verstehen!
- **Nur 1 Dependency:** SQLite JDBC Treiber (automatischer Download)
