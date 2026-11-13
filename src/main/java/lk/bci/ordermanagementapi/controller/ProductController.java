package lk.bci.ordermanagementapi.controller;

import jakarta.validation.Valid;
import lk.bci.ordermanagementapi.config.RequestIdFilter;
import lk.bci.ordermanagementapi.dto.request.AdjustStockRequest;
import lk.bci.ordermanagementapi.dto.request.CreateProductRequest;
import lk.bci.ordermanagementapi.dto.request.PatchProductRequest;
import lk.bci.ordermanagementapi.dto.request.ProductFilterRequest;
import lk.bci.ordermanagementapi.dto.request.UpdateProductRequest;
import lk.bci.ordermanagementapi.dto.response.ProductResponse;
import lk.bci.ordermanagementapi.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping({"/api/products", "/api/v1/products"})
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @ModelAttribute ProductFilterRequest filter,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        bindRequestIdToMdc(requestId);
        log.debug(
                "REST request to filter products: filter={}, page={}, size={}",
                filter, pageable.getPageNumber(), pageable.getPageSize()
        );
        Page<ProductResponse> products = productService.getProducts(filter, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable Long id
    ) {
        bindRequestIdToMdc(requestId);
        log.debug("REST request to get product by id={}", id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @Valid @RequestBody CreateProductRequest request
    ) {
        bindRequestIdToMdc(requestId);
        log.info("REST request to create product name={}, categoryId={}", request.name(), request.categoryId());
        ProductResponse productResponse = productService.createProduct(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(productResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> replaceProduct(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        bindRequestIdToMdc(requestId);
        log.info("REST request to replace product id={}", id);
        return ResponseEntity.ok(productService.replaceProduct(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> patchProduct(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable Long id,
            @Valid @RequestBody PatchProductRequest request
    ) {
        bindRequestIdToMdc(requestId);
        log.info("REST request to partially update product id={}", id);
        return ResponseEntity.ok(productService.patchProduct(id, request));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> adjustStock(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable Long id,
            @Valid @RequestBody AdjustStockRequest request
    ) {
        bindRequestIdToMdc(requestId);
        log.info("REST request to adjust stock for product id={}", id);
        return ResponseEntity.ok(productService.adjustStock(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable Long id
    ) {
        bindRequestIdToMdc(requestId);
        log.info("REST request to delete product id={}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    private void bindRequestIdToMdc(String requestId) {
        if (requestId != null && !requestId.isBlank()) {
            MDC.put(RequestIdFilter.REQUEST_ID_HEADER, requestId.trim());
            MDC.put(RequestIdFilter.MDC_REQUEST_ID_KEY, requestId.trim());
        }
    }
}
