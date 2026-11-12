package info.syachiku.hashmap;

import java.util.Objects;

public record ScoreResult(ScoreStatus status, Integer score) {
    public ScoreResult {
        Objects.requireNonNull(status, "status");
        if (status == ScoreStatus.GRADED && score == null) {
            throw new IllegalArgumentException("採点済みには点数が必要です");
        }
        if (status == ScoreStatus.NOT_GRADED && score != null) {
            throw new IllegalArgumentException("採点前には点数を持たせません");
        }
    }

    public static ScoreResult graded(int score) {
        return new ScoreResult(ScoreStatus.GRADED, score);
    }

    public static ScoreResult notGraded() {
        return new ScoreResult(ScoreStatus.NOT_GRADED, null);
    }
}
