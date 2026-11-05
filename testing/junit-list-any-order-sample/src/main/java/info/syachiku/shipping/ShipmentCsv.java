package info.syachiku.shipping;

import java.util.List;

public final class ShipmentCsv {
    private ShipmentCsv() {
    }

    public static List<String> dataRows(List<ShipmentLine> lines) {
        return lines.stream()
                .map(line -> line.productCode() + "," + line.units())
                .toList();
    }

    public record ShipmentLine(String productCode, int units) {
    }
}
