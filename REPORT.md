# Project Report — Wordle (JavaFX, MVC)

## Overview
A polished Wordle clone built with **JavaFX** using a strict **MVC** architecture. The app includes:
- Smart Hint system (letter-frequency / information gain proxy)
- Statistics dashboard (games played, win %, guess distribution)
- Hard Mode (must reuse revealed letters; greens fixed in place)
- Save/Load persistence using simple UTF-8 text files (no external JSON libs)

---

## Design Decisions

### Architecture (MVC + Observer)
- **Model (`model/`)**: Pure Java, no JavaFX imports. Holds game rules, validation, hint engine, stats, and persistence DTOs.
- **View (`viewfx/`)**: JavaFX UI components (`BoardViewFX`, `KeyboardViewFX`). Only renders model state; no game logic.
- **Controller (`control/`)**: `GameControllerFX` translates key/mouse input into model calls. It owns ephemeral input (current row buffer) and shows end-of-game prompts.
- **Observer pattern**: The model notifies views to repaint when its state changes (lightight custom observable).

**Key separation checks**
- Model contains **no** UI imports.
- View does **not** decide game outcomes—only renders.
- Controller is a thin orchestrator (routes inputs, triggers model methods, updates views, and handles dialogs).

### Data Structures
- **Board state**: `List<String> guesses` and `List<List<LetterFeedback>> feedback`, plus `GameStatus`.
- **Keyboard state**: map from `char -> LetterFeedback` where precedence is `ABSENT < PRESENT < CORRECT`.
- **Dictionary**:
  - `SimpleDictionary` (secrets): from `resources/wordlist.txt`.
  - `EnglishAllowListDictionary` (guesses): validates against `resources/english-words-5.txt` so any real English 5-letter word is accepted while secrets remain curated.
  - (Optional dev mode: `OpenDictionary` accepted any `[A-Za-z]{5}`;  switched off in final build.)
- **Stats**: plain counters and a `int[6]` histogram for wins in 1–6 guesses.

### Algorithms

#### Guess Evaluation (`evaluate(secret, guess)`)
- **Two-pass** algorithm:
  1) Mark **greens** and decrement letter counts.
  2) For remaining positions, mark **yellows** only if the letter count remains > 0; otherwise **absent**.
- Handles duplicates correctly in O(WORD_LENGTH) with O(26) extra space.

#### Keyboard Precedence
- On each feedback cell, upgrade the stored key color with max precedence:
  `ABSENT < PRESENT < CORRECT`. Never downgrade.

#### Smart Hint (information proxy)
- Filter candidate words by constraints implied by prior feedback (greens fixed, min letter counts from green+yellow, and zero-count letters).
- Score each candidate by **unique-letter frequency** across the candidate pool (discourages duplicates; approximates information gain).
- Return top-k suggestions, excluding already-guessed words.

#### Hard Mode Enforcement
- Compute constraints from previous rows:
  - **Must-at**: all green positions must be reused exactly.
  - **Min counts**: for each letter, require at least the number revealed across green+yellow so far.
- Validate proposed guess; on violation, throw `IllegalArgumentException` with a precise message (e.g., “position 3 must be ‘A’” or “must include ‘E’”).

### Persistence
- **Game state** → `~/.wordle/save.txt`

---

## Challenges & Solutions

1) **JavaFX + Modules friction**
 - *Issue*: JPMS and module path caused dependency headaches.
 - *Solution*: Removed module system for the app; used JavaFX Maven plugin on the classpath.

2) **Persistence without third-party libs**
 - *Issue*: Original plan used Gson; modules and dependency resolution got noisy.
 - *Solution*: Replaced with a simple line-based text format; small IO helpers (`Persistence`, `StatsIO`). Readable and robust enough.

3) **Duplicate letter logic**
 - *Issue*: Getting feedback right (e.g., only one yellow when secret has a single copy).
 - *Solution*: Standard two-pass evaluation with per-letter counts; unit tests added for edge cases.

4) **Keyboard precedence conflicts**
 - *Issue*: Early guesses could mark a letter ABSENT that later shows PRESENT/CORRECT.
 - *Solution*: Precedence lattice and only **upgrade** states. Tests assert no downgrade.

5) **Hard Mode corner cases**
 - *Issue*: Precisely defining “reuse revealed letters” across multiple rows (counts vs positions).
 - *Solution*: Derived constraints from accumulated greens+yellows and fixed greens; clear error messages for violations.

6) **Hint quality vs performance**
 - *Issue*: True entropy scoring is heavier; needed snappy suggestions.
 - *Solution*: **Unique-letter frequency** over remaining candidates—fast and effective; can be swapped later for full information-theory scoring.

---

## Lessons Learned
- Strong separation of concerns pays off: adding features (stats, hints, hard mode) required **no** changes to view internals.
- Testing the **model in isolation** is faster and more stable than UI tests.
- Small, explicit text persistence is often enough and avoids dependency churn.
- Designing **clear error messages** (especially for Hard Mode) improves UX and helps debugging.

---
