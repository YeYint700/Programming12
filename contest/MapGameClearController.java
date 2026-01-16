import javafx.event.ActionEvent;

public class MapGameClearController {

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
