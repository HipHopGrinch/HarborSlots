# Harbor Slots

Original cruise-casino slots for Android. Play money, a saved bankroll, and voyage tallies for eight cruise lines.

The games are Harbor Hold, Brightwork, Night Market, Penny Patch, Kelp Fall, Mesa Trail, Puffer Reef, and Beacon Wheel. Names, symbols, reel strips, and prize weights belong to this project.

## Game types

Each game belongs to one of eight types (Lock and Respin, Build and Bonus, Free Spins, Pot Bonus, Tumbling Ways, 243 Ways, Cluster Pays, Prize Wheel), defined in `engine/src/main/kotlin/com/harborreel/engine/GameTypes.kt`. The circled "i" on a game shows its type, the type's description, the game's own note, and real cabinets of that type seen on a cruise floor.

## Outcomes

Each reel is a virtual strip. A spin draws a stop with PCG-XSH-RR (`engine/src/main/kotlin/com/harborreel/engine/Rng.kt`), then shows that symbol and the two below it. Symbols that appear more often on the strip land more often. Bonus draws use the same generator and the weight tables in `Tables.kt` and `Games.kt`.

A fixed seed repeats. A normal session takes a fresh seed.

## Voyage tallies

One tally point per credit wagered, on the cruise line you select. Totals stay in app storage on the device. They are not a cruise line's real loyalty program.

## Run

Open this folder in Android Studio, let Gradle sync, and run the `app` configuration on a device or emulator. Java 17 or newer is required.

Engine checks, from this folder:

```
gradlew.bat :engine:test
```
