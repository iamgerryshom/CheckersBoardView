# CheckersBoardView

**CheckersBoardView** is a customizable Android View for displaying a classic **Checkers** (Draughts) board.  
You can easily integrate it into your app, customize the appearance with XML attributes, and even define custom game rules!

---

## ✨ Features

- Customizable dark and light tile colors
- Responsive and scalable board
- Easy XML integration
- Lightweight and fast
- Listen to board events like win, piece capture, and active player switches
- Support for custom checkers game rules

---

## 📦 Installation

<details>
<summary><b>Gradle</b></summary>

```gradle
implementation 'com.github.iamgerryshom:CheckersBoardView:1.4.0'
```
</details>

For now, you can clone or copy the `CheckersBoardView` class into your project.

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
// Set your player ID (must be unique between the two players)
binding.checkersBoardView.setMyPlayerId("me");

// Set up event listeners to respond to board events
binding.checkersBoardView.addListener(new CheckersBoardView.BoardListener() {
    @Override
    public void onWin(String winnerPlayerId) {
        // Called when a player wins the game
    }

    @Override
    public void onPieceCaptured(String capturedPiecePlayerId, int remainingPieceCount) {
        // Triggered when a piece is captured (jumped)
    }

    @Override
    public void onActivePlayerSwitched(String newActivePlayerId) {
        // Called when the active player changes after a move
    }

    @Override
    public void onPieceCompletedMoveSequence(MoveSequence moveSequence) {
        /*
         * Called when the player completes a move (regular move, single capture, or chain capture).
         * The full move sequence is provided and can be recorded or sent to an opponent.
         */
    }
});

/*
 * Initialize the shared game board.
 * Both players must reference the same CheckersBoard instance (use synchronization for remote play).
 */
binding.checkersBoardView.setCheckersBoard(new CheckersBoard());

/*
 * Use this method to play a move sequence received from the opponent.
 * Ideal for syncing moves in online or Bluetooth-based multiplayer games.
 */
binding.checkersBoardView.playOpponentMoveSequence(new MoveSequence());

// Define the rules for piece capturing
final CaptureRule captureRule = new CaptureRule(
        true, //forceCapture
        true, //allowMultiCapture
        false //mustTakeLongestJumpPath
);

// Define the rules for overall game flow
final GameFlowRule gameFlowRule = new GameFlowRule(
        12, //maxTurnsWithoutCapture
        60 //maxTurnDurationSeconds
);

// Define the rules for King pieces
final KingPieceRule kingPieceRule = new KingPieceRule(
        0, //maxMoveSteps (0 = infinity)
        0, //maxLandingStepsAfterCapture (0 == infinity)
        false, //canChangeDirectionDuringMultiJump
        false //canMoveImmediatelyAfterPromotion
);

// Define the rules for Normal pieces
final NormalPieceRule normalPieceRule = new NormalPieceRule(
        true, //restrictToForwardMovement
        true, //allowBackwardCapture
        true //promoteOnlyAtLastRow
);

/*
if you don't set any rules the default rules will be applied
 */
binding.checkersBoardView.setRule(captureRule);
binding.checkersBoardView.setRule(gameFlowRule);
binding.checkersBoardView.setRule(kingPieceRule);
binding.checkersBoardView.setRule(normalPieceRule);
```

---

## ⚙️ Custom Attributes

| Attribute | Description | Example |
|:----------|:------------|:--------|
| `app:darkTileColor` | Color for dark tiles | `#212121` |
| `app:lightTileColor` | Color for light tiles | `#494949` |

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

