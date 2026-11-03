package info.syachiku.bean;

import info.syachiku.inquiry.SomeService;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;

public final class BeanLookupDemo {
    public static void main(String[] args) {
        show("B01 scan", BeanContexts.fromXml("scan.xml"));
        show("B02 wrong scan", BeanContexts.fromXml("wrong-scan.xml"));
        show("B03 XML unread", BeanContexts.withoutXml());
        show("B04 duplicate", BeanContexts.fromXml("duplicate.xml"));
        show("B05 primary", BeanContexts.fromXml("primary.xml"));
    }

    private static void show(String label, ConfigurableApplicationContext context) {
        try (context) {
            int count = context.getBeanNamesForType(SomeService.class).length;
            try {
                SomeService service = context.getBean(SomeService.class);
                System.out.println(label + ": candidates=" + count + ", " + service.doSomething());
            } catch (NoUniqueBeanDefinitionException e) {
                System.out.println(label + ": candidates=" + count + ", NoUniqueBeanDefinitionException");
            } catch (NoSuchBeanDefinitionException e) {
                System.out.println(label + ": candidates=" + count + ", NoSuchBeanDefinitionException");
            }
        }
    }
}
