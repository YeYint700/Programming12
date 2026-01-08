import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.Objects;


public class GameItem {
    //アイテムの種類
    //enumは列挙型クラス
    public enum ItemType{
        SPEEDBOOSTER,
        KEY
    }
    //アイテムを拾ったときの効果適用に必要な状態
    public static class GameContext{
        //SPEED
        public int speedMultiplier =1;
        public Label speedBoostLabel;//スピード強化の残り秒数を表示するラベル
        public Timeline speedBoostTimeline;//スピード強化の残り時間管理用
        public int speedBoostRemainingSeconds=0;//残り秒数
        //KEY
        public boolean hasKey = false;//キーを保有
        public ImageView keyInventoryIcon;//残り時間ラベルの下などに表示するキー画像
    }
    //１アイテムの属性
    private final int x;
    private final int y;
    private final ItemType type;
    private final ImageView imageView;//迷路上に表示するアイテム画像
    private boolean picked =false;//取得済みアイテムは迷路上で非表示

    //コンストラクタ
    //座標とアイテムの種類だけを指定してそれに応じた画像を用意
    public GameItem(int x,int y,ItemType type){
        this.x=x;
        this.y=y;
        this.type=Objects.requireNonNull(type,"ItemType must not be null");
        this.imageView=createDefaultImageView(type);
    }
    //座標や種類に加えて任意のImageViewを外から渡して使える（見た目を変更するとき、場面によって切り替えるとき）
    public GameItem(int x,int y, ItemType type, ImageView customImageView){
        this.x=x;
        this.y=y;
        this.type=Objects.requireNonNull(type,"ItemType must not be null");
        this.imageView = Objects.requireNonNull(customImageView,"ImageView must not be null");
    }
    
    //画像生成
    private ImageView createDefaultImageView(ItemType type){
        String url;
        switch ( (type)) {
            case SPEEDBOOSTER:
                url="/png/speedbooster.png";
                break;
            case KEY:
                url="/png/key.png";
                break;
            default:
                //画像が見つからない場合
                throw new IllegalArgumentException("Unknown ItemType: " + type);
        }
        ImageView iv =new ImageView(new Image(url));
        //サイズを調整
        iv.setFitWidth(32);
        iv.setFitHeight(32);
        iv.setPreserveRatio(true);
        return iv;
    }
    //ゲッター類
    public int getX(){return x;}
    public int getY(){return y;}
    public ItemType getType(){return type;}
    public ImageView getImageView(){return imageView;}
    public boolean isPicked(){return picked;}
    public String posKey(){return x + "_"+y;}

    //効果適用
    public void applyEffect(GameContext ctx){
        if(picked)
            return;//すでに取得済みなら何もしない
        switch (type) {
            case SPEEDBOOSTER:
                applySpeedBooster(ctx);
                break;
            case KEY:
                applyKey(ctx);
                break;
        }
        //取得後にアイテムは非表示
        picked = true;
    }

    //Speedbooster:10病患　移動スピード　２倍
    //重複した場合は残り時間をリセット
    //スピードアップ中はHPの右に「Spped up:残り秒数」
    //０になった瞬間にラベルを消し、１倍に戻す
    private void applySpeedBooster(GameContext ctx){
        startOrResetSpeedBoost(ctx, 10);//10秒
        System.out.println("Speedbooster: speed X2 for 10s(reset if already active).");
    }

    private void startOrResetSpeedBoost(GameContext ctx, int seconds){
        //既存のタイムラインがあれば停止
        if(ctx.speedBoostTimeline != null){
            ctx.speedBoostTimeline.stop();
            ctx.speedBoostTimeline=null;
        }

        ctx.speedMultiplier=2;
        ctx.speedBoostRemainingSeconds=seconds;

        //ラベル表示更新
        if(ctx.speedBoostLabel != null){
            ctx.speedBoostLabel.setVisible(true);
            ctx.speedBoostLabel.setText("Speed up: "+ctx.speedBoostRemainingSeconds);
        }

        //１秒毎に残り時間を更新して０病で終了
        ctx.speedBoostTimeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                ctx.speedBoostRemainingSeconds--;
                if(ctx.speedBoostLabel!= null){
                    if(ctx.speedBoostRemainingSeconds>0){
                        ctx.speedBoostLabel.setText("Speed up: "+ctx.speedBoostRemainingSeconds);
                    }else{
                        //0になったタイミングでラベルを非表示
                        ctx.speedBoostLabel.setVisible(false);
                    }
                }
                if(ctx.speedBoostRemainingSeconds <=0){
                    //効果終了
                    ctx.speedMultiplier=1;
                    if(ctx.speedBoostTimeline!=null){
                        ctx.speedBoostTimeline.stop();
                        ctx.speedBoostTimeline=null;
                    }
                }
            })
        );
        ctx.speedBoostTimeline.setCycleCount(Timeline.INDEFINITE);
        ctx.speedBoostTimeline.play();
    }

    //Key: 取得して表示
    //取得したらタイマーの下に画像を表示
    private void applyKey(GameContext ctx){
        ctx.hasKey=true;
        if(ctx.keyInventoryIcon != null){
            ctx.keyInventoryIcon.setVisible(true);
        }
        System.out.println("Key: obtained");
    }

    //ドア到達時にキーを消費
    //キーを取得済みなら消費してアイコンを被表示にしてドアを開けられる
    //キーは取得できなかったら何も起きない
    public static boolean tryUseKey(GameContext ctx){
        if(ctx.hasKey){
            ctx.hasKey=false;
            if(ctx.keyInventoryIcon !=null){
                ctx.keyInventoryIcon.setVisible(false);
            }
            System.out.println("Key: used (door opend)");
            return true;
        } else{
            System.out.println("Key: not owned (cannot open door)");
            return false;
        }
    }
}