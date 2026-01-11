package io.smartpos.services.sale;

import io.smartpos.core.domain.sale.Sale;
import io.smartpos.core.domain.sale.SaleItem;
import io.smartpos.core.exceptions.BusinessException;
import io.smartpos.infrastructure.dao.InventoryMovementDao;
import io.smartpos.infrastructure.dao.SaleDao;
import io.smartpos.infrastructure.dao.SaleItemDao;
import io.smartpos.infrastructure.datasource.DataSourceProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

        @Mock
        private SaleDao saleDao;

        @Mock
        private SaleItemDao saleItemDao;

        @Mock
        private InventoryMovementDao inventoryDao;

        @Mock
        private DataSourceProvider dataSourceProvider;

        @Mock
        private Connection connection;

        @InjectMocks
        private SaleServiceImpl service;

        private Sale sale;

        @BeforeEach
        void setUp() throws Exception {

                when(dataSourceProvider.getConnection())
                                .thenReturn(connection);

                sale = new Sale();

                SaleItem item = new SaleItem();
                item.setProductId(1);
                item.setQuantity(new BigDecimal("2"));
                item.setUnitPrice(new BigDecimal("10"));

                sale.setItems(List.of(item));
        }

        @Test
        void registerSale_success() throws Exception {

                when(inventoryDao.getCurrentStockByProductIds(
                                anyList(),
                                eq(connection)))
                                .thenReturn(
                                                Map.of(1, new BigDecimal("10")));

                when(saleDao.save(eq(sale), eq(connection)))
                                .thenReturn(100);

                assertDoesNotThrow(() -> service.registerSale(sale));

                verify(connection).setAutoCommit(false);
                verify(connection).commit();

                verify(saleDao).save(eq(sale), eq(connection));
                verify(saleItemDao)
                                .saveBatch(eq(sale.getItems()), eq(connection));

                verify(inventoryDao)
                                .registerExitBatch(
                                                eq(Map.of(
                                                                1, new BigDecimal("2"))),
                                                eq("SALE"),
                                                eq(connection));
        }

        @Test
        void registerSale_insufficientStock_throwsException() throws Exception {

                when(inventoryDao.getCurrentStockByProductIds(
                                anyList(),
                                eq(connection)))
                                .thenReturn(
                                                Map.of(1, new BigDecimal("1")));

                BusinessException ex = assertThrows(
                                BusinessException.class,
                                () -> service.registerSale(sale));

                assertTrue(
                                ex.getMessage().contains("Insufficient stock"));

                verify(connection).rollback();
                verify(saleDao, never())
                                .save(any(), any());
        }

        @Test
        void registerSale_exceptionDuringSave_rollsBack() throws Exception {

                when(inventoryDao.getCurrentStockByProductIds(
                                anyList(),
                                eq(connection)))
                                .thenReturn(
                                                Map.of(1, new BigDecimal("10")));

                when(saleDao.save(any(), any()))
                                .thenThrow(new RuntimeException("DB error"));

                assertThrows(
                                BusinessException.class,
                                () -> service.registerSale(sale));

                verify(connection).rollback();
        }
}
