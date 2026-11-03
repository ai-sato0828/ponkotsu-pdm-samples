package info.syachiku.samples.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class MoneyComparisonDemo {
    public static void main(String[] args) {
        BigDecimal left = new BigDecimal("1200.0");
        BigDecimal right = new BigDecimal("1200.00");
        System.out.println("equals=" + left.equals(right));
        System.out.println("sameAmount=" + MoneyRules.sameAmount(left, right));
        System.out.println("scales=" + left.scale() + "," + right.scale());
        Set<BigDecimal> hashSet = new HashSet<>();
        hashSet.add(left);
        hashSet.add(right);
        System.out.println("HashSet.size=" + hashSet.size());
        System.out.println("HashSet.contains(1200)=" + hashSet.contains(new BigDecimal("1200")));
        Map<BigDecimal, String> map = new HashMap<>();
        map.put(left, "paid");
        System.out.println("HashMap.get(1200.00)=" + map.get(right));
        Set<BigDecimal> treeSet = new TreeSet<>();
        treeSet.add(left);
        treeSet.add(right);
        System.out.println("TreeSet.size=" + treeSet.size());
        Set<BigDecimal> normalized = new HashSet<>();
        normalized.add(MoneyRules.fixedTwoDecimals(left));
        normalized.add(MoneyRules.fixedTwoDecimals(right));
        System.out.println("normalized.size=" + normalized.size());
        System.out.println("normalized.contains(1200)="
                + normalized.contains(MoneyRules.fixedTwoDecimals(new BigDecimal("1200"))));
        System.out.println("fixed(1200.000)="
                + MoneyRules.fixedTwoDecimals(new BigDecimal("1200.000")));
        try {
            MoneyRules.fixedTwoDecimals(new BigDecimal("1200.001"));
        } catch (ArithmeticException ex) {
            System.out.println("fixed(1200.001)=rejected");
        }
        BigDecimal a = new BigDecimal("0.004");
        BigDecimal b = new BigDecimal("0.003");
        System.out.println("differentBeforeRounding=" + !MoneyRules.sameAmount(a, b));
        System.out.println("equalAfterRounding=" + a.setScale(2, RoundingMode.HALF_UP)
                .equals(b.setScale(2, RoundingMode.HALF_UP)));
    }
}
