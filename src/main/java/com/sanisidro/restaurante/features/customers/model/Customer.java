package com.sanisidro.restaurante.features.customers.model;

import com.sanisidro.restaurante.core.model.Auditable;
import com.sanisidro.restaurante.core.security.model.User;
import jakarta.persistence.*;
        import lombok.*;

        import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(name = "id", column = @Column(name = "customer_id"))
public class Customer extends Auditable {

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "points")
    private Integer points;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Address> addresses = new LinkedHashSet<>();

}
