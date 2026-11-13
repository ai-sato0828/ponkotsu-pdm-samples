package info.syachiku.finallyreturn;

// ローカルの保存モデル。DBやネット接続は使わない。
public final class RecordingOrderRepository {
    private final RuntimeException saveFailure;
    private int saveCalls;
    private int savedCount;

    public RecordingOrderRepository(RuntimeException saveFailure) {
        this.saveFailure = saveFailure; // nullは「保存成功」のテスト入力。
    }

    public void save() {
        saveCalls++;
        if (saveFailure != null) {
            throw saveFailure;
        }
        savedCount++;
    }

    public int saveCalls() {
        return saveCalls;
    }

    public int savedCount() {
        return savedCount;
    }
}
