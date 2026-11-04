package example.paging;

public final class PageIndex {
    private PageIndex() {}

    public static int toZeroBased(int requestedPage) {
        if (requestedPage < 1) {
            throw new IllegalArgumentException("ページ番号は1以上を指定してください");
        }
        return requestedPage - 1;
    }
}
