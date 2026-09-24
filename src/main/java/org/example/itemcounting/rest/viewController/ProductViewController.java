//package org.example.itemcounting.rest.viewController;
//
//import lombok.RequiredArgsConstructor;
//import org.example.itemcounting.business.service.ProductService;
//import org.example.itemcounting.rest.dto.ProductDTO;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//@Controller
//@RequestMapping("/products")
//@RequiredArgsConstructor
//public class ProductViewController {
//
//    private final ProductService productService;
//
//    @GetMapping
//    public String listProducts(Model model) {
//        model.addAttribute("products", productService.getAllProducts());
//        return "products/list";
//    }
//
//    // показать форму создания
//    @GetMapping("/new")
//    public String showCreateForm(Model model) {
//        model.addAttribute("product", new ProductDTO());
//        model.addAttribute("action", "create");
//        return "products/form";
//    }
//
//    // обработка создания
//    @PostMapping("/new")
//    public String createProduct(@ModelAttribute ProductDTO productDTO) {
//        productService.createProduct(productDTO);
//        return "redirect:/products";
//    }
//
//    // форма редактирования
//    @GetMapping("/edit/{id}")
//    public String showEditForm(@PathVariable Long id, Model model) {
//        ProductDTO product = productService.getProductById(id);
//        model.addAttribute("product", product);
//        model.addAttribute("action", "edit");
//        return "products/form";
//    }
//
//    // обработка обновления
//    @PostMapping("/edit/{id}")
//    public String updateProduct(@PathVariable Long id, @ModelAttribute ProductDTO productDTO) {
//        productService.updateProduct(id, productDTO);
//        return "redirect:/products";
//    }
//
//    // детали товара
//    @GetMapping("/{id}")
//    public String viewProduct(@PathVariable Long id, Model model) {
//        model.addAttribute("product", productService.getProductById(id));
//        return "products/detail";
//    }
//
//    // удаление
//    @PostMapping("/delete/{id}")
//    public String deleteProduct(@PathVariable Long id) {
//        productService.deleteProduct(id);
//        return "redirect:/products";
//    }
//
//    @GetMapping("/")
//    public String index() {
//        return "index";
//    }
//}