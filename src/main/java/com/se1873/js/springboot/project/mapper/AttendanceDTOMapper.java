package com.se1873.js.springboot.project.mapper;

import com.se1873.js.springboot.project.dto.AttendanceDTO;
import com.se1873.js.springboot.project.entity.Attendance;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
  componentModel = "spring",
  uses = {
    EmployeeDTOMapper.class,
    DepartmentDTOMapper.class,
    PositionDTOMapper.class,
  }
)
public abstract class AttendanceDTOMapper {

  @Mapping(target = "departmentId", ignore = true)
  @Mapping(target = "departmentName", ignore = true)

  @Mapping(target = "positionId", ignore = true)
  @Mapping(target = "positionName", ignore = true)
  @Mapping(target = "positionDescription", ignore = true)

  public abstract AttendanceDTO toDTO(Attendance attendance);

  @AfterMapping
  protected void mapNestedFields(
    Attendance attendance,
    @MappingTarget AttendanceDTO attendanceDTO
  ) {
    attendanceDTO.setAttendanceId(attendance.getAttendanceId());
    attendanceDTO.setAttendanceStatus(attendance.getStatus());
    attendanceDTO.setAttendanceCheckIn(attendance.getCheckIn());
    attendanceDTO.setAttendanceCheckOut(attendance.getCheckOut());
    attendanceDTO.setAttendanceOvertimeHours(attendance.getOvertimeHours());
    attendanceDTO.setAttendanceDate(attendance.getDate());
    attendanceDTO.setEmployeeId(attendance.getEmployee().getEmployeeId());
    attendanceDTO.setEmployeeFirstName(attendance.getEmployee().getFirstName());
    attendanceDTO.setEmployeeLastName(attendance.getEmployee().getLastName());
    attendanceDTO.setEmployeeCode(attendance.getEmployee().getEmployeeCode());
  }
}
