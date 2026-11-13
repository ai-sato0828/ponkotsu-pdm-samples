package info.syachiku.finallyreturn;

@FunctionalInterface
public interface NameRepository {
    // 戻り値は非null。取得失敗はRuntimeException。nullは契約違反。
    String loadName();
}
