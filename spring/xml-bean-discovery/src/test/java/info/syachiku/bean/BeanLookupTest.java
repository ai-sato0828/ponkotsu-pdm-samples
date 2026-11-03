package info.syachiku.bean;

import info.syachiku.inquiry.SomeService;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import static org.junit.jupiter.api.Assertions.*;

class BeanLookupTest {
    @Test void B01_xmlLoadsTheAnnotatedService() {
        try (var context = BeanContexts.fromXml("scan.xml")) {
            assertArrayEquals(new String[]{"someService"}, context.getBeanNamesForType(SomeService.class));
            assertEquals("Hello from SomeService!", context.getBean(SomeService.class).doSomething());
        }
    }

    @Test void B02_wrongScanPackageLeavesZeroCandidates() {
        try (var context = BeanContexts.fromXml("wrong-scan.xml")) {
            assertEquals(0, context.getBeanNamesForType(SomeService.class).length);
            assertThrowsExactly(NoSuchBeanDefinitionException.class, () -> context.getBean(SomeService.class));
        }
    }

    @Test void B03_annotationAloneDoesNotLoadXml() {
        try (var context = BeanContexts.withoutXml()) {
            assertEquals(0, context.getBeanNamesForType(SomeService.class).length);
            assertThrowsExactly(NoSuchBeanDefinitionException.class, () -> context.getBean(SomeService.class));
        }
    }

    @Test void B04_twoCandidatesCannotBeChosenByType() {
        try (var context = BeanContexts.fromXml("duplicate.xml")) {
            assertEquals(Set.of("serviceA", "serviceB"), Set.of(context.getBeanNamesForType(SomeService.class)));
            var error = assertThrowsExactly(NoUniqueBeanDefinitionException.class, () -> context.getBean(SomeService.class));
            assertEquals(2, error.getNumberOfBeansFound());
            assertEquals(Set.of("serviceA", "serviceB"), Set.copyOf(error.getBeanNamesFound()));
        }
    }

    @Test void B05_primaryChoosesOneWithoutDeletingTheOther() {
        try (var context = BeanContexts.fromXml("primary.xml")) {
            assertEquals(2, context.getBeanNamesForType(SomeService.class).length);
            assertSame(context.getBean("serviceA"), context.getBean(SomeService.class));
            assertNotSame(context.getBean("serviceA"), context.getBean("serviceB"));
        }
    }

    @Test void B06_explicitNameChoosesFromTheDuplicateConfiguration() {
        try (var context = BeanContexts.fromXml("duplicate.xml")) {
            assertEquals("Hello from SomeService!", context.getBean("serviceA", SomeService.class).doSomething());
            assertNotSame(context.getBean("serviceA"), context.getBean("serviceB"));
        }
    }
}
