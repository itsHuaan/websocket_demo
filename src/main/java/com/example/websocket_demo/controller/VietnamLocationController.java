package com.example.websocket_demo.controller;

import com.example.websocket_demo.common.Const;
import com.example.websocket_demo.common.DataUtil;
import com.example.websocket_demo.common.MessageService;
import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.dto.response.GisResponse;
import com.example.websocket_demo.dto.response.ProvinceResponse;
import com.example.websocket_demo.dto.response.WardResponse;
import com.example.websocket_demo.service.location.VietnamLocationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.websocket_demo.enumeration.ResponseMessage.*;

@RestController
@Tag(name = "Vietnam Location Controller")
@RequestMapping(Const.API_PREFIX_V1 + "/locations")
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VietnamLocationController {
    MessageService messageService;
    VietnamLocationService vietnamLocationService;

    @GetMapping(value = {"/regions", "/regions/{code-name}"})
    public ResponseEntity<ApiResponse<?>> getAllRegions(@PathVariable(value = "code-name", required = false) String codeName) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK,
                messageService.getMessage(FETCH_ALL_ADMINISTRATIVE_REGIONS.getCode()),
                DataUtil.isNullOrEmpty(codeName)
                        ? vietnamLocationService.getAllRegions()
                        : vietnamLocationService.getRegionByCodeName(codeName)));
    }

    @GetMapping(value = {"/units", "/units/{code-name}"})
    public ResponseEntity<ApiResponse<?>> getAllUnits(@PathVariable(name = "code-name", required = false) String codeName) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK,
                messageService.getMessage(FETCH_ALL_ADMINISTRATIVE_UNITS.getCode()),
                DataUtil.isNullOrEmpty(codeName)
                        ? vietnamLocationService.getAllUnits()
                        : vietnamLocationService.getUnitByCodeName(codeName)));
    }

    @GetMapping({"/provinces", "/provinces/{code-name}"})
    public ResponseEntity<ApiResponse<?>> getAllProvinces(@PathVariable(value = "code-name", required = false) String codeName) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK,
                "Fetched all provinces",

                DataUtil.isNullOrEmpty(codeName)
                ? vietnamLocationService.getAllProvinces()
                : vietnamLocationService.getProvinceByCodeName(codeName)));
    }

    @GetMapping("/units/{code-name}/provinces")
    public ResponseEntity<ApiResponse<List<ProvinceResponse>>> getProvincesByUnit(@PathVariable("code-name") String codeName) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Fetched all provinces", vietnamLocationService.getProvincesByUnit(codeName)));
    }

    @GetMapping("/regions/{code-name}/provinces")
    public ResponseEntity<ApiResponse<List<ProvinceResponse>>> getProvincesByRegion(@PathVariable("code-name") String codeName) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Fetched all provinces", vietnamLocationService.getProvincesByRegion(codeName)));
    }

    @GetMapping("/provinces/{code-name}/wards")
    public ResponseEntity<ApiResponse<List<WardResponse>>> getWardsByProvince(@PathVariable("code-name") String codeName) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Fetched wards", vietnamLocationService.getWardsByProvince(codeName)));
    }

    @GetMapping("/wards/{code-name}")
    public ResponseEntity<ApiResponse<WardResponse>> getWardByCode(@PathVariable("code-name") String codeName) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Fetched ward", vietnamLocationService.getWardByCode(codeName)));
    }

    @GetMapping("/provinces/{code}/gis")
    public ResponseEntity<ApiResponse<GisResponse>> getGisProvince(@PathVariable String code) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Fetched gis province", vietnamLocationService.getGisProvince(code)));
    }

    @GetMapping("/wards/{code}/gis")
    public ResponseEntity<ApiResponse<GisResponse>> getGisWard(@PathVariable String code) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Fetched gis ward", vietnamLocationService.getGisWard(code)));
    }
}
