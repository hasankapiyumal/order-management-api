package lk.bci.ordermanagementapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_profile")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String phone;
    private String address;

    @OneToOne(fetch = FetchType.LAZY) // Removed CascadeType.ALL to prevent deleting User if profile is deleted
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}