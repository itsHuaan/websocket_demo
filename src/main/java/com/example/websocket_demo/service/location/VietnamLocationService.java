package com.example.websocket_demo.service.location;

import com.example.websocket_demo.dto.response.*;
import com.example.websocket_demo.entity.AdministrativeRegionEntity;
import com.example.websocket_demo.entity.AdministrativeUnitEntity;

import java.util.List;

public interface VietnamLocationService {
    List<AdministrativeRegionResponse> getAllRegions();
    AdministrativeRegionResponse getRegionByCodeName(String codeName);
    List<AdministrativeUnitResponse> getAllUnits();
    AdministrativeUnitResponse getUnitByCodeName(String codeName);
    List<ProvinceResponse> getAllProvinces();
    List<ProvinceResponse> getProvincesByUnit(String codeName);
    List<ProvinceResponse> getProvincesByRegion(String codeName);
    ProvinceResponse getProvinceByCodeName(String codeName);
    List<WardResponse> getWardsByProvince(String codeName);
    WardResponse getWardByCode(String codeName);
    GisResponse getGisProvince(String code);
    GisResponse getGisWard(String code);

}
