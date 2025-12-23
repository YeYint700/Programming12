import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class settei3 {
    public void func2(ActionEvent actionEvent) {
        Systemout.println("func2:Generate New Map");

        mapDate = new MapDate(21,15);

        chara.setCharaDir(MoveChara.TYPE_RIGHT); 
    chara.setIndex(1, 1);  
    
    drawMap(mapData, chara);
    }
}
