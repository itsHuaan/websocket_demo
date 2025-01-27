package com.example.websocket_demo.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Embeddable
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SkuValueId implements Serializable {
    Long skuId;
    Long optionId;
    Long optionValueId;
}
