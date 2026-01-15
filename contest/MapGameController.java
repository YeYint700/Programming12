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

    private javafx.animation.Timeline timer;
    private int remainingSeconds = 30;

    private GameItem.GameContext gameCtx;
    private final Map<String, GameItem> itemByPos = new HashMap<>();
    private final List<GameItem> items = new ArrayList<>();

    @FXML
    private Label speedLabel;

    @FXML
    private ImageView keyInventory;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mapData = new MapData(21, 15);
        chara = new MoveChara(1, 1, mapData);
        mapImageViews = new ImageView[mapData.getHeight() * mapData.getWidth()];
        for (int y = 0; y < mapData.getHeight(); y ++) {
            for (int x = 0; x < mapData.getWidth(); x ++) {
                int index = y * mapData.getWidth() + x;
                mapImageViews[index] = mapData.getImageView(x, y);
            }
        }
        gameCtx = new GameItem.GameContext();
        gameCtx.speedBoostLabel=speedLabel;
        gameCtx.keyInventoryIcon=keyInventory;

        if(gameCtx.speedBoostLabel !=null)
        gameCtx.speedBoostLabel.setVisible(false);
        if(gameCtx.keyInventoryIcon !=null)
        gameCtx.keyInventoryIcon.setVisible(false);

        setupItems();

        drawMap(chara, mapData);

        // timer starts.
        startTimer();

        mapGrid.setFocusTraversable(true);
        mapGrid.requestFocus();
        mapGrid.setOnKeyPressed(this::keyAction);
    }
    private String posKey(int x, int y){
        return x + "_"+ y;
    }

    private void setupItems(){
        //必要に応じて座標を調整
        placeItem(new GameItem(3,3,GameItem.ItemType.SPEEDBOOSTER));
        placeItem(new GameItem(10, 5, GameItem.ItemType.SPEEDBOOSTER));
        placeItem(new GameItem(15, 12, GameItem.ItemType.KEY));
    }

    private void placeItem(GameItem item){
        items.add(item);
        itemByPos.put(item.posKey(),item);    
    }

    // Draw the map
    public void drawMap(MoveChara c, MapData m) {
        int cx = c.getPosX();
        int cy = c.getPosY();
        mapGrid.getChildren().clear();
        for (int y = 0; y < mapData.getHeight(); y ++) {
            for (int x = 0; x < mapData.getWidth(); x ++) {
                int index = y * mapData.getWidth() + x;
                StackPane cell = new StackPane();
                cell.getChildren().add(mapImageViews[index]);
                GameItem item = itemByPos.get(posKey(x,y));
                if(item !=null && !item.isPicked()){
                    cell.getChildren().add(item.getImageView());
                }
                if (x == cx && y == cy) {
                    mapGrid.add(c.getCharaImageView(), x, y);
                } else {
                    mapGrid.add(cell, x, y);
                }
            }
        }
    }

    private void afterMove(){
        pickupIfAny();
        checkDoor(chara.getPosX(), chara.getPosY());
        checkGoal();
        drawMap(chara,mapData);
    }

    private void pickupIfAny(){
        int x = chara.getPosX();
        int y = chara.getPosY();

        GameItem item = itemByPos.get(posKey(x,y));
        if(item == null || item.isPicked())
            return;
        item.applyEffect(gameCtx);
        itemByPos.remove(item.posKey());
    }

    private void moveWithSpeed(int dx,int dy){
        int steps = Math.max(1,gameCtx.speedMultiplier);
        for(int i =0; i<steps; i++){
            boolean can = chara.move(dx,dy);
            pickupIfAny();
            checkDoor(chara.getPosX(),chara.getPosY());
        }
        drawMap(chara, mapData);
    }

    private void checkDoor(int x,int y){
        if(!isDoorTile(x,y))
            return;
        boolean opened = GameItem.tryUseKey(gameCtx);
        if(opened){
            openDoorAt(x,y);
            drawMap(chara, mapData);
        }
    }

    private boolean isDoorTile(int x, int y){
        return mapData.isDoor(x,y);
    }

    private void openDoorAt(int x,int y){
        mapData.openDoor(x,y);
    }
    private void checkGoal(){
        int x = chara.getPosX();
        int y = chara.getPosY();

        if(x == mapData.getGoalX() && y == mapData.getGoalY()
            && mapData.getMap(x,y) == MapData.TYPE_SPACE){
            onGameClear();
        }
    }

    private void onGameClear(){
        try{
            if(timer != null)
                timer.stop();

            if(gameCtx != null && gameCtx.speedBoostTimeline != null){
                gameCtx.speedBoostTimeline.stop();
            }

            StageDB.getMainStage().hide();
            StageDB.getMainSound().stop();

            StageDB.getGameClearStage().show();

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void onGameOver(){
        try{
            if(gameCtx != null && gameCtx.speedBoostTimeline != null){
                gameCtx.speedBoostTimeline.stop();
                gameCtx.speedBoostTimeline=null;
            }
            if(timer != null){
                timer.stop();
            }
            StageDB.getMainStage().hide();
            StageDB.getMainSound().stop();
            StageDB.getGameOverStage().show();
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    // Get users' key actions
    public void keyAction(KeyEvent event) {
        KeyCode key = event.getCode();
        System.out.println("keycode:" + key);
        if (key == KeyCode.A) {
            leftButtonAction();
        } else if (key == KeyCode.S) {
            downButtonAction();
        } else if (key == KeyCode.W) {
            upButtonAction();
        } else if (key == KeyCode.D) {
            rightButtonAction();
        }
    }

    // Operations for going the cat up
    public void upButtonAction() {
        printAction("UP");
        chara.setCharaDirection(MoveChara.TYPE_UP);
        chara.move(0, -1);
        afterMove();
    }

    // Operations for going the cat down
    public void downButtonAction() {
        printAction("DOWN");
        chara.setCharaDirection(MoveChara.TYPE_DOWN);
        chara.move(0, 1);
        afterMove();
    }

    // Operations for going the cat right
    public void leftButtonAction() {
        printAction("LEFT");
        chara.setCharaDirection(MoveChara.TYPE_LEFT);
        chara.move(-1, 0);
        afterMove();
    }

    // Operations for going the cat right
    public void rightButtonAction() {
        printAction("RIGHT");
        chara.setCharaDirection(MoveChara.TYPE_RIGHT);
        chara.move(1, 0);
        afterMove();
    }

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
        System.out.println("func2: 新マップ生成中");
        
        if (timer != null) {
            timer.stop();
        }

        // 新しいマップデータ作成
        mapData = new MapData(21, 15);

         // mapImageViews 再初期化
        mapImageViews = new ImageView[mapData.getHeight() * mapData.getWidth()];
        for (int y = 0; y < mapData.getHeight(); y++) {
            for (int x = 0; x < mapData.getWidth(); x++) {
                int index = y * mapData.getWidth() + x;
                mapImageViews[index] = mapData.getImageView(x, y);
            }
        }

        // キャラクターを初期位置に戻す
        chara = new MoveChara(1, 1, mapData);

        // アイテムをリセットして配置
        items.clear();
        itemByPos.clear();
        setupItems();

        // ゲームコンテキストのリセット
        gameCtx.speedMultiplier = 1;
        if(gameCtx.speedBoostLabel != null)
            gameCtx.speedBoostLabel.setVisible(false);
        if(gameCtx.keyInventoryIcon != null)
            gameCtx.keyInventoryIcon.setVisible(false);

        // マップ描画
        drawMap(chara, mapData);

        // タイマー再スタート
        startTimer();

         StageDB.getMainStage().show();
    }

　  @FXML
    public void func3ButtonAction(ActionEvent event) {
        System.out.println("func3: Nothing to do");
    }

    @FXML
    public void func4ButtonAction(ActionEvent event) {
        System.out.println("func4: Nothing to do");
    }

    // Print actions of user inputs
    public void printAction(String actionString) {
        System.out.println("Action: " + actionString);
    }

    private void startTimer(){
        if (timer != null){
            timer.stop();
        }

        remainingSeconds = 30;
        if (timerLabel != null){
            timerLabel.setText(String.valueOf(remainingSeconds));
        } else {
            System.out.println("timerLabel is null");
        }

        timer = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                remainingSeconds--;

                if (timerLabel != null){
                    timerLabel.setText(String.valueOf(remainingSeconds));
                }

                if (remainingSeconds <= 0){
                    timer.stop();
                    onTimeUp();
                }
            })
        );
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void onTimeUp(){
        try {
            System.out.println("Time Over");
            if(gameCtx != null && gameCtx.speedBoostTimeline !=null){
                gameCtx.speedBoostTimeline.stop();
                gameCtx.speedBoostTimeline = null;
                gameCtx.speedMultiplier =1;
                if(speedLabel != null)
                    speedLabel.setVisible(false);
            }
            StageDB.getMainStage().hide();
            StageDB.getMainSound().stop();
            StageDB.getGameOverSound().play();
            StageDB.getGameOverStage().show();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

}
