package com.example.websocket_demo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.websocket_demo.entity.TestImportEntity;
import org.springframework.data.repository.query.Param;

public interface ITestImportRepository
        extends JpaRepository<TestImportEntity, Long>, JpaSpecificationExecutor<TestImportEntity> {
    @Query("SELECT i.isdn FROM TestImportEntity i WHERE i.isdn IN :isdns")
    List<String> findExistingIsdns(@Param("isdns") List<String> isdns);

    @Modifying
    @Query("UPDATE TestImportEntity i SET i.time = :time WHERE i.isdn IN :isdns")
    void bulkUpdate(@Param("time") LocalDateTime time,
            @Param("isdns") List<String> isdns);
}
