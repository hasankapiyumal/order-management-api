package lk.bci.ordermanagementapi.service;

import lk.bci.ordermanagementapi.dto.request.AdjustStockRequest;
import lk.bci.ordermanagementapi.dto.request.CreateProductRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ProductMapper productMapper;

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
        Specification<Product> spec = ProductSpecification.filterProducts(
                name,
                categoryId,
                categoryName,
                tagId,
                tagName,
                minPrice,
                maxPrice,
                inStock
        );

        return productRepository.findAll(spec, pageable)
                .map(productMapper::toResponse);
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid category ID: " + request.categoryId()
                ));

        Set<Tag> tags = resolveTags(request.tagIds());

        Product product = productMapper.toEntity(request, category, tags);
        product.setName(request.name().trim());

        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse adjustStock(Long id, AdjustStockRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        int newStock;
        if (request.quantityChange() != null) {
            newStock = product.getStock() + request.quantityChange();
        } else if (request.delta() != null) {
            newStock = product.getStock() + request.delta();
        } else if (request.stock() != null) {
            newStock = request.stock();
        } else if (request.quantity() != null) {
            newStock = product.getStock() + request.quantity();
        } else {
            throw new IllegalArgumentException(
                    "Stock adjustment value ('quantityChange', 'delta', 'quantity', or 'stock') is required"
            );
        }

        if (newStock < 0) {
            throw new InsufficientStockException(
                    "Insufficient stock for product id " + id + ". Current stock: " + product.getStock()
            );
        }

        product.setStock(newStock);
        Product savedProduct = productRepository.saveAndFlush(product);
        return productMapper.toResponse(savedProduct);
    }

    private Set<Tag> resolveTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Tag> foundTags = tagRepository.findAllById(tagIds);
        if (foundTags.size() != tagIds.size()) {
            throw new IllegalArgumentException("One or more provided tag IDs are invalid");
        }

        return new HashSet<>(foundTags);
    }
}
