package info.syachiku.bean;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public final class BeanContexts {
    private BeanContexts() {}

    public static ClassPathXmlApplicationContext fromXml(String resource) {
        return new ClassPathXmlApplicationContext(resource);
    }

    public static GenericApplicationContext withoutXml() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        return context;
    }
}
