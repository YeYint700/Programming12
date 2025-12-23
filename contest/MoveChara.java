import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.AnimationTimer;

import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class MoveChara {
    public static final int TYPE_DOWN = 0;
    public static final int TYPE_LEFT = 1;
    public static final int TYPE_RIGHT = 2;
    public static final int TYPE_UP = 3;

    private final String[] directions = { "Down", "Left", "Right", "Up" };
    private final String[] animationNumbers = { "1", "2", "3" };

    private static final int ROWS = 4;
    private static final int COLS = 3;

    public boolean useSpriteSheet = true;
    private static final String SPRITE_SHEET_PATH = "./png/dog.png";
    private final String pngPathPre = "png/cat";
    private final String pngPathSuf = ".png";

    private int posX;
    private int posY;

    private MapData mapData;

    private Image[][] charaImages;
    private ImageView[] charaImageViews;
    private ImageAnimation[] charaImageAnimations;

    private int charaDirection;

    MoveChara(int startX, int startY, MapData mapData) {
        this.mapData = mapData;
        charaImages = new Image[ROWS][COLS];
        charaImageViews = new ImageView[ROWS];
        charaImageAnimations = new ImageAnimation[ROWS];

        if (useSpriteSheet) {
            Image spriteSheet = new Image(SPRITE_SHEET_PATH);
            double sheetWidth = spriteSheet.getWidth();
            double sheetHeight = spriteSheet.getHeight();

            double frameWidthD = sheetWidth / COLS;
            double frameHeightD = sheetHeight / ROWS;
            int frameWidth = (int) frameWidthD;
            int frameHeight = (int) frameHeightD;
            
            PixelReader reader = spriteSheet.getPixelReader();

            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    int x = (int) Math.round(col * frameWidthD);
                    int y = (int) Math.round(row * frameHeightD);
                    WritableImage  cut = new WritableImage(reader, x, y, frameWidth, frameHeight);
                    charaImages[row][col] = resizeTo32(cut);
                }
                charaImageViews[row] = new ImageView(charaImages[row][0]);
                charaImageAnimations[row] =
                        new ImageAnimation(charaImageViews[row], charaImages[row]);
            }
        } else {
            for (int i = 0; i < 4; i++) {
                charaImages[i] = new Image[3];
                for (int j = 0; j < 3; j++) {
                    charaImages[i][j] = new Image(
                            pngPathPre + directions[i] + animationNumbers[j] + pngPathSuf);
                }
                charaImageViews[i] = new ImageView(charaImages[i][0]);
                charaImageAnimations[i] = new ImageAnimation(
                        charaImageViews[i], charaImages[i]);
            }
        }

        posX = startX;
        posY = startY;

        setCharaDirection(TYPE_RIGHT); // start with right-direction
    }

    // resize image to 32*32.
    private Image resizeTo32(Image img) {

        double w = img.getWidth();
        double h = img.getHeight();
        double scale = 32.0 / Math.max(w, h); 
        double newW = w * scale;
        double newH = h * scale;

        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);
        iv.setFitWidth(newW);
        iv.setFitHeight(newH);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        Image scaled = iv.snapshot(params, null);

        WritableImage out = new WritableImage(32, 32);
        var pw = out.getPixelWriter();
        var pr = scaled.getPixelReader();

        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                pw.setColor(x, y, Color.TRANSPARENT);
            }
        }

        int offsetX = (32 - (int)newW) / 2;
        int offsetY = (32 - (int)newH) / 2;

        for (int y = 0; y < (int)newH; y++) {
            for (int x = 0; x < (int)newW; x++) {
                pw.setColor(offsetX + x, offsetY + y, pr.getColor(x, y));
            }
        }

        return out;
    }

    // set the cat's direction
    public void setCharaDirection(int cd) {
        charaDirection = cd;
        for (int i = 0; i < 4; i++) {
            if (i == charaDirection) {
                charaImageAnimations[i].start();
            } else {
                charaImageAnimations[i].stop();
            }
        }
    }

    // check whether the cat can move on
    private boolean isMovable(int dx, int dy) {
        if (mapData.getMap(posX + dx, posY + dy) == MapData.TYPE_WALL) {
            return false;
        } else if (mapData.getMap(posX + dx, posY + dy) == MapData.TYPE_SPACE) {
            return true;
        }
        return false;
    }

    // move the cat
    public boolean move(int dx, int dy) {
        if (isMovable(dx, dy)) {
            posX += dx;
            posY += dy;
	    System.out.println("chara[X,Y]:" + posX + "," + posY);
            return true;
        } else {
            return false;
        }
    }

    // getter: direction of the cat
    public ImageView getCharaImageView() {
        return charaImageViews[charaDirection];
    }

    // getter: x-positon of the cat
    public int getPosX() {
        return posX;
    }

    // getter: y-positon of the cat
    public int getPosY() {
        return posY;
    }

    // Show the cat animation
    private class ImageAnimation extends AnimationTimer {

        private ImageView charaView = null;
        private Image[] charaImages = null;
        private int index = 0;

        private long duration = 500 * 1000000L; // 500[ms]
        private long startTime = 0;

        private long count = 0L;
        private long preCount;
        private boolean isPlus = true;

        public ImageAnimation(ImageView charaView, Image[] images) {
            this.charaView = charaView;
            this.charaImages = images;
            this.index = 0;
        }

        @Override
        public void handle(long now) {
            if (startTime == 0) {
                startTime = now;
            }

            preCount = count;
            count = (now - startTime) / duration;
            if (preCount != count) {
                if (isPlus) {
                    index++;
                } else {
                    index--;
                }
                if (index < 0 || 2 < index) {
                    index = 1;
                    isPlus = !isPlus; // true == !false, false == !true
                }
                charaView.setImage(charaImages[index]);
            }
        }
    }
}
