# CheckersBoardView

**CheckersBoardView** is a customizable Android View for displaying a classic **Checkers** (Draughts) board.
You can easily integrate it into your app and customize the appearance with XML attributes.

---

## ✨ Features

- Customizable dark and light tile colors
- Responsive and scalable board
- Easy integration with XML
- Lightweight and fast
- Listen to board events like win, capture, and active player switches

---

## 📦 Installation

<details>
<summary><b>Gradle (Coming Soon)</b></summary>

```gradle
implementation 'com.gerryshom:checkersboardview:1.0.0'
```
</details>

For now, you can clone or copy the `CheckersBoardView` class into your project.

---

## 🚀 Usage

### XML Setup
Simply add the `CheckersBoardView` to your XML layout:

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

### Programmatic Setup

```java
// Make sure you set your player ID
binding.checkersBoardView.setMyPlayerId("me");

// Set up event listeners
binding.checkersBoardView.addListener(new CheckersBoardView.BoardListener() {
    @Override
    public void onWin(String winnerPlayerId) {
        // Handle game win
    }

    @Override
    public void onPieceCaptured(String capturedPiecePlayerId, int remainingPieceCount) {
        // Handle piece capture
    }

    @Override
    public void onActivePlayerSwitched(String newActivePlayerId) {
        // Handle player turn switch
    }

    @Override
    public void onPieceCompletedMoveSequence(MoveSequence moveSequence) {
        // Handle completed move sequences
    }
});

// Set up a new game board
binding.checkersBoardView.setCheckersBoard(new CheckersBoard());
```

---

## ⚙️ Custom Attributes

| Attribute | Description | Example |
|:----------|:------------|:--------|
| `app:darkTileColor` | Color of the dark tiles | `#212121` |
| `app:lightTileColor` | Color of the light tiles | `#494949` |

You can easily change the board appearance by modifying these attributes in XML.

---

## 📸 Screenshots

<img src="assets/screenshot1.png" alt="Default Board" width="400"/>

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

