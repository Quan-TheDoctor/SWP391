package com.se1873.js.springboot.project.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.se1873.js.springboot.project.dto.RequestCreationResponseDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name="reponse")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class response {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer requestId;

    private String fullName;

    private String employeeCode;

    private String positionName;

    private Double oldBaseSalary;

    private Double newBaseSalary;

    @JsonFormat(pattern = "EEEE yyyy-MM-dd")
    private LocalDate startedAt;

    public static response toEntity (RequestCreationResponseDTO dto) {
        if (dto == null) {
            return null;
        }

        response entity = new response();
        entity.setRequestId(dto.getRequestId());
        entity.setFullName(dto.getFullName());
        entity.setEmployeeCode(dto.getEmployeeCode());
        entity.setPositionName(dto.getPositionName());
        entity.setOldBaseSalary(dto.getOldBaseSalary());
        entity.setNewBaseSalary(dto.getNewBaseSalary());
        entity.setStartedAt(dto.getStartedAt());

        return entity;
    }
}
