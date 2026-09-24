//package org.example.itemcounting.mockMVC;
//
//import org.example.itemcounting.business.service.StockService;
//import static org.hamcrest.Matchers.*;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import org.example.itemcounting.rest.dto.StockDTO;
//import org.example.itemcounting.rest.viewController.StockViewController;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.hamcrest.Matchers.hasSize;
//
//
//@WebMvcTest(StockViewController.class)
//public class StockViewControllerTest {
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private StockService stockService;
//
//    @Test
//    void listStock() throws Exception {
//        LocalDateTime now = LocalDateTime.now();
//        StockDTO stock1 = new StockDTO(
//                1L,
//                101L,
//                "Apple",
//                "APP-001",
//                BigDecimal.valueOf(150.00),
//                now
//        );
//        StockDTO stock2 = new StockDTO(
//                2L,
//                102L,
//                "Melon",
//                "MEL-002",
//                BigDecimal.valueOf(28.00),
//                now
//        );
//        List<StockDTO> testStocks = List.of(stock1, stock2);
//
//        when(stockService.getAllStocks()).thenReturn(testStocks);
//
//
//        mockMvc.perform(get("/stock"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("stock/list"))
//                .andExpect(model().attributeExists("stocks"))
//                .andExpect(model().attribute("stocks", hasSize(2)))
//
//                .andExpect(model().attribute("stocks",
//                        hasItem(allOf(
//                                hasProperty("id", equalTo(1L)),
//                                hasProperty("productId", equalTo(101L)),
//                                hasProperty("productName", equalTo("Apple")),
//                                hasProperty("productSku", equalTo("APP-001")),
//                                hasProperty("quantity", equalTo(BigDecimal.valueOf(150.00))),
//                                hasProperty("updatedAt", equalTo(now))
//                        ))
//                ))
//                .andExpect(model().attribute("stocks",
//                        hasItem(allOf(
//                                hasProperty("id", equalTo(2L)),
//                                hasProperty("productId", equalTo(102L)),
//                                hasProperty("productName", equalTo("Melon")),
//                                hasProperty("productSku", equalTo("MEL-002")),
//                                hasProperty("quantity", equalTo(BigDecimal.valueOf(28.00))),
//                                hasProperty("updatedAt", equalTo(now))
//                        ))
//                ));
//    }
//}
