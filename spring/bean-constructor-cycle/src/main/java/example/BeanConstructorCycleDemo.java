package example;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public final class BeanConstructorCycleDemo {
    private BeanConstructorCycleDemo() {}

    public static void main(String[] args) {
        for (boolean allowCircularReferences : new boolean[] {false, true}) {
            System.out.printf("[constructor cycle allowCircularReferences=%s]%n",
                    allowCircularReferences);
            try (var context = prepareCycle(allowCircularReferences)) {
                context.refresh();
                throw new IllegalStateException("Expected the constructor cycle to stop startup");
            } catch (BeanCreationException failure) {
                for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
                    if (cause instanceof BeanCreationException creation) {
                        System.out.printf("%s bean=%s%n",
                                creation.getClass().getSimpleName(), creation.getBeanName());
                    }
                }
                System.out.println(failure.getMostSpecificCause().getMessage());
            }
        }

        try (var context = prepareWorkflow()) {
            context.refresh();
            var workflow = context.getBean(CheckoutWorkflow.class);
            Invoice invoice = workflow.issueInvoice("ORDER-42");
            System.out.printf("[workflow] order=%s totalYen=%d%n",
                    invoice.orderId(), invoice.totalYen());
        }
    }

    static AnnotationConfigApplicationContext prepareCycle(boolean allowCircularReferences) {
        var context = new AnnotationConfigApplicationContext();
        context.getDefaultListableBeanFactory()
                .setAllowCircularReferences(allowCircularReferences);
        context.registerBean("orderService", CyclicOrderService.class);
        context.registerBean("invoiceService", CyclicInvoiceService.class);
        return context;
    }

    static AnnotationConfigApplicationContext prepareWorkflow() {
        var context = new AnnotationConfigApplicationContext();
        context.getDefaultListableBeanFactory().setAllowCircularReferences(false);
        context.registerBean("orderService", OrderService.class);
        context.registerBean("invoiceService", InvoiceService.class);
        context.registerBean("checkoutWorkflow", CheckoutWorkflow.class);
        return context;
    }

    // Before: each service requires the other service at construction.
    public static final class CyclicOrderService {
        private final CyclicInvoiceService invoices;

        public CyclicOrderService(CyclicInvoiceService invoices) {
            this.invoices = invoices;
        }

        public Invoice issueInvoice(String orderId) {
            return invoices.issueFor(orderId);
        }

        public Order findOrder(String orderId) {
            return new Order(orderId, 1800);
        }
    }

    public static final class CyclicInvoiceService {
        private final CyclicOrderService orders;

        public CyclicInvoiceService(CyclicOrderService orders) {
            this.orders = orders;
        }

        public Invoice issueFor(String orderId) {
            Order order = orders.findOrder(orderId);
            return new Invoice(order.id(), order.totalYen());
        }
    }

    // After: the workflow retrieves the order and passes the actual value.
    public static final class CheckoutWorkflow {
        private final OrderService orders;
        private final InvoiceService invoices;

        public CheckoutWorkflow(OrderService orders, InvoiceService invoices) {
            this.orders = orders;
            this.invoices = invoices;
        }

        public Invoice issueInvoice(String orderId) {
            return invoices.issueFor(orders.findOrder(orderId));
        }
    }

    public static final class OrderService {
        public Order findOrder(String orderId) {
            return new Order(orderId, 1800);
        }
    }

    public static final class InvoiceService {
        public Invoice issueFor(Order order) {
            return new Invoice(order.id(), order.totalYen());
        }
    }

    public record Order(String id, int totalYen) {}

    public record Invoice(String orderId, int totalYen) {}
}
