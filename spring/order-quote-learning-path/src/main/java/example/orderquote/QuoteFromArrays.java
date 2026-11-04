package example.orderquote;

/** 学習用。個数・単価の配列が対応し、値は有効だと仮定する。 */
public final class QuoteFromArrays {
    public static void main(String[] args) {
        int[] quantities = {2, 1};
        int[] unitPrices = {1200, 350};

        long total = 0;
        for (int index = 0; index < quantities.length; index++) {
            total += (long) quantities[index] * unitPrices[index];
        }
        System.out.println(total); // 2750
    }
}
