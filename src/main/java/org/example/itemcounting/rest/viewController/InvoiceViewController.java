package org.example.itemcounting.rest.viewController;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.business.service.InvoiceService;
import org.example.itemcounting.business.service.ProductService;
import org.example.itemcounting.enums.InvoiceStatus;
import org.example.itemcounting.enums.InvoiceType;
import org.example.itemcounting.rest.dto.InvoiceDTO;
import org.example.itemcounting.rest.dto.InvoiceItemDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceViewController {

    private final InvoiceService invoiceService;
    private final ProductService productService;

    // ----- Приходные накладные -----
    @GetMapping("/arrival")
    public String listArrivalInvoices(Model model) {
        List<InvoiceDTO> invoices = invoiceService.getAllInvoices(InvoiceType.ARRIVAL, null);
        model.addAttribute("invoices", invoices);
        model.addAttribute("type", "ARRIVAL");
        return "invoices/list";
    }

    @GetMapping("/arrival/new")
    public String showCreateArrivalForm(Model model) {
        InvoiceDTO invoice = new InvoiceDTO();
        invoice.setType(InvoiceType.ARRIVAL);
        invoice.setStatus(InvoiceStatus.DRAFT);

        List<InvoiceItemDTO> items = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            items.add(new InvoiceItemDTO());
        }

        invoice.setItems(items);
        model.addAttribute("invoice", invoice);
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("action", "create");
        return "invoices/form";
    }

    @PostMapping("/arrival/new")
    public String createArrivalInvoice(@ModelAttribute InvoiceDTO invoiceDTO) {
        invoiceDTO.setType(InvoiceType.ARRIVAL);
        invoiceDTO.setStatus(InvoiceStatus.DRAFT);

        if (invoiceDTO.getItems() != null) {
            invoiceDTO.setItems(
                    invoiceDTO.getItems().stream()
                            .filter(item -> item.getProductId() != null)
                            .toList()
            );
        }
        invoiceService.createInvoice(invoiceDTO);
        return "redirect:/invoices/arrival";
    }

    // ----- Расходные накладные -----
    @GetMapping("/shipment")
    public String listShipmentInvoices(Model model) {
        List<InvoiceDTO> invoices = invoiceService.getAllInvoices(InvoiceType.SHIPMENT, null);
        model.addAttribute("invoices", invoices);
        model.addAttribute("type", "SHIPMENT");
        return "invoices/list";
    }

    @GetMapping("/shipment/new")
    public String showCreateShipmentForm(Model model) {
        InvoiceDTO invoice = new InvoiceDTO();
        invoice.setType(InvoiceType.SHIPMENT);
        invoice.setStatus(InvoiceStatus.DRAFT);
        
        List<InvoiceItemDTO> items = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            items.add(new InvoiceItemDTO());
        }
        invoice.setItems(items);
        model.addAttribute("invoice", invoice);
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("action", "create");
        return "invoices/form";
    }

    @PostMapping("/shipment/new")
    public String createShipmentInvoice(@ModelAttribute InvoiceDTO invoiceDTO) {
        invoiceDTO.setType(InvoiceType.SHIPMENT);
        invoiceDTO.setStatus(InvoiceStatus.DRAFT);

        if (invoiceDTO.getItems() != null) {
            invoiceDTO.setItems(
                    invoiceDTO.getItems().stream()
                            .filter(item -> item.getProductId() != null)
                            .toList()
            );
        }
        invoiceService.createInvoice(invoiceDTO);
        return "redirect:/invoices/shipment";
    }




    @GetMapping("/{id}")
    public String viewInvoice(@PathVariable Long id, Model model) {
        InvoiceDTO invoice = invoiceService.getInvoiceById(id);
        model.addAttribute("invoice", invoice);
        return "invoices/detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancelInvoice(@PathVariable Long id) {
        invoiceService.cancelInvoice(id);

        InvoiceDTO invoice = invoiceService.getInvoiceById(id);
        if (invoice.getType() == InvoiceType.ARRIVAL) {
            return "redirect:/invoices/arrival";
        } else {
            return "redirect:/invoices/shipment";
        }
    }
}
