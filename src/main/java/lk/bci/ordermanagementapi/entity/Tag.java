package lk.bci.ordermanagementapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter; // Added missing imports
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
@AllArgsConstructor
@NoArgsConstructor
@Getter // Added Getter
@Setter // Added Setter
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Product> productSet = new HashSet<>();
}