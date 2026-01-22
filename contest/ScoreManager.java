public class ScoreManager {
    private static int Score = 0;
    public static int ItemCount = 0;

    public static int getScore() {
        return Score;
    }
     public static void addScore(int s) {
        score += s;  
    }
    
    //合計得点
    public static void totalScore(int remainingSeconds, int itemCount) {
        Score = (remainingSeconds * 100) + (itemCount * 300);
    }
    //得点のリセット
    public static void resetScore() {
        Score = 0;
        ItemCount = 0;
    }
}
