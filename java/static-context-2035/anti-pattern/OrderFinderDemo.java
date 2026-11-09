public class OrderFinderDemo {
    interface Repository { String find(long id); }

    static final class OrderFinder {
        private static Repository repository;
        OrderFinder(Repository repository) { OrderFinder.repository = repository; }
        static String find(long id) { return repository.find(id); }
    }

    public static void main(String[] args) {
        new OrderFinder(id -> "A-" + id);
        System.out.println("first=" + OrderFinder.find(1));
        new OrderFinder(id -> "B-" + id);
        System.out.println("afterSecond=" + OrderFinder.find(1));
    }
}
