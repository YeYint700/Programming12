import javafx.application.Application;
import javafx.stage.Stage;

public class MapGame extends Application {
  Stage stage;

  @Override
  public void start(Stage primaryStage) throws Exception {
    stage = primaryStage;
    stage.hide();
    StageDB.setMainClass(getClass());
    StageDB.getMainStage().show();
    StageDB.getMainSound().play();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
