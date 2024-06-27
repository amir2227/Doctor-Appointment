package com.blubank.doctorappointment.payloads.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorRes {
    private Long id;
    private String name;
}
