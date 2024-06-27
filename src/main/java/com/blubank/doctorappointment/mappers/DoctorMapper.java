package com.blubank.doctorappointment.mappers;

import com.blubank.doctorappointment.models.Doctor;
import com.blubank.doctorappointment.payloads.responses.DoctorRes;

public class DoctorMapper {

    public static DoctorRes mapToResponse(Doctor doctor){
        return DoctorRes.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .build();
    }
}
