package com.blubank.doctorappointment.payloads.responses;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AppointmentRes {
    private Long id;
    private Boolean isTaken;
    private PatientRes patient;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
