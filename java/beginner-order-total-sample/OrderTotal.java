public class OrderTotal {
    public static void main(String[] args) {
        int unitPrice = 500;   // 1個の値段
        int quantity = 2;      // 購入する個数
        int maxQuantity = 10;  // 購入できる上限

        if (quantity < 1 || quantity > maxQuantity) {
            System.out.println("1〜" + maxQuantity + "個で入力");
        } else {
            int total = unitPrice * quantity;
            System.out.println(total + "円");
        }
    }
}
