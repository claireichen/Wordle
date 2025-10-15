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

## Optional Run tests:
mvn test

