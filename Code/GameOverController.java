
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class GameOverController {

    @FXML
    void onGameOverAction(ActionEvent event) {
        try {
            StageDB.getGameOverStage().hide();
            StageDB.getMainSound().stop();

            StageDB.resetMainStage();
            StageDB.getMainStage().show();
            StageDB.getMainSound().play();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
//Hello
