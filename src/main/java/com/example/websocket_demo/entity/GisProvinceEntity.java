package com.example.websocket_demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_gis_provinces")
public class GisProvinceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_code", referencedColumnName = "code", nullable = false)
    private ProvinceEntity province;

    @Column(name = "gis_server_id", length = 50)
    private String gisServerId;

    @Column(name = "area_km2")
    private Double areaKm2;

    @Column(name = "bbox", nullable = false, columnDefinition = "POLYGON")
    private Polygon bbox;

    @Column(name = "geom", nullable = false, columnDefinition = "MULTIPOLYGON")
    private MultiPolygon geom;
}
