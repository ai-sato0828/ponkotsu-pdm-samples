package info.syachiku.stock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockInquiryServiceTest {
    @Mock
    private StockGateway warehouse;
    private StockInquiryService service;

    @BeforeEach
    void setUp() {
        service = new StockInquiryService(warehouse);
    }

    @Test
    void returnsEightForKnownProduct() {
        when(warehouse.availableUnits("SKU-100")).thenReturn(8);

        assertEquals(8, service.availableUnits("SKU-100"));
        verify(warehouse).availableUnits("SKU-100");
    }

    @Test
    void rejectsBlankWithoutContactingWarehouse() {
        var error = assertThrows(IllegalArgumentException.class,
                () -> service.availableUnits("   "));
        assertEquals("商品コードを入力してください", error.getMessage());
        verifyNoInteractions(warehouse);
    }
}
