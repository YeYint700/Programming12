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
   StageDB.getMainStage().setHeight(600);  // ゲーム画面復帰時に明示的にサイズ変更
   StageDB.getMainStage().setWidth(692);   // ゲーム画面復帰時に明示的にサイズ変更
   StageDB.getMainSound().play();
  } catch (Exception ex) {
   System.out.println(ex.getMessage());
  }
 }
}
