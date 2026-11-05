public class OrderTotal {
    public static void main(String[] args) {
        int[] linePrices = {500, 700, 300};
        int[] emptyPrices = {};
        int[] onePrice = {500};
        System.out.println("3件: " + sumBefore(linePrices) + " / " + sumAfter(linePrices));
        System.out.println("0件: " + sumBefore(emptyPrices) + " / " + sumAfter(emptyPrices));
        System.out.println("1件: " + sumBefore(onePrice) + " / " + sumAfter(onePrice));
    }

    static int sumBefore(int[] linePrices) {
        int total = 0;
        for (int i = 0; i < linePrices.length; i++) {
            total = linePrices[i];
        }
        return total;
    }

    static int sumAfter(int[] linePrices) {
        int total = 0;
        for (int i = 0; i < linePrices.length; i++) {
            total += linePrices[i];
        }
        return total;
    }
}
