# Number Guessing Game (Java Swing GUI)

A desktop number-guessing game built with Java Swing. Pick a difficulty, guess the secret number within a time limit and a limited number of attempts, and rack up points across rounds.

## Features

- **Three difficulty levels**
  - Easy: 1–50, 10 attempts
  - Medium: 1–100, 7 attempts
  - Hard: 1–200, 5 attempts
- **30-second countdown timer** per round, with color warnings as time runs low (green → orange → red)
- **Scoring system** based on attempts used and time remaining
- **Round and total score tracking** across multiple games
- **Play Again** (same difficulty, new number) and **New Game** (reset stats) options
- Input validation for empty fields, non-numeric input, and out-of-range guesses

## Requirements

- Java Development Kit (JDK) 8 or later

## How to Run

1. Save the source file as `NumberGuessingGameGUI.java`.
2. Compile:
   ```bash
   javac NumberGuessingGameGUI.java
   ```
3. Run:
   ```bash
   java NumberGuessingGameGUI
   ```

## How to Play

1. Select a difficulty level from the dropdown before starting a round.
2. Enter your guess in the text field and click **Guess** (or press Enter).
3. The game tells you if your guess is too high or too low.
4. Win by guessing the number before you run out of attempts or time.
5. Click **Play Again** to try another round at the same difficulty, or **New Game** to reset your total score and rounds.
6. Click **Exit** to close the game.

## Scoring

```
score = (maxAttempts - attemptsUsed + 1) * 10 + (timeLeft / 2)
```

Fewer attempts and more remaining time yield a higher score.

## Project Structure

```
NumberGuessingGameGUI.java   # Single-file Swing application (GUI + game logic)
```

## Notes

- Difficulty cannot be changed mid-round; finish or lose the current round first.
- If time runs out before a correct guess, the round ends automatically and the secret number is revealed.