package com.blubank.doctorappointment.payloads.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientRes {
    private Long id;
    private String name;
    private String phone;
}
