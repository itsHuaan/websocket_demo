package com.example.websocket_demo.mapper;

import com.example.websocket_demo.common.DataUtil;
import com.example.websocket_demo.dto.response.*;
import com.example.websocket_demo.entity.*;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {
    public AdministrativeUnitResponse mapToAdministrativeUnitDto(AdministrativeUnitEntity administrativeUnit) {
        return AdministrativeUnitResponse.builder()
                .fullName(administrativeUnit.getFullName())
                .fullNameEn(administrativeUnit.getFullNameEn())
                .shortName(administrativeUnit.getShortName())
                .shortNameEn(administrativeUnit.getShortNameEn())
                .codeName(administrativeUnit.getCodeName())
                .codeNameEn(administrativeUnit.getCodeNameEn())
                .build();
    }

    public AdministrativeRegionResponse mapToAdministrativeRegionDto(AdministrativeRegionEntity administrativeRegion) {
        return AdministrativeRegionResponse.builder()
                .name(administrativeRegion.getName())
                .nameEn(administrativeRegion.getNameEn())
                .codeName(administrativeRegion.getCodeName())
                .codeNameEn(administrativeRegion.getCodeNameEn())
                .build();
    }

    public ProvinceResponse mapToProvinceDto(ProvinceEntity province) {
        return ProvinceResponse.builder()
                .code(province.getCode())
                .name(province.getName())
                .nameEn(province.getNameEn())
                .fullName(province.getFullName())
                .fullNameEn(province.getFullNameEn())
                .codeName(province.getCodeName())
                .administrativeUnit(!DataUtil.isNullOrEmpty(province.getAdministrativeUnit())
                        ? this.mapToAdministrativeUnitDto(province.getAdministrativeUnit())
                        : null)
                .build();
    }

    public WardResponse mapToWardDto(WardEntity ward) {
        return WardResponse.builder()
                .code(ward.getCode())
                .name(ward.getName())
                .nameEn(ward.getNameEn())
                .fullName(ward.getFullName())
                .fullNameEn(ward.getFullNameEn())
                .codeName(ward.getCodeName())
                .administrativeUnit(!DataUtil.isNullOrEmpty(ward.getAdministrativeUnit())
                        ? this.mapToAdministrativeUnitDto(ward.getAdministrativeUnit())
                        : null)
                .build();
    }

    public GisResponse mapToGisProvinceDto(GisProvinceEntity gisProvince) {
        return GisResponse.builder()
                .id(gisProvince.getId())
                .code(gisProvince.getProvince() != null ? gisProvince.getProvince().getCode() : null)
                .gisServerId(gisProvince.getGisServerId())
                .areaKm2(gisProvince.getAreaKm2())
                .bboxWkt(gisProvince.getBbox() != null ? gisProvince.getBbox().toText() : null)
                .geomWkt(gisProvince.getGeom() != null ? gisProvince.getGeom().toText() : null)
                .build();
    }

    public GisResponse mapToGisWardDto(GisWardEntity gisWard) {
        return GisResponse.builder()
                .id(gisWard.getId())
                .code(gisWard.getWard() != null ? gisWard.getWard().getCode() : null)
                .gisServerId(gisWard.getGisServerId())
                .areaKm2(gisWard.getAreaKm2() != null ? gisWard.getAreaKm2().doubleValue() : null)
                .bboxWkt(gisWard.getBbox() != null ? gisWard.getBbox().toText() : null)
                .geomWkt(gisWard.getGeom() != null ? gisWard.getGeom().toText() : null)
                .build();
    }
}
