import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.fxml.FXML;

public class MapGameClearController {
    
    @FXML
    private Label scoreLabel;

    @FXML
    public void initialize() {
            int score = ScoreManager.getScore();
            scoreLabel.setText("Score: " + score);
    }

    public void onRetry(ActionEvent event) {
        try {
            StageDB.resetMainStage();
            StageDB.getGameClearStage().hide();

            StageDB.getMainStage().show();
            StageDB.getMainSound().play();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void onExit(ActionEvent event) {
        System.exit(0);
    }
}
