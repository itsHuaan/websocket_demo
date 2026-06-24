package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.dto.response.GisDto;
import com.example.websocket_demo.dto.response.ProvinceDto;
import com.example.websocket_demo.dto.response.WardDto;
import com.example.websocket_demo.entity.*;
import com.example.websocket_demo.repository.*;
import com.example.websocket_demo.service.location.VietnamLocationService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VietnamLocationController {
    VietnamLocationService vietnamLocationService;

    @GetMapping("/regions")
    public ResponseEntity<ApiResponse<List<AdministrativeRegion>>> getAllRegions() {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Fetched all administrative regions", vietnamLocationService.getAllRegions()));
    }

    @GetMapping("/units")
    public ResponseEntity<ApiResponse<List<AdministrativeUnit>>> getAllUnits() {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Fetched all administrative unit", vietnamLocationService.getAllUnits()));
    }

    @GetMapping("/provinces")
    public ResponseEntity<ApiResponse<List<ProvinceDto>>> getAllProvinces() {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Fetched all provinces", vietnamLocationService.getAllProvinces()));
    }

    @GetMapping("/provinces/{code}")
    public ResponseEntity<ApiResponse<ProvinceDto>> getProvinceByCode(@PathVariable String code) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Fetched province", vietnamLocationService.getProvinceByCode(code)));
    }

    @GetMapping("/provinces/{code}/wards")
    public ResponseEntity<ApiResponse<List<WardDto>>> getWardsByProvince(@PathVariable String code) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Fetched wards", vietnamLocationService.getWardsByProvince(code)));
    }

    @GetMapping("/wards/{code}")
    public ResponseEntity<ApiResponse<WardDto>> getWardByCode(@PathVariable String code) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Fetched ward", vietnamLocationService.getWardByCode(code)));
    }

    @GetMapping("/provinces/{code}/gis")
    public ResponseEntity<ApiResponse<GisDto>> getGisProvince(@PathVariable String code) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Fetched gis province", vietnamLocationService.getGisProvince(code)));
    }

    @GetMapping("/wards/{code}/gis")
    public ResponseEntity<ApiResponse<GisDto>> getGisWard(@PathVariable String code) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Fetched gis ward", vietnamLocationService.getGisWard(code)));
    }


}
