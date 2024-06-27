package com.blubank.doctorappointment.mappers;

import com.blubank.doctorappointment.models.Appointment;
import com.blubank.doctorappointment.payloads.responses.AppointmentRes;

public class AppointmentMapper {

    public static AppointmentRes mapToPatientResponse(Appointment appointment) {
        boolean isTaken = appointment.getPatient() != null;
        return AppointmentRes.builder()
                .id(appointment.getId())
                .isTaken(isTaken)
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .build();
    }
    public static AppointmentRes mapToDoctorResponse(Appointment appointment) {
        AppointmentRes res = mapToPatientResponse(appointment);
        res.setPatient(res.getIsTaken() ? PatientMapper.mapToResponse(appointment.getPatient()) : null);
        return res;
    }
}
