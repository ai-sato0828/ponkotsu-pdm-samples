package info.syachiku.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RequiredNameValidator implements ConstraintValidator<RequiredName, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        // 業務仕様: Javaが空白またはスペースと判定する文字だけの氏名は受け付けない。
        // 値の前後や途中にある空白を削除する処理ではない。
        return value.codePoints().anyMatch(codePoint ->
                !Character.isWhitespace(codePoint) && !Character.isSpaceChar(codePoint));
    }
}
