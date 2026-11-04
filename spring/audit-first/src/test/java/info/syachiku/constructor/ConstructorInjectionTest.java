package info.syachiku.constructor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConstructorInjectionTest {
    @Test
    void 候補が二つあると子クラスの引数を解決できない() {
        try (var context = contextWithCandidates()) {
            context.register(AmbiguousLogic.class);
            var failure = assertThrows(UnsatisfiedDependencyException.class, context::refresh);
            assertInstanceOf(NoUniqueBeanDefinitionException.class, failure.getMostSpecificCause());
        }
    }

    @Test
    void 引数のQualifierで選んだBeanが親の処理へ渡る() {
        try (var context = contextWithCandidates()) {
            context.register(SampleLogicImpl2.class);
            context.refresh();
            assertEquals("message:A", context.getBean(SampleLogicImpl2.class).run());
        }
    }

    private AnnotationConfigApplicationContext contextWithCandidates() {
        var context = new AnnotationConfigApplicationContext();
        context.registerBean("sampleUtilImpl", SampleUtil.class, () -> () -> "A");
        context.registerBean("sampleUtilBackup", SampleUtil.class, () -> () -> "B");
        context.registerBean(SampleClass.class);
        return context;
    }
}
