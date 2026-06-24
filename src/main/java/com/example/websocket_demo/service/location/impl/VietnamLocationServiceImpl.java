package com.example.websocket_demo.service.location.impl;

import com.example.websocket_demo.dto.response.GisDto;
import com.example.websocket_demo.dto.response.ProvinceDto;
import com.example.websocket_demo.dto.response.WardDto;
import com.example.websocket_demo.entity.AdministrativeRegion;
import com.example.websocket_demo.entity.AdministrativeUnit;
import com.example.websocket_demo.entity.GisProvince;
import com.example.websocket_demo.entity.GisWard;
import com.example.websocket_demo.mapper.LocationMapper;
import com.example.websocket_demo.repository.*;
import com.example.websocket_demo.service.location.VietnamLocationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VietnamLocationServiceImpl implements VietnamLocationService {

    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;
    private final GisProvinceRepository gisProvinceRepository;
    private final GisWardRepository gisWardRepository;
    private final AdministrativeRegionRepository administrativeRegionRepository;
    private final AdministrativeUnitRepository administrativeUnitRepository;
    private final LocationMapper locationMapper;

    @Override
    public List<AdministrativeRegion> getAllRegions() {
        return administrativeRegionRepository.findAll();
    }

    @Override
    public List<AdministrativeUnit> getAllUnits() {
        return administrativeUnitRepository.findAll();
    }

    @Override
    public List<ProvinceDto> getAllProvinces() {
        return provinceRepository.findAll().stream()
                .map(locationMapper::mapToProvinceDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProvinceDto getProvinceByCode(String code) {
        return provinceRepository.findById(code)
                .map(locationMapper::mapToProvinceDto)
                .orElseThrow();
    }

    @Override
    public List<WardDto> getWardsByProvince(String code) {
        return wardRepository.findByProvinceCode(code).stream()
                .map(locationMapper::mapToWardDto)
                .collect(Collectors.toList());
    }

    @Override
    public WardDto getWardByCode(String code) {
        return wardRepository.findById(code)
                .map(locationMapper::mapToWardDto)
                .orElseThrow();
    }

    @Override
    public GisDto getGisProvince(String code) {
        GisProvince gisProvince = gisProvinceRepository.findByProvinceCode(code);
        if (gisProvince == null) throw new NoSuchElementException("Province not found");
        return locationMapper.mapToGisProvinceDto(gisProvince);

    }

    @Override
    public GisDto getGisWard(String code) {
        GisWard gisWard = gisWardRepository.findByWardCode(code);
        if (gisWard == null) throw new NoSuchElementException("Ward not found");
        return locationMapper.mapToGisWardDto(gisWard);
    }
}
