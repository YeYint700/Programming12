import java.net.URL;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.fxml.FXML;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.layout.StackPane;

public class MapGameController implements Initializable {

    public MapData mapData;
    public MoveChara chara;

    @FXML
    public GridPane mapGrid;

    public ImageView[] mapImageViews;

    @FXML
    private Label timerLabel;

    private Timeline timer;
    private int remainingSeconds = 30;

    // ===== Items / Context =====
    private GameItem.GameContext gameCtx;
    private final Map<String, GameItem> itemByPos = new HashMap<>();
    private final List<GameItem> items = new ArrayList<>();

    @FXML
    private Label speedLabel;

    @FXML
    private ImageView keyInventory;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //得点のリセット
        ScoreManager.resetScore();
        
        // 1) map + chara
        mapData = new MapData(21, 15);
        chara = new MoveChara(1, 1, mapData);

        // 2) tiles
        rebuildMapImageViews();

        // 3) context
        gameCtx = new GameItem.GameContext();
        gameCtx.speedBoostLabel = speedLabel;
        gameCtx.keyInventoryIcon = keyInventory;
        if (gameCtx.speedBoostLabel != null)
            gameCtx.speedBoostLabel.setVisible(false);
        if (gameCtx.keyInventoryIcon != null)
            gameCtx.keyInventoryIcon.setVisible(false);

        // 4) items
        items.clear();
        itemByPos.clear();
        setupItems();

        // 5) draw + timer
        drawMap(chara, mapData);
        startTimer();

        // 6) focus for key input (NO mainStage reference)
        mapGrid.setFocusTraversable(true);
        mapGrid.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getRoot().requestFocus();
            }
        });
    }

    // ===== Utility =====

    private String posKey(int x, int y) {
        return x + "_" + y;
    }

    private void rebuildMapImageViews() {
        mapImageViews = new ImageView[mapData.getHeight() * mapData.getWidth()];
        for (int y = 0; y < mapData.getHeight(); y++) {
            for (int x = 0; x < mapData.getWidth(); x++) {
                int index = y * mapData.getWidth() + x;
                mapImageViews[index] = mapData.getImageView(x, y);
            }
        }
    }

    // ===== Difficulty =====
    private enum Difficulty {
        EASY(60),
        NORMAL(45),
        HARD(30);

        final int startSeconds;

        Difficulty(int s) {
            this.startSeconds = s;
        }
    }

    private Difficulty difficulty = Difficulty.EASY; // 初期はEasy

    // ===== Items =====

    private void setupItems() {
        // 置きたい座標（必要なら変更）
        placeItem(new GameItem(3, 3, GameItem.ItemType.SPEEDBOOSTER));
        placeItem(new GameItem(10, 5, GameItem.ItemType.SPEEDBOOSTER));
        placeItem(new GameItem(15, 12, GameItem.ItemType.KEY));
    }

    // 指定座標に置く（壁ならランダム配置に回す）
    private void placeItem(GameItem item) {
        int x = item.getX();
        int y = item.getY();

        if (!mapData.isEnterable(x, y) || itemByPos.containsKey(posKey(x, y))) {
            placeItemRandomly(item);
            return;
        }

        String key = posKey(x, y);
        items.add(item);
        itemByPos.put(key, item);
    }

    private void placeItemRandomly(GameItem item) {
        List<String> emptyKeys = new ArrayList<>();
        for (int y = 0; y < mapData.getHeight(); y++) {
            for (int x = 0; x < mapData.getWidth(); x++) {
                String key = posKey(x, y);
                if (!itemByPos.containsKey(key) && mapData.isEnterable(x, y)) {
                    emptyKeys.add(key);
                }
            }
        }

        if (emptyKeys.isEmpty()) {
            System.out.println("No empty space to place item");
            return;
        }

        int idx = (int) (Math.random() * emptyKeys.size());
        String key = emptyKeys.get(idx);
        int x = Integer.parseInt(key.split("_")[0]);
        int y = Integer.parseInt(key.split("_")[1]);

        GameItem newItem = new GameItem(x, y, item.getType());

        items.add(newItem);
        itemByPos.put(posKey(x, y), newItem);
    }

    private void pickupIfAny() {
        int x = chara.getPosX();
        int y = chara.getPosY();
        String key = posKey(x, y);

        GameItem item = itemByPos.get(key);
        if (item == null)
            return;

        item.applyEffect(gameCtx);
        itemByPos.remove(key);
        items.remove(item);
        ScoreManager.ItemCount +=1;
    }

    // ===== Draw =====

    public void drawMap(MoveChara c, MapData m) {
        int cx = c.getPosX();
        int cy = c.getPosY();

        mapGrid.getChildren().clear();

        for (int y = 0; y < mapData.getHeight(); y++) {
            for (int x = 0; x < mapData.getWidth(); x++) {

                int index = y * mapData.getWidth() + x;

                StackPane cell = new StackPane();
                cell.getChildren().add(mapImageViews[index]);

                GameItem item = itemByPos.get(posKey(x, y));
                if (item != null && !item.isPicked()) {
                    cell.getChildren().add(item.getImageView());
                }

                //キャラをcelに入れることでアイテムと重ねて表示可能に
                if (x == cx && y == cy) {
                    cell.getChildren().add(c.getCharaImageView());
                }

                mapGrid.add(cell, x, y);
            }
        }
    }

    // ===== Movement flow =====

    private void moveWithSpeed(int dx, int dy) {
        int steps = Math.max(1, gameCtx != null ? gameCtx.speedMultiplier : 1);

        for (int i = 0; i < steps; i++) {
            boolean moved = chara.move(dx, dy);
            if (!moved)
                break;

            pickupIfAny();
            checkDoor(chara.getPosX(), chara.getPosY());
        }
    }

    private void afterMove() {
        checkGoal();
        drawMap(chara, mapData);
    }

    // ===== Door / Goal =====

    private void checkDoor(int x, int y) {
        if (!mapData.isDoor(x, y))
            return;

        boolean opened = GameItem.tryUseKey(gameCtx);
        if (opened) {
            mapData.openDoor(x, y);
        }
    }

    private void checkGoal() {
        int x = chara.getPosX();
        int y = chara.getPosY();

        if (x == mapData.getGoalX() && y == mapData.getGoalY()) {
            onGameClear();
        }
    }

    // ===== Stop =====

    private void stopAllTimers() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        if (gameCtx != null && gameCtx.speedBoostTimeline != null) {
            gameCtx.speedBoostTimeline.stop();
            gameCtx.speedBoostTimeline = null;
        }
        if (gameCtx != null) {
            gameCtx.speedMultiplier = 1;
        }
        if (speedLabel != null)
            speedLabel.setVisible(false);
        // keyInventory は「鍵所持表示」なので、状況に応じて消す
        if (keyInventory != null)
            keyInventory.setVisible(false);
    }

    // ===== Clear / Over =====

    private void onGameClear() {
        stopAllTimers();
        try {
            ScoreManager.totalScore(remainingSeconds, ScoreManager.ItemCount);
            
            StageDB.getMainStage().hide();
            StageDB.getMainSound().stop();
            StageDB.getGameClearStage().show();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void onGameOver() {
        stopAllTimers();
        try {
            StageDB.getMainStage().hide();
            StageDB.getMainSound().stop();
            StageDB.getGameOverSound().play();
            StageDB.getGameOverStage().show();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // ===== Key input =====

    public void keyAction(KeyEvent event) {
        KeyCode key = event.getCode();
        if (key == KeyCode.A) {
            leftButtonAction();
        } else if (key == KeyCode.S) {
            downButtonAction();
        } else if (key == KeyCode.W) {
            upButtonAction();
        } else if (key == KeyCode.D) {
            rightButtonAction();
        }
        event.consume();
    }

    public void upButtonAction() {
        printAction("UP");
        chara.setCharaDirection(MoveChara.TYPE_UP);
        moveWithSpeed(0, -1);
        afterMove();
    }

    public void downButtonAction() {
        printAction("DOWN");
        chara.setCharaDirection(MoveChara.TYPE_DOWN);
        moveWithSpeed(0, 1);
        afterMove();
    }

    public void leftButtonAction() {
        printAction("LEFT");
        chara.setCharaDirection(MoveChara.TYPE_LEFT);
        moveWithSpeed(-1, 0);
        afterMove();
    }

    public void rightButtonAction() {
        printAction("RIGHT");
        chara.setCharaDirection(MoveChara.TYPE_RIGHT);
        moveWithSpeed(1, 0);
        afterMove();
    }

    // fanc共通メソッド
    private void regenerateMapAndRestart() {
        System.out.println("Generate New Map (" + difficulty + ")");

        stopAllTimers();

        mapData = new MapData(21, 15);
        rebuildMapImageViews();

        chara = new MoveChara(1, 1, mapData);
        chara.setCharaDirection(MoveChara.TYPE_RIGHT);

        items.clear();
        itemByPos.clear();
        setupItems();

        drawMap(chara, mapData);

        startTimer();
        mapGrid.requestFocus();
    }

    // ===== Buttons =====

    @FXML
    public void func1ButtonAction(ActionEvent event) {
        try {
            System.out.println("func1");
            StageDB.getMainStage().hide();
            StageDB.getMainSound().stop();
            StageDB.getGameOverStage().show();
            StageDB.getGameOverSound().play();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    public void func2ButtonAction(ActionEvent event) {
        setDifficulty(Difficulty.EASY); // 60s
        regenerateMapAndRestart();
    }

    @FXML
    public void func3ButtonAction(ActionEvent event) {
        setDifficulty(Difficulty.NORMAL); // 45s
        regenerateMapAndRestart();
    }

    @FXML
    public void func4ButtonAction(ActionEvent event) {
        setDifficulty(Difficulty.HARD); // 30s
        regenerateMapAndRestart();
    }

    // ===== Timer =====

    private void startTimer() {
        if (timer != null)
            timer.stop();

        remainingSeconds = difficulty.startSeconds;// Change
        if (timerLabel != null)
            timerLabel.setText(String.valueOf(remainingSeconds));

        timer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    remainingSeconds--;
                    if (timerLabel != null)
                        timerLabel.setText(String.valueOf(remainingSeconds));
                    if (remainingSeconds <= 0) {
                        if (timer != null)
                            timer.stop();
                        onTimeUp();
                    }
                }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    // 難易度を変えるメソッド（追加）
    private void setDifficulty(Difficulty d) {
        difficulty = d;
        System.out.println("Difficulty set to: " + d + " (" + d.startSeconds + "s)");
    }

    private void onTimeUp() {
        System.out.println("Time Over");
        onGameOver();
    }

    // ===== Debug =====
    public void printAction(String actionString) {
        System.out.println("Action: " + actionString);
    }
}

