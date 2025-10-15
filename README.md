Wordle (JavaFX MVC Project)
Overview

This is a full-featured Wordle clone implemented in JavaFX, built for the Object-Oriented Programming project.
It demonstrates mastery of MVC architecture, encapsulation, inheritance, polymorphism, and abstraction, along with persistent data storage and optional extra-credit features.

Features
Core Gameplay

Guess a 5-letter word within 6 tries.

Color-coded feedback:

🟩 Green: Correct letter in the correct position

🟨 Yellow: Letter exists but in a different position

⬛ Gray: Letter not in the word

On-screen keyboard updates colors dynamically.

Secret words randomly selected from wordlist.txt.

MVC Architecture

Model: Game logic, word validation, statistics, and hint computation.

View: JavaFX UI (board grid, keyboard, menus).

Controller: Handles all user input and coordinates between the view and model.

Persistence

Game state saved to ~/.wordle/save.txt

Statistics persisted automatically to ~/.wordle/stats.txt

All saves are simple UTF-8 text (no external libraries).

Smart Hint System (Extra Credit)

Suggests 5 likely guesses using letter-frequency analysis and remaining constraints.

Avoids repeating words you’ve already guessed.

Access via Game → Smart Hint…

Statistics Dashboard (Extra Credit)

Tracks:

Games played

Wins and losses

Win percentage

Guess distribution histogram (1–6 tries)

Accessible via Game → Statistics…

Automatically updates after every completed game.

Hard Mode (Extra Credit)

Enforces stricter guessing rules:

All revealed letters (greens/yellows) must be reused in subsequent guesses.

All greens must stay fixed in their discovered positions.

Toggle via Game → Hard Mode checkbox.

Project Structure
src/
├── main/java/org/example/wordle/
│   ├── App.java                # Entry point (JavaFX)
│   ├── Launcher.java           # Shortcut launcher
│   ├── control/
│   │   └── GameControllerFX.java
│   ├── model/
│   │   ├── WordleModel.java
│   │   ├── SimpleDictionary.java
│   │   ├── OpenDictionary.java
│   │   ├── EnglishAllowListDictionary.java
│   │   ├── HintEngine.java
│   │   ├── Stats.java
│   │   └── GameStatus.java, LetterFeedback.java, etc.
│   ├── io/
│   │   ├── Persistence.java
│   │   └── StatsIO.java
│   └── viewfx/
│       ├── BoardViewFX.java
│       └── KeyboardViewFX.java
│
├── main/resources/
│   ├── wordlist.txt            # Wordle secret list
│   └── english-words-5.txt     # Optional allow list for real English words
│
└── test/java/org/example/wordle/
    └── WordleModelTest.java

How to Run
Prerequisites

Java 17+

Maven 3.8+

JavaFX SDK (if not bundled)

Commands
# Compile
mvn clean compile

# Run
mvn javafx:run


If you use IntelliJ:

Open the project as a Maven project.

Ensure the JavaFX VM options are not needed (Maven plugin handles them).

Run org.example.wordle.Launcher.

Controls
Action	Key / UI
Type Letter	Keyboard
Delete	Backspace / ⌫
Submit Guess	Enter / ⏎
Reset Game	Game → Reset
Save Game	Game → Save
Load Game	Game → Load
Get Hint	Game → Smart Hint…
Toggle Hard Mode	Game → Hard Mode
View Stats	Game → Statistics…
💾 Files Saved
File	Description
~/.wordle/save.txt	Current game state
~/.wordle/stats.txt	Persistent statistics
wordlist.txt	Source of secret words
english-words-5.txt (optional)	Large allow-list for real English words
Testing

Run all unit tests:

mvn test

Example Tests

Correct/partial/absent feedback

Word validation

Win/loss detection

Keyboard precedence

Hard Mode enforcement
