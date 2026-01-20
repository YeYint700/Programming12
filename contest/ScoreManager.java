public class ScoreManager {
    private static int Score = 0;
    public static int ItemCount = 0;

    public static int getScore() {
        return Score;
    }
    
    public static void scorebyTime(int remainingSeconds) {
        int timeScore= remainingSeconds * 1000;
    }
    
    public static void scorebyItem(int itemCount) {
        int itemScore= itemCount * 500;
    }
    
    public static void totalScore(int remainingSeconds, int itemCount) {
        Score = (remainingSeconds * 1000) + (itemCount * 500);
    }
    
    public static void resetScore() {
        Score = 0;
        ItemCount = 0;
    }
}