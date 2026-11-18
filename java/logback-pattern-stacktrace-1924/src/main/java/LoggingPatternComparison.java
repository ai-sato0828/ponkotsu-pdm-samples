import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.read.ListAppender;

public class LoggingPatternComparison {
    static final String HIDDEN_PATTERN = "%msg%n%nopex";
    static final String EXPLICIT_PATTERN = "%msg%n%ex";
    static final String DEFAULT_PATTERN = "%msg%n";

    public static void main(String[] args) {
        Comparison comparison = compare();
        System.out.println("event throwable = " + comparison.throwableType());
        print(HIDDEN_PATTERN, comparison.hidden());
        print(EXPLICIT_PATTERN, comparison.explicit());
        print(DEFAULT_PATTERN, comparison.automatic());
    }

    static Comparison compare() {
        LoggerContext context = new LoggerContext();
        context.setMDCAdapter(new LogbackMDCAdapter());
        context.start();
        try {
            Logger logger = context.getLogger("inventory.example");
            logger.setLevel(Level.ERROR);
            logger.setAdditive(false);
            ListAppender<ILoggingEvent> capture = new ListAppender<>();
            capture.setContext(context);
            capture.start();
            logger.addAppender(capture);

            try {
                saveInventory();
            } catch (IllegalStateException exception) {
                logger.error("在庫の保存に失敗 id={}", "I-17", exception);
            }

            ILoggingEvent event = capture.list.get(0);
            IThrowableProxy throwable = event.getThrowableProxy();
            return new Comparison(
                    throwable == null ? "none" : throwable.getClassName(),
                    render(context, event, HIDDEN_PATTERN),
                    render(context, event, EXPLICIT_PATTERN),
                    render(context, event, DEFAULT_PATTERN));
        } finally {
            context.stop();
        }
    }

    private static void saveInventory() {
        throw new IllegalStateException("stock write failed");
    }

    private static String render(LoggerContext context, ILoggingEvent event, String pattern) {
        PatternLayout layout = new PatternLayout();
        layout.setContext(context);
        layout.setPattern(pattern);
        layout.start();
        return layout.doLayout(event);
    }

    private static void print(String pattern, String output) {
        System.out.println("[" + pattern + "]");
        output.lines().limit(3).forEach(System.out::println);
    }

    record Comparison(String throwableType, String hidden, String explicit, String automatic) {}
}
