package com.se1873.js.springboot.management.model;

import com.se1873.js.springboot.management.enums.AttendanceTypeENUM;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "attendance_types")
public class AttendanceType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "attendance_type_id")
  private int attendanceTypeId;

  @OneToMany(mappedBy = "attendanceType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Attendance> attendances;

  @Enumerated(EnumType.STRING)
  @Column(name = "attendance_type_name")
  private AttendanceTypeENUM attendanceTypeName;

  @Column(name = "is_deleted", columnDefinition = "BIT")
  private boolean isDeleted = Boolean.FALSE;
}
