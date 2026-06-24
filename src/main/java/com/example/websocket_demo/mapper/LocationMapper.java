package com.example.websocket_demo.mapper;

import com.example.websocket_demo.dto.response.GisDto;
import com.example.websocket_demo.dto.response.ProvinceDto;
import com.example.websocket_demo.dto.response.WardDto;
import com.example.websocket_demo.entity.GisProvince;
import com.example.websocket_demo.entity.GisWard;
import com.example.websocket_demo.entity.Province;
import com.example.websocket_demo.entity.Ward;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {
    public ProvinceDto mapToProvinceDto(Province province) {
        return ProvinceDto.builder()
                .code(province.getCode())
                .name(province.getName())
                .nameEn(province.getNameEn())
                .fullName(province.getFullName())
                .fullNameEn(province.getFullNameEn())
                .codeName(province.getCodeName())
                .administrativeUnitId(province.getAdministrativeUnit() != null ? province.getAdministrativeUnit().getId() : null)
                .build();
    }

    public WardDto mapToWardDto(Ward ward) {
        return WardDto.builder()
                .code(ward.getCode())
                .name(ward.getName())
                .nameEn(ward.getNameEn())
                .fullName(ward.getFullName())
                .fullNameEn(ward.getFullNameEn())
                .codeName(ward.getCodeName())
                .provinceCode(ward.getProvince() != null ? ward.getProvince().getCode() : null)
                .administrativeUnitId(ward.getAdministrativeUnit() != null ? ward.getAdministrativeUnit().getId() : null)
                .build();
    }

    public GisDto mapToGisProvinceDto(GisProvince gisProvince) {
        return GisDto.builder()
                .id(gisProvince.getId())
                .code(gisProvince.getProvince() != null ? gisProvince.getProvince().getCode() : null)
                .gisServerId(gisProvince.getGisServerId())
                .areaKm2(gisProvince.getAreaKm2())
                .bboxWkt(gisProvince.getBbox() != null ? gisProvince.getBbox().toText() : null)
                .geomWkt(gisProvince.getGeom() != null ? gisProvince.getGeom().toText() : null)
                .build();
    }

    public GisDto mapToGisWardDto(GisWard gisWard) {
        return GisDto.builder()
                .id(gisWard.getId())
                .code(gisWard.getWard() != null ? gisWard.getWard().getCode() : null)
                .gisServerId(gisWard.getGisServerId())
                .areaKm2(gisWard.getAreaKm2() != null ? gisWard.getAreaKm2().doubleValue() : null)
                .bboxWkt(gisWard.getBbox() != null ? gisWard.getBbox().toText() : null)
                .geomWkt(gisWard.getGeom() != null ? gisWard.getGeom().toText() : null)
                .build();
    }
}
