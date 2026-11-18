package example.orders;

public class OrderTotal {
    public static void main(String[] args) {
        int total = OrderLine.subtotal(1200, 2) + OrderLine.subtotal(350, 1);
        System.out.println(total);
    }
}
