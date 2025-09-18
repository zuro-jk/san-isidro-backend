package com.sanisidro.restaurante.features.customers.model;

import com.sanisidro.restaurante.core.model.Auditable;
import com.sanisidro.restaurante.features.customers.dto.address.request.AddressRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(name = "id", column = @Column(name = "address_id"))
public class Address extends Auditable {

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Size(max = 255)
    @Column(name = "address", columnDefinition = "text", nullable = false)
    private String address;

    @Size(max = 255)
    @Column(name = "reference", columnDefinition = "text")
    private String reference;

    public String getDescription() {
        if (reference != null && !reference.isBlank()) {
            return address + " (" + reference + ")";
        }
        return address;
    }

    public void updateFromDto(AddressRequest dto) {
        if (dto.getAddress() != null) this.address = dto.getAddress();
        if (dto.getReference() != null) this.reference = dto.getReference();
    }
}
