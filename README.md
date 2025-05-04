# CheckersBoardView

**CheckersBoardView** is a customizable Android View for displaying a classic **Checkers** (Draughts) board.  
You can easily integrate it into your app, customize the appearance with XML attributes, and even define custom game rules!  
It also supports playing against a computer opponent powered by the **Minimax algorithm**.

---

## ✨ Features

- Customizable dark and light tile colors
- Responsive and scalable board
- Easy XML integration
- Lightweight and fast
- Listen to board events like win, piece capture, and active player switches
- Support for custom checkers game rules
- Built-in AI opponent using the Minimax algorithm

---

## 📦 Installation

<details>
<summary><b>Gradle</b></summary>

```gradle
implementation 'com.github.iamgerryshom:CheckersBoardView:1.4.0'
```
</details>

Alternatively, you can clone or copy the `CheckersBoardView` class directly into your project.

---

## 🚀 Usage

### XML Setup
Add the `CheckersBoardView` to your layout:

```xml
<com.gerryshom.checkersboardview.view.CheckersBoardView
    android:id="@+id/checkersBoardView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_gravity="center"
    android:layout_margin="32dp"
    app:darkTileColor="#212121"
    app:lightTileColor="#494949" />
```

---

### Programmatic Setup

```java
/**
 * Opponent can be computer or real human.
 * Use inbuilt computer playerId if playing with computer,
 * or define a custom ID if playing with another human.
 */
final String opponentPlayerId = Player.computer().getId(); // Inbuilt computer ID
final String humanPlayerId = "Human"; // Your human player ID

binding.checkersBoardView.setMyPlayerId(humanPlayerId)
    .addListener(new CheckersBoardView.BoardListener() {
        @Override
        public void onPieceCompletedMoveSequence(MoveSequence moveSequence) {
            /**
             * Triggered when a player's piece lands on the final tile of their move.
             * moveSequence contains a list of all moves made in this sequence.
             */
        }

        @Override
        public void onActivePlayerSwitched(String newActivePlayerId) {
            /**
             * Triggered when a complete move sequence ends and control switches to the opponent.
             */
            binding.tvActivePlayer.setText(
                newActivePlayerId.equals(Player.computer().getId()) ? "Computer's turn" : "Your turn"
            );
        }

        @Override
        public void onWin(String winnerPlayerId) {
            /**
             * Triggered when either player can no longer make any valid moves.
             */
            binding.tvActivePlayer.setText(
                winnerPlayerId.equals(Player.computer().getId()) ? "Computer Won" : "You Won"
            );
        }

        @Override
        public void onPieceCaptured(String capturedPiecePlayerId, int remainingPieceCount) {
            /**
             * Triggered when a piece is captured either via a single jump or a multi-jump chain.
             */
            if (capturedPiecePlayerId.equals(Player.computer().getId())) {
                binding.tvOpponentPieceCount.setText("Computer: " + remainingPieceCount);
            } else {
                binding.tvMyPlayerPieceCount.setText("You: " + remainingPieceCount);
            }
        }
    }).setup(humanPlayerId, opponentPlayerId); // Always call this last to prepare the board
```

---

### Rules

```java
// Define the rules for piece capturing
final CaptureRule captureRule = new CaptureRule(
    true,  // forceCapture: player must capture if able
    true,  // allowMultiCapture: allow multiple sequential jumps
    false  // mustTakeLongestJumpPath: not enforced
);

// Define the rules for overall game flow
final GameFlowRule gameFlowRule = new GameFlowRule(
    12, // maxTurnsWithoutCapture: max turns before draw
    60  // maxTurnDurationSeconds: max time per move in seconds
);

// Define the rules for King pieces
final KingPieceRule kingPieceRule = new KingPieceRule(
    0,     // maxMoveSteps (0 = unlimited)
    0,     // maxLandingStepsAfterCapture (0 = unlimited)
    false, // canChangeDirectionDuringMultiJump
    false  // canMoveImmediatelyAfterPromotion
);

// Define the rules for Normal pieces
final NormalPieceRule normalPieceRule = new NormalPieceRule(
    true,  // restrictToForwardMovement
    true,  // allowBackwardCapture
    true   // promoteOnlyAtLastRow
);

/*
 * If no rules are set explicitly, default rules will apply.
 */
binding.checkersBoardView.setRule(captureRule);
binding.checkersBoardView.setRule(gameFlowRule);
binding.checkersBoardView.setRule(kingPieceRule);
binding.checkersBoardView.setRule(normalPieceRule);
```

---

### Playing Moves

```java
/**
 * In a remote player setup,
 * take the MoveSequence from the remote player and apply it.
 * This won't trigger your local moveSequence listener.
 */
binding.checkersBoardView.playOpponentMoveSequence(remotePlayerMoveSequence);
```

---

## ⚙️ Custom Attributes

| Attribute            | Description              | Example   |
|----------------------|--------------------------|-----------|
| `app:darkTileColor`  | Color for dark tiles     | `#212121` |
| `app:lightTileColor` | Color for light tiles    | `#494949` |

---

## 📸 Screenshots

| Default Theme |
|:-------------:|
| <img src="assets/screenshot1.png" alt="Default Board" width="400"/> |

---

## 🤝 Contributing

Pull requests are welcome!  
If you have suggestions for improvements, feel free to open an issue or submit a PR.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

Thanks for checking out **CheckersBoardView**!  
Feel free to use, customize, and build your next awesome project with it.

