package info.syachiku.registration.orders;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = OrderSampleApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.open-in-view=false"})
class OrderRegistrationTest {
    @Autowired
    private ApplicationContext context;

    @Test
    void ステレオタイプの三つのクラスとSpringDataのインターフェースがBeanになる() {
        assertThat(context.getBeansOfType(OrderCsvParser.class)).hasSize(1);
        assertThat(context.getBeansOfType(OrderService.class)).hasSize(1);
        assertThat(context.getBeansOfType(OrderQueryRepository.class)).hasSize(1);
        assertThat(context.getBeansOfType(OrderRow.class)).isEmpty();
        assertThat(AopUtils.isAopProxy(context.getBean(OrderRepository.class))).isTrue();
    }

    @Test
    void 入力変換と業務処理を通して保存し検索用RepositoryでDBから状態を読める() {
        var service = context.getBean(OrderService.class);
        long id = service.place("order-42,READY");

        assertThat(service.findStatus(id)).isEqualTo("READY");
        assertThatThrownBy(() -> context.getBean(OrderQueryRepository.class).findStatus(-1))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }
}
