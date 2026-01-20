public class ScoreManager {
    private static int Score = 0;
    public static int ItemCount = 0;

    public static int getScore() {
        return Score;
    }
    //残り時間に応じた得点
    public static void scorebyTime(int remainingSeconds) {
        int timeScore= remainingSeconds * 1000;
    }
    //アイテムの取得数に応じた得点
    public static void scorebyItem(int itemCount) {
        int itemScore= itemCount * 500;
    }
    //合計得点
    public static void totalScore(int remainingSeconds, int itemCount) {
        Score = (remainingSeconds * 1000) + (itemCount * 500);
    }
    //得点のリセット
    public static void resetScore() {
        Score = 0;
        ItemCount = 0;
    }
}