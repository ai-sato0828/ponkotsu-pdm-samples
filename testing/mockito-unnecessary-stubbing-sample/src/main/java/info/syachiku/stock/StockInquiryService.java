package info.syachiku.stock;

public final class StockInquiryService {
    private final StockGateway warehouse;

    public StockInquiryService(StockGateway warehouse) {
        this.warehouse = warehouse;
    }

    public int availableUnits(String productCode) {
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("商品コードを入力してください");
        }
        return warehouse.availableUnits(productCode);
    }
}
