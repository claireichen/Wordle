# Wordle (JavaFX, MVC)

## Run Instructions
**Prereqs**
- Java 17 or newer (JDK 17+)
- Maven 3.8+

**Steps**
1. Ensure `src/main/resources/wordlist.txt` exists (one 5-letter word per line; used for secret selection).
2. From the project root, run:
   ```bash
   mvn -U clean javafx:run

## Feature List
Core Wordle gameplay
- 6 attempts to guess a 5-letter secret word
- Feedback colors: 🟩 correct spot, 🟨 wrong spot, ⬛ not in word
- On-screen keyboard with live color updates
  
Smart Hint System (Game → Smart Hint…)
- Suggests top candidate guesses using letter-frequency scoring over remaining candidates

Statistics Dashboard (Game → Statistics…)
- Games played, wins/losses, win %, and guess distribution (1–6)
- Auto-saved to ~/.wordle/stats.txt

Hard Mode (Game → Hard Mode)
- Must reuse revealed letters; all greens must stay fixed in position

Persistence
- Save/Load current game to/from ~/.wordle/save.txt
- Simple text format (no external JSON libs)
  
Quality of life
- “Play Again” prompt after finishing a game (picks a new secret)
- Non-modular JavaFX setup (runs on classpath via Maven plugin)

## Controls
- Typing: Physical keyboard letters
- Delete: Backspace (or on-screen ⌫)
- Submit: Enter (or on-screen ⏎)
- Menu Actions:
   - Game → Reset: New game with a new secret
   - Game → Save / Load
   - Game → Smart Hint…
   - Game → Statistics…
   - Game → Hard Mode (toggle)

## Known Issues
- Dictionary scope: by default, only words presented in `english-words-5.txt` are accepted as guesses
- Fixed window size: main window is not resizable
- Hints are heuristic: smart hints use frequency scoring; suggestions may still miss the optimal play in some cases
