package com.example.websocket_demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProvinceResponse {
    String code;
    String name;
    String nameEn;
    String fullName;
    String fullNameEn;
    String codeName;
    AdministrativeUnitResponse administrativeUnit;
//    Integer administrativeRegionId;
}
