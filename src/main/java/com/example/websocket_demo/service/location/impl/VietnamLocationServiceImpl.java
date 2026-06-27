package com.example.websocket_demo.service.location.impl;

import com.example.websocket_demo.common.DataUtil;
import com.example.websocket_demo.common.MessageService;
import com.example.websocket_demo.dto.response.*;
import com.example.websocket_demo.entity.AdministrativeRegionEntity;
import com.example.websocket_demo.entity.AdministrativeUnitEntity;
import com.example.websocket_demo.entity.GisProvinceEntity;
import com.example.websocket_demo.entity.GisWardEntity;
import com.example.websocket_demo.mapper.LocationMapper;
import com.example.websocket_demo.repository.*;
import com.example.websocket_demo.service.location.VietnamLocationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.example.websocket_demo.enumeration.ResponseMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VietnamLocationServiceImpl implements VietnamLocationService {

    MessageService messageService;
    ProvinceRepository provinceRepository;
    WardRepository wardRepository;
    GisProvinceRepository gisProvinceRepository;
    GisWardRepository gisWardRepository;
    AdministrativeRegionRepository administrativeRegionRepository;
    AdministrativeUnitRepository administrativeUnitRepository;
    LocationMapper locationMapper;

    @Override
    public List<AdministrativeRegionResponse> getAllRegions() {
        return administrativeRegionRepository.findAll().stream()
                .map(locationMapper::mapToAdministrativeRegionDto)
                .toList();
    }

    @Override
    public AdministrativeRegionResponse getRegionByCodeName(String codeName) {
        String snakeCaseCodeName = DataUtil.toSnakeCase(codeName);
        return administrativeRegionRepository.findByCodeName(snakeCaseCodeName)
                .map(locationMapper::mapToAdministrativeRegionDto)
                .orElseThrow(() -> new NoSuchElementException(messageService.getMessage(CANNOT_FIND_ADMINISTRATIVE_REGION.getCode(), snakeCaseCodeName)));
    }

    @Override
    public List<AdministrativeUnitResponse> getAllUnits() {
        return administrativeUnitRepository.findAll().stream()
                .map(locationMapper::mapToAdministrativeUnitDto)
                .toList();
    }

    @Override
    public AdministrativeUnitResponse getUnitByCodeName(String codeName) {
        String snakeCaseCodeName = DataUtil.toSnakeCase(codeName);
        return administrativeUnitRepository.findByCodeName(snakeCaseCodeName)
                .map(locationMapper::mapToAdministrativeUnitDto)
                .orElseThrow(() -> new NoSuchElementException(messageService.getMessage(CANNOT_FIND_ADMINISTRATIVE_UNIT.getCode(), snakeCaseCodeName)));
    }

    @Override
    public List<ProvinceResponse> getAllProvinces() {
        return provinceRepository.findAll().stream()
                .map(locationMapper::mapToProvinceDto)
                .toList();
    }

    @Override
    public List<ProvinceResponse> getProvincesByUnit(String codeName) {
        String snakeCaseCodeName = DataUtil.toSnakeCase(codeName);
        return provinceRepository.findByUnitCode(snakeCaseCodeName).stream()
                .map(locationMapper::mapToProvinceDto)
                .toList();
    }

    @Override
    public List<ProvinceResponse> getProvincesByRegion(String codeName) {
        String snakeCaseCodeName = DataUtil.toSnakeCase(codeName);
        return provinceRepository.findByRegionCode(snakeCaseCodeName).stream()
                .map(locationMapper::mapToProvinceDto)
                .toList();
    }

    @Override
    public ProvinceResponse getProvinceByCodeName(String codeName) {
        String snakeCaseCodeName = DataUtil.toSnakeCase(codeName);
        return provinceRepository.findByCodeName(snakeCaseCodeName)
                .map(locationMapper::mapToProvinceDto)
                .orElseThrow(() -> new NoSuchElementException(messageService.getMessage(PROVINCE_NOT_FOUND.getCode(), snakeCaseCodeName)));
    }

    @Override
    public List<WardResponse> getWardsByProvince(String codeName) {
        String snakeCaseCodeName = DataUtil.toSnakeCase(codeName);
        return wardRepository.findByProvinceCodeName(snakeCaseCodeName).stream()
                .map(locationMapper::mapToWardDto)
                .toList();
    }

    @Override
    public WardResponse getWardByCode(String codeName) {
        String snakeCaseCodeName = DataUtil.toSnakeCase(codeName);
        return wardRepository.findByCodeName(snakeCaseCodeName)
                .map(locationMapper::mapToWardDto)
                .orElseThrow(() -> new NoSuchElementException(messageService.getMessage(WARD_NOT_FOUND.getCode(), snakeCaseCodeName)));
    }

    @Override
    public GisResponse getGisProvince(String code) {
        GisProvinceEntity gisProvinceEntity = gisProvinceRepository.findByProvinceCode(code);
        if (gisProvinceEntity == null) throw new NoSuchElementException(messageService.getMessage(PROVINCE_NOT_FOUND.getCode()));
        return locationMapper.mapToGisProvinceDto(gisProvinceEntity);

    }

    @Override
    public GisResponse getGisWard(String code) {
        GisWardEntity gisWardEntity = gisWardRepository.findByWardCode(code);
        if (gisWardEntity == null) throw new NoSuchElementException(messageService.getMessage(WARD_NOT_FOUND.getCode()));
        return locationMapper.mapToGisWardDto(gisWardEntity);
    }
}
