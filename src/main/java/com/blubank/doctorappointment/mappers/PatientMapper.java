package com.blubank.doctorappointment.mappers;

import com.blubank.doctorappointment.models.Patient;
import com.blubank.doctorappointment.payloads.responses.PatientRes;

public class PatientMapper {

    public static PatientRes mapToResponse(Patient patient){
        return PatientRes.builder()
                .id(patient.getId())
                .name(patient.getName())
                .phone(patient.getPhone())
                .build();
    }
}
