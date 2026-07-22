package org.example.itemcounting.rest.viewController;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.business.service.StockService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockViewController {

    private final StockService stockService;

    @GetMapping
    public String listStock(Model model) {
        model.addAttribute("stocks", stockService.getAllStocks());
        return "stock/list";
    }
}