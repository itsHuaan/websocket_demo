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
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_gis_wards")
public class GisWardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_code", referencedColumnName = "code", nullable = false)
    private WardEntity ward;

    @Column(name = "gis_server_id", length = 50)
    private String gisServerId;

    @Column(name = "area_km2", precision = 12, scale = 5)
    private BigDecimal areaKm2;

    @Column(name = "bbox", nullable = false, columnDefinition = "POLYGON")
    private Polygon bbox;

    @Column(name = "geom", nullable = false, columnDefinition = "MULTIPOLYGON")
    private MultiPolygon geom;
}
