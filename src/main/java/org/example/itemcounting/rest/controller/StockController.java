//package org.example.itemcounting.rest.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.example.itemcounting.business.service.StockService;
//import org.example.itemcounting.rest.dto.StockDTO;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/stock")
//@RequiredArgsConstructor
//public class StockController {
//
//    private final StockService stockService;
//
//    // остаток по всем товарам
//    @GetMapping("/current")
//    public ResponseEntity<List<StockDTO>> getCurrentStock() {
//        return ResponseEntity.ok(stockService.getAllStocks());
//    }
//}