package lk.bci.ordermanagementapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name ="category")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Category {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Column(nullable = false, unique = true)
 private String name;

 // Initialized directly on the field
 @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
 private List<Product> products = new ArrayList<>();

 public void addProduct(Product product) {
  products.add(product);
  product.setCategory(this);
 }

 public void removeProduct(Product product) {
  products.remove(product);
  product.setCategory(null);
 }
}