package example.logging;

import java.util.List;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.read.ListAppender;

public class LoggerAppenderDemo {
    private static final String LOGGER_NAME = "example.shipping.PriceLookup";

    public static void main(String[] args) {
        print("INHERITED", inheritedInfo());
        print("CHILD_DEBUG", explicitDebug());
        print("THRESHOLD_INFO", debugWithInfoThreshold());
    }

    static Result inheritedInfo() { return capture(false, false); }
    static Result explicitDebug() { return capture(true, false); }
    static Result debugWithInfoThreshold() { return capture(true, true); }

    private static Result capture(boolean explicitDebug, boolean infoThreshold) {
        LoggerContext context = new LoggerContext();
        context.setMDCAdapter(new LogbackMDCAdapter());
        context.start();
        try {
            Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
            root.setLevel(Level.INFO);
            Logger child = context.getLogger(LOGGER_NAME);
            child.setLevel(explicitDebug ? Level.DEBUG : null);
            child.setAdditive(true);

            ListAppender<ILoggingEvent> target = new ListAppender<>();
            target.setName("root-target");
            target.setContext(context);
            if (infoThreshold) {
                ThresholdFilter filter = new ThresholdFilter();
                filter.setContext(context);
                filter.setLevel("INFO");
                filter.start();
                target.addFilter(filter);
            }
            target.start();
            root.addAppender(target);

            boolean debugEnabled = child.isDebugEnabled();
            child.debug("lookup started sku={}", "SKU-001");
            child.info("lookup complete sku={}", "SKU-001");
            List<String> events = target.list.stream()
                    .map(event -> event.getLevel() + ":" + event.getFormattedMessage())
                    .toList();
            return new Result(root.getLevel().toString(),
                    child.getLevel() == null ? "INHERITED" : child.getLevel().toString(),
                    child.getEffectiveLevel().toString(), debugEnabled, List.copyOf(events));
        } finally {
            context.stop();
        }
    }

    private static void print(String label, Result result) {
        System.out.printf("%s root=%s child=%s effective=%s debugEnabled=%s events=%s%n",
                label, result.rootLevel(), result.childLevel(), result.effectiveLevel(),
                result.debugEnabled(), result.events());
    }

    record Result(String rootLevel, String childLevel, String effectiveLevel,
                  boolean debugEnabled, List<String> events) {}
}
