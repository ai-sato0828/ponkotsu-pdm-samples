package info.syachiku.field;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FieldInjectionTest {
    @Test
    void 候補が二つあると型だけでは注入先を決められない() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(SampleLogicImpl.class, SampleLogicImpl2.class, AmbiguousController.class);
            var failure = assertThrows(UnsatisfiedDependencyException.class, context::refresh);
            assertInstanceOf(NoUniqueBeanDefinitionException.class, failure.getMostSpecificCause());
        }
    }

    @Test
    void 登録名とQualifierが一致すると二つ目の実装が呼ばれる() {
        try (var context = new AnnotationConfigApplicationContext(
                SampleLogicImpl.class, SampleLogicImpl2.class, SampleController.class)) {
            assertEquals("B", context.getBean(SampleController.class).execute());
        }
    }
}
