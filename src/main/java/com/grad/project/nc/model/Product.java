package com.grad.project.nc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Product {
    private Long productId;
    private String productName;
    private String productDescription;
    private Boolean isActive;
    private ProductType productType;

    private List<ProductCharacteristicValue> productCharacteristicValues;
    private List<ProductCharacteristic> productCharacteristics;
    private List<ProductRegionPrice> prices;
}
