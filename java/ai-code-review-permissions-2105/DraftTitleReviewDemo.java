public class DraftTitleReviewDemo {
    enum Status { DRAFT, RECEIVED, IN_PROGRESS, COMPLETED }
    enum EditResult { UPDATED, REJECTED, SAVE_FAILED }

    record Inquiry(long authorId, Status status, String title) {}

    interface TitleStore {
        void save(Inquiry inquiry) throws SaveFailure;
    }

    static final class SaveFailure extends Exception {}

    static EditResult editBefore(Inquiry inquiry, long actorId, String title, TitleStore store) {
        if (!validTitle(title)) {
            return EditResult.REJECTED;
        }
        return persist(inquiry, title, store);
    }

    static EditResult editAfter(Inquiry inquiry, long actorId, String title, TitleStore store) {
        if (!validTitle(title) || actorId != inquiry.authorId() || inquiry.status() != Status.DRAFT) {
            return EditResult.REJECTED;
        }
        return persist(inquiry, title, store);
    }

    private static boolean validTitle(String title) {
        if (title == null) {
            return false;
        }
        int length = title.codePointCount(0, title.length());
        return length >= 1 && length <= 100;
    }

    private static EditResult persist(Inquiry inquiry, String title, TitleStore store) {
        try {
            store.save(new Inquiry(inquiry.authorId(), inquiry.status(), title));
            return EditResult.UPDATED;
        } catch (SaveFailure failure) {
            return EditResult.SAVE_FAILED;
        }
    }

    private static final class RecordingStore implements TitleStore {
        private final boolean shouldFail;
        private Inquiry saved;
        private int attempts;
        private int savedCount;

        RecordingStore(Inquiry initial, boolean shouldFail) {
            this.saved = initial;
            this.shouldFail = shouldFail;
        }

        @Override
        public void save(Inquiry inquiry) throws SaveFailure {
            attempts++;
            if (shouldFail) {
                throw new SaveFailure();
            }
            saved = inquiry;
            savedCount++;
        }
    }

    public static void main(String[] args) {
        verify("自分の下書き・1文字", Status.DRAFT, 7L, "件", false, EditResult.UPDATED, EditResult.UPDATED);
        verify("自分の下書き・100文字", Status.DRAFT, 7L, "件".repeat(100), false, EditResult.UPDATED, EditResult.UPDATED);
        verify("件名なし", Status.DRAFT, 7L, null, false, EditResult.REJECTED, EditResult.REJECTED);
        verify("空の件名", Status.DRAFT, 7L, "", false, EditResult.REJECTED, EditResult.REJECTED);
        verify("101文字", Status.DRAFT, 7L, "件".repeat(101), false, EditResult.REJECTED, EditResult.REJECTED);
        verify("他人の下書き", Status.DRAFT, 9L, "件", false, EditResult.UPDATED, EditResult.REJECTED);
        verify("自分の受付済み", Status.RECEIVED, 7L, "件", false, EditResult.UPDATED, EditResult.REJECTED);
        verify("自分の対応中", Status.IN_PROGRESS, 7L, "件", false, EditResult.UPDATED, EditResult.REJECTED);
        verify("自分の完了", Status.COMPLETED, 7L, "件", false, EditResult.UPDATED, EditResult.REJECTED);
        verify("自分の下書き・保存失敗", Status.DRAFT, 7L, "件", true, EditResult.SAVE_FAILED, EditResult.SAVE_FAILED);
    }

    private static void verify(String name, Status status, long actorId, String title, boolean shouldFail,
                               EditResult expectedBefore, EditResult expectedAfter) {
        var inquiry = new Inquiry(7L, status, "変更前の件名");
        var beforeStore = new RecordingStore(inquiry, shouldFail);
        var afterStore = new RecordingStore(inquiry, shouldFail);
        EditResult before = editBefore(inquiry, actorId, title, beforeStore);
        EditResult after = editAfter(inquiry, actorId, title, afterStore);
        check(name + "（修正前）", before, expectedBefore, beforeStore, inquiry, title);
        check(name + "（修正後）", after, expectedAfter, afterStore, inquiry, title);
        System.out.printf("%s | 修正前=%s | 修正後=%s%n", name, summary(before, beforeStore), summary(after, afterStore));
    }

    private static void check(String name, EditResult actual, EditResult expected, RecordingStore store,
                              Inquiry original, String requestedTitle) {
        int expectedAttempts = expected == EditResult.REJECTED ? 0 : 1;
        int expectedSaves = expected == EditResult.UPDATED ? 1 : 0;
        var expectedStored = expectedSaves == 0 ? original
                : new Inquiry(original.authorId(), original.status(), requestedTitle);
        if (actual != expected || store.attempts != expectedAttempts || store.savedCount != expectedSaves
                || !store.saved.equals(expectedStored)) {
            throw new AssertionError(name + ": " + summary(actual, store) + ", expected " + expected);
        }
    }

    private static String summary(EditResult result, RecordingStore store) {
        return "%s 試行=%d 保存=%d 成功表示=%b".formatted(result, store.attempts, store.savedCount,
                result == EditResult.UPDATED);
    }
}
