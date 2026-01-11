
/*package io.smartpos.services.sale;

import io.smartpos.core.domain.sale.Sale;
import io.smartpos.core.domain.sale.SaleItem;
import io.smartpos.infrastructure.dao.InventoryMovementDao;
import io.smartpos.infrastructure.dao.SaleDao;
import io.smartpos.infrastructure.dao.SaleItemDao;
import io.smartpos.infrastructure.datasource.DataSourceProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SaleServiceImplTest {

        private SaleDao saleDao;
        private SaleItemDao saleItemDao;
        private InventoryMovementDao inventoryDao;
        private DataSourceProvider dataSourceProvider;
        private Connection connection;

        private SaleServiceImpl service;

        @BeforeEach
        void setUp() throws Exception {

                saleDao = mock(SaleDao.class);
                saleItemDao = mock(SaleItemDao.class);
                inventoryDao = mock(InventoryMovementDao.class);
                dataSourceProvider = mock(DataSourceProvider.class);
                connection = mock(Connection.class);

                when(dataSourceProvider.getConnection()).thenReturn(connection);

                service = new SaleServiceImpl(
                                saleDao,
                                saleItemDao,
                                inventoryDao,
                                dataSourceProvider);
        }

        @Test
        void findByDateRange_shouldAttachItemsAndCalculateTotals() {

                LocalDate start = LocalDate.now().minusDays(1);
                LocalDate end = LocalDate.now();

                Sale sale1 = new Sale();
                sale1.setId(1);
                sale1.setSaleDate(start);

                Sale sale2 = new Sale();
                sale2.setId(2);
                sale2.setSaleDate(end);

                when(saleDao.findByDateRange(start, end))
                                .thenReturn(List.of(sale1, sale2));

                SaleItem item1 = new SaleItem();
                item1.setSaleId(1);
                item1.setQuantity(new BigDecimal("2"));
                item1.setUnitPrice(new BigDecimal("10"));

                SaleItem item2 = new SaleItem();
                item2.setSaleId(1);
                item2.setQuantity(new BigDecimal("1"));
                item2.setUnitPrice(new BigDecimal("5"));

                SaleItem item3 = new SaleItem();
                item3.setSaleId(2);
                item3.setQuantity(new BigDecimal("3"));
                item3.setUnitPrice(new BigDecimal("7"));

                when(saleItemDao.findBySaleIds(
                                List.of(1, 2),
                                connection)).thenReturn(List.of(item1, item2, item3));

                List<Sale> result = service.findByDateRange(start, end);

                assertEquals(2, result.size());

                Sale r1 = result.get(0);
                Sale r2 = result.get(1);

                assertEquals(2, r1.getItems().size());
                assertEquals(1, r2.getItems().size());

                assertEquals(
                                new BigDecimal("25"),
                                r1.getTotalAmount());

                assertEquals(
                                new BigDecimal("21"),
                                r2.getTotalAmount());
        }

    @Test
    void findByDateRange_shouldReturnEmptyListWhenNoSales() {

        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now();

        when(saleDao.findByDateRange(start, end))
                .thenReturn(List.of());

        List<Sale> result = service.findByDateRange(start, end);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(saleItemDao, never())
                .findBySaleIds(any(), any());
    }


}
*/