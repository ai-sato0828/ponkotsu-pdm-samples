package info.syachiku.rollback;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ExtendWith(OutputCaptureExtension.class)
class RollbackContractTest {
    @Autowired
    private OrderRegistration orders;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void 内側の失敗をcatchしても外側コミットで失敗し両方残らない(CapturedOutput output) {
        assertThatThrownBy(() -> orders.registerAndCatch("caught", true))
                .isInstanceOf(UnexpectedRollbackException.class)
                .hasMessageContaining("rollback-only");

        assertThat(output).contains("outer caught review failure; rollbackOnly=true")
                .contains("outer method reached its return");
        assertThat(orderStatuses("caught")).isEmpty();
        assertThat(reviewResults("caught")).isEmpty();
    }

    @Test
    void 審査が必須なら元の失敗を呼び出し元へ返し両方を取り消す() {
        assertThatThrownBy(() -> orders.registerWithRequiredReview("mandatory-fail", true))
                .isInstanceOf(ReviewFailedException.class)
                .hasMessage("審査処理に失敗");

        assertThat(orderStatuses("mandatory-fail")).isEmpty();
        assertThat(reviewResults("mandatory-fail")).isEmpty();
    }

    @Test
    void 必須の審査に成功すれば注文と審査結果を両方保存する() {
        orders.registerWithRequiredReview("mandatory-ok", false);

        assertThat(orderStatuses("mandatory-ok")).containsExactly("REVIEWED");
        assertThat(reviewResults("mandatory-ok")).containsExactly("CHECKED");
    }

    @Test
    void 審査を分けて失敗を許す契約なら注文だけを保留で残す() {
        orders.registerWithIndependentReview("review-fail", true, false);

        assertThat(orderStatuses("review-fail")).containsExactly("PENDING");
        assertThat(reviewResults("review-fail")).isEmpty();
    }

    @Test
    void 外側が後から失敗しても独立して確定した審査結果は残る() {
        assertThatThrownBy(() -> orders.registerWithIndependentReview("order-fail", false, true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("注文登録の後続処理に失敗");

        assertThat(orderStatuses("order-fail")).isEmpty();
        assertThat(reviewResults("order-fail")).containsExactly("CHECKED");
    }

    private List<String> orderStatuses(String requestId) {
        return jdbc.queryForList("select status from order_registration where request_id = ?",
                String.class, requestId);
    }

    private List<String> reviewResults(String requestId) {
        return jdbc.queryForList("select result from review_record where request_id = ?",
                String.class, requestId);
    }
}
