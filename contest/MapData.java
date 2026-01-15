import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.ArrayDeque;
import java.util.Queue;

public class MapData {
    public static final int TYPE_SPACE = 0;
    public static final int TYPE_WALL = 1;
    public static final int TYPE_DOOR = 2;
    private static final String mapImageFiles[] = {
            "png/SPACE.png",
            "png/WALL.png",
            "png/door.png"
    };

    private Image[] mapImages;
    private ImageView[][] mapImageViews;
    private int[][] maps;
    private int width; // width of the map
    private int height; // height of the map
    private int goalX;
    private int goalY;//ゴールを追加

    MapData(int x, int y) {
        mapImages = new Image[mapImageFiles.length];
        mapImageViews = new ImageView[y][x];
        for (int i = 0; i < mapImageFiles.length; i ++) {
            mapImages[i] = new Image(mapImageFiles[i]);
        }

        width = x;
        height = y;
        maps = new int[y][x];

        fillMap(MapData.TYPE_WALL);
        digMap(1, 3);
        setFarthestGoal(1,1); // 最遠ゴール
        setImageViews();
    }

    // fill two-dimentional arrays with a given number (maps[y][x])
    private void fillMap(int type) {
        for (int y = 0; y < height; y ++) {
            for (int x = 0; x < width; x++) {
                maps[y][x] = type;
            }
        }
    }

    // dig walls for making roads
    private void digMap(int x, int y) {
        setMap(x, y, MapData.TYPE_SPACE);
        int[][] dl = { { 0, 1 }, { 0, -1 }, { -1, 0 }, { 1, 0 } };
        int[] tmp;

        for (int i = 0; i < dl.length; i ++) {
            int r = (int) (Math.random() * dl.length);
            tmp = dl[i];
            dl[i] = dl[r];
            dl[r] = tmp;
        }

        for (int i = 0; i < dl.length; i ++) {
            int dx = dl[i][0];
            int dy = dl[i][1];
            if (getMap(x + dx * 2, y + dy * 2) == MapData.TYPE_WALL) {
                setMap(x + dx, y + dy, MapData.TYPE_SPACE);
                digMap(x + dx * 2, y + dy * 2);
            }
        }
    }

    private void setFarthestGoal(int startX, int startY) {
        int[][] dist = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                dist[y][x] = -1;
            }
        }

        Queue<int[]> q = new ArrayDeque<>();
        q.add(new int[]{startX, startY});
        dist[startY][startX] = 0;

        int[][] dir = {{1,0},{-1,0},{0,1},{0,-1}};

        int maxDist = 0;
        goalX = startX;
        goalY = startY;

        while (!q.isEmpty()) {
            int[] p = q.poll();
            int x = p[0];
            int y = p[1];

            for (int[] d : dir) {
                int nx = x + d[0];
                int ny = y + d[1];

                if (getMap(nx, ny) == TYPE_SPACE && dist[ny][nx] == -1) {
                    dist[ny][nx] = dist[y][x] + 1;
                    q.add(new int[]{nx, ny});

                    if (dist[ny][nx] > maxDist) {
                        maxDist = dist[ny][nx];
                        goalX = nx;
                        goalY = ny;
                    }
                }
            }
        }

        // ゴールをドアにする
        maps[goalY][goalX] = TYPE_DOOR;
    }

    public int getMap(int x, int y) {
        if (x < 0 || width <= x || y < 0 || height <= y) {
            return -1;
        }
        return maps[y][x];
    }

    public void setMap(int x, int y, int type) {
        if (x < 1 || width <= x - 1 || y < 1 || height <= y - 1) {
            return;
        }
        maps[y][x] = type;
    }

    public ImageView getImageView(int x, int y) {
        return mapImageViews[y][x];
    }

    public void setImageViews() {
        for (int y = 0; y < height; y ++) {
            for (int x = 0; x < width; x++) {
                mapImageViews[y][x] = new ImageView(mapImages[maps[y][x]]);
            }
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public boolean isDoor(int x, int y){
        return getMap(x,y) == TYPE_DOOR;
    }

    public void openDoor(int x,int y){
        setMap(x,y,TYPE_SPACE);
    } 

    public int getGoalX() {
        return goalX;
    }

    public int getGoalY() {
        return goalY;
    }
    public void setGoal(int x, int y){
        goalX = x;
        goalY = y;
        maps[y][x] = TYPE_DOOR; // ゴールをドア扱いにする
}
