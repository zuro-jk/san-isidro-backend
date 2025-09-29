package com.sanisidro.restaurante.features.customers.model;

import com.sanisidro.restaurante.core.model.Auditable;
import com.sanisidro.restaurante.features.customers.dto.address.request.AddressAdminRequest;
import jakarta.persistence.*;
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

    @Column(name = "street", nullable = false)
    private String street;

    @Column()
    private String reference;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String province;

    @Column(length = 20)
    private String zipCode;

    @Column()
    private String instructions;

    public String getDescription() {
        String desc = street + ", " + city + ", " + province;
        if (reference != null && !reference.isBlank()) desc += " (" + reference + ")";
        return desc;
    }

    public void updateFromDto(AddressAdminRequest dto) {
        if (dto.getStreet() != null) this.street = dto.getStreet();
        if (dto.getReference() != null) this.reference = dto.getReference();
        if (dto.getCity() != null) this.city = dto.getCity();
        if (dto.getProvince() != null) this.province = dto.getProvince();
        if (dto.getZipCode() != null) this.zipCode = dto.getZipCode();
        if (dto.getInstructions() != null) this.instructions = dto.getInstructions();
    }
}
