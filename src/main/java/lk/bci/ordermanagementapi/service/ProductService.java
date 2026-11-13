package lk.bci.ordermanagementapi.service;

import lk.bci.ordermanagementapi.dto.request.AdjustStockRequest;
import lk.bci.ordermanagementapi.dto.request.CreateProductRequest;
import lk.bci.ordermanagementapi.dto.request.PatchProductRequest;
import lk.bci.ordermanagementapi.dto.request.ProductFilterRequest;
import lk.bci.ordermanagementapi.dto.request.UpdateProductRequest;
import lk.bci.ordermanagementapi.dto.response.ProductResponse;
import lk.bci.ordermanagementapi.entity.Category;
import lk.bci.ordermanagementapi.entity.Product;
import lk.bci.ordermanagementapi.entity.Tag;
import lk.bci.ordermanagementapi.exception.InsufficientStockException;
import lk.bci.ordermanagementapi.exception.ResourceNotFoundException;
import lk.bci.ordermanagementapi.mapper.ProductMapper;
import lk.bci.ordermanagementapi.repository.CategoryRepository;
import lk.bci.ordermanagementapi.repository.ProductRepository;
import lk.bci.ordermanagementapi.repository.ProductSpecification;
import lk.bci.ordermanagementapi.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(ProductFilterRequest filter, Pageable pageable) {
        log.debug("Querying products with filter={}, page={}, size={}", filter, pageable.getPageNumber(), pageable.getPageSize());
        Specification<Product> spec = ProductSpecification.build(filter);
        return productRepository.findAll(spec, pageable)
                .map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(
            String name,
            Long categoryId,
            String categoryName,
            Long tagId,
            String tagName,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock,
            Pageable pageable
    ) {
        return getProducts(
                new ProductFilterRequest(name, categoryId, categoryName, tagId, tagName, minPrice, maxPrice, inStock),
                pageable
        );
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.debug("Fetching product by id={}", id);
        Product product = findProductOrThrow(id);
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Category category = resolveCategory(request.categoryId());
        Set<Tag> tags = resolveTags(request.tagIds());

        Product product = productMapper.toEntity(request, category, tags);
        product.setName(request.name().trim());

        Product savedProduct = productRepository.save(product);
        log.info(
                "Successfully created product id={}, name={}, price={}, stock={}",
                savedProduct.getId(), savedProduct.getName(), savedProduct.getPrice(), savedProduct.getStock()
        );
        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse replaceProduct(Long id, UpdateProductRequest request) {
        Product product = findProductOrThrow(id);
        Category category = resolveCategory(request.categoryId());
        Set<Tag> tags = resolveTags(request.tagIds());

        productMapper.updateEntity(product, request, category, tags);
        product.setName(request.name().trim());

        Product savedProduct = productRepository.saveAndFlush(product);
        log.info("Successfully replaced product id={}, name={}", savedProduct.getId(), savedProduct.getName());
        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse patchProduct(Long id, PatchProductRequest request) {
        Product product = findProductOrThrow(id);

        productMapper.patchEntity(product, request);
        if (request.name() != null) {
            product.setName(request.name().trim());
        }
        if (request.categoryId() != null) {
            product.setCategory(resolveCategory(request.categoryId()));
        }
        if (request.tagIds() != null) {
            product.setTags(resolveTags(request.tagIds()));
        }

        Product savedProduct = productRepository.saveAndFlush(product);
        log.info("Successfully patched product id={}", savedProduct.getId());
        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse adjustStock(Long id, AdjustStockRequest request) {
        Product product = findProductOrThrow(id);
        int previousStock = product.getStock();

        int newStock;
        if (request.quantityChange() != null) {
            newStock = previousStock + request.quantityChange();
        } else if (request.delta() != null) {
            newStock = previousStock + request.delta();
        } else if (request.stock() != null) {
            newStock = request.stock();
        } else if (request.quantity() != null) {
            newStock = previousStock + request.quantity();
        } else {
            log.warn("Invalid stock adjustment request for product id={}: no stock value provided", id);
            throw new IllegalArgumentException(
                    "Stock adjustment value ('quantityChange', 'delta', 'quantity', or 'stock') is required"
            );
        }

        if (newStock < 0) {
            log.warn(
                    "Insufficient stock when adjusting product id={}: currentStock={}, requestedNewStock={}",
                    id, previousStock, newStock
            );
            throw new InsufficientStockException(
                    "Insufficient stock for product id " + id + ". Current stock: " + previousStock
            );
        }

        product.setStock(newStock);
        Product savedProduct = productRepository.saveAndFlush(product);
        log.info("Adjusted stock for product id={}: {} -> {}", id, previousStock, newStock);
        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProductOrThrow(id);
        productRepository.delete(product);
        log.info("Successfully deleted product id={}", id);
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with id={}", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });
    }

    private Category resolveCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Invalid category ID={}", categoryId);
                    return new IllegalArgumentException("Invalid category ID: " + categoryId);
                });
    }

    private Set<Tag> resolveTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Tag> foundTags = tagRepository.findAllById(tagIds);
        if (foundTags.size() != tagIds.size()) {
            log.warn("Invalid tag IDs provided: requested={}, found={}", tagIds.size(), foundTags.size());
            throw new IllegalArgumentException("One or more provided tag IDs are invalid");
        }

        return new HashSet<>(foundTags);
    }
}
