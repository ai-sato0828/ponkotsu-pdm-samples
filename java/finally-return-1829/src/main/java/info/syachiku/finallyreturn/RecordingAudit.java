package info.syachiku.finallyreturn;

// この教材の記録器。外部ログ送信や失敗する後処理をモデル化しない。
public final class RecordingAudit {
    private int calls;
    private int caughtCalls;

    public void record() {
        calls++;
    }

    public void recordCatch() {
        caughtCalls++;
    }

    public int calls() {
        return calls;
    }

    public int caughtCalls() {
        return caughtCalls;
    }
}
