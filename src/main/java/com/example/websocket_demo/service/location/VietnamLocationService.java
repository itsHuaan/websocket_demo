package com.example.websocket_demo.service.location;

import com.example.websocket_demo.controller.VietnamLocationController;
import com.example.websocket_demo.dto.response.GisDto;
import com.example.websocket_demo.dto.response.ProvinceDto;
import com.example.websocket_demo.dto.response.WardDto;
import com.example.websocket_demo.entity.AdministrativeRegion;
import com.example.websocket_demo.entity.AdministrativeUnit;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface VietnamLocationService {
    List<AdministrativeRegion> getAllRegions();
    List<AdministrativeUnit> getAllUnits();
    List<ProvinceDto> getAllProvinces();
    ProvinceDto getProvinceByCode(String code);
    List<WardDto> getWardsByProvince(String code);
    WardDto getWardByCode(String code);
    GisDto getGisProvince(String code);
    GisDto getGisWard(String code);

}
