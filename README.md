# � Vokabeltrainer Pro - Modern Edition

Ein **moderner** Vokabeltrainer mit **Grok-Style Dark Theme** - Alles in EINER Java-Datei!

## ✨ Features

### 🎨 **Modernes UI Design**
- **Dark Theme** im Grok-Stil (dunkle Farben, moderne Akzente)
- **Farbcodierte Buttons** (Blau, Lila, Grün, Rot, Orange)
- **Große, lesbare Schrift** (Segoe UI)
- **Smooth Hover-Effekte** auf allen Buttons
- **Professionelle Tabellen** mit modernem Styling

### 📋 **2 Hauptseiten**
- **📚 Verwaltung:** Vokabelsets & Vokabeln verwalten
- **🎯 Quiz:** Interaktives Lernen mit 3 Modi

### 🎯 **3 Quiz-Modi**
1. **📖 Original → Übersetzung** (z.B. "hello" → "hallo")
2. **🔄 Übersetzung → Original** (z.B. "hallo" → "hello") 
3. **🎲 Gemischt** (beide Richtungen zufällig)

### 💾 **Datenbank**
- **SQLite** - Professionelle Datenbank
- Alle Daten in `vokabeltrainer.db`
- Zuverlässig und schnell

## 🚀 Installation & Start

### **Windows - SUPER EINFACH:**

```
Doppelklick auf start.bat
```

**ODER im Terminal:**

```powershell
java -cp ".;sqlite-jdbc.jar;slf4j-api.jar;slf4j-simple.jar" Vokabeltrainer
```

### **Was start.bat macht:**
- ✅ Lädt SQLite & SLF4J automatisch herunter
- ✅ Kompiliert das Programm
- ✅ Startet den Vokabeltrainer

## 📖 Verwendung

1. **Set erstellen:** "➕ Neues Set" klicken
2. **Vokabeln hinzufügen:** Set auswählen, "➕ Vokabel hinzufügen"
3. **Quiz starten:** "🎯 Zum Quiz" → Modus wählen → "▶ Quiz starten"
4. **Lernen:** Übersetzungen eingeben und Enter drücken!

## 🎨 Design-Highlights

- **Dunkler Hintergrund** (#0F172A) - angenehm für die Augen
- **Akzentfarben:**
  - 🔵 Blau (#3B82F6) - Primäre Aktionen
  - 🟣 Lila (#9333EA) - Navigation
  - 🟢 Grün (#22C55E) - Erfolg/Hinzufügen
  - 🔴 Rot (#EF4444) - Löschen
  - 🟠 Orange (#FB923C) - Sekundäre Aktionen
- **Große Buttons** mit Hover-Effekten
- **Moderne Tabelle** mit Farbcodierung
- **Smooth Übergänge** zwischen Ansichten

## 📁 Dateien

- ✅ **`Vokabeltrainer.java`** - DAS komplette Programm! 🎯
- ✅ **`start.bat`** - Automatischer Starter
- ✅ **`vokabeltrainer.db`** - SQLite Datenbank
- ✅ **`sqlite-jdbc.jar`** - SQLite Treiber
- ✅ **`slf4j-api.jar`** & **`slf4j-simple.jar`** - Logging

## 🛠 Technologie

- **GUI:** Java Swing (mit custom Styling)
- **Datenbank:** SQLite
- **Design:** Grok-inspired Dark Theme
- **Alles in 1 Datei:** ~630 Zeilen pure Power! ⚡
