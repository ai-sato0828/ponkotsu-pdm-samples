public class MessageApp {

    private final String message;

    public MessageApp(String message) {
        this.message = message;
    }

    public static void main(String[] args) {
        MessageApp app = new MessageApp("Hello");
        app.printMessage();
    }

    public void printMessage() {
        System.out.println(message);
    }
}
