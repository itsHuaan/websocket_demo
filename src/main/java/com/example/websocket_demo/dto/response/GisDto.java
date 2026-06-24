package com.example.websocket_demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GisDto {
    Integer id;
    String code;
    String gisServerId;
    Double areaKm2;
    String bboxWkt;
    String geomWkt;
}
