package com.blubank.doctorappointment.services;

import com.blubank.doctorappointment.exceptions.BadRequestException;
import com.blubank.doctorappointment.exceptions.ConflictException;
import com.blubank.doctorappointment.exceptions.NotFoundException;
import com.blubank.doctorappointment.models.Appointment;
import com.blubank.doctorappointment.models.Patient;
import com.blubank.doctorappointment.payloads.requests.CreatePatientDto;
import com.blubank.doctorappointment.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentService appointmentService;

    public Patient create(CreatePatientDto dto) {
        Patient patient = new Patient();
        patient.setName(dto.getName());
        patient.setPhone(dto.getPhone());
        return patientRepository.save(patient);
    }


    public List<Appointment> getPatientAppointments(String patientPhone) {
        Patient patient = getPatientByPhone(patientPhone)
                .orElseThrow(() -> new NotFoundException("Patient not found"));
        return patient.getAppointments();
    }

    public Appointment bookAppointment(Long appointmentId,CreatePatientDto dto) {
        if(dto.getPhone() == null || dto.getName() == null)
            throw new BadRequestException("name and phone required.");
        Patient patient = getPatientByPhone(dto.getPhone())
                .orElse(create(dto));
        try {
            return appointmentService.bookAppointment(appointmentId, patient);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ConflictException("Appointment is being modified by another transaction");
        }
    }

    public List<Appointment> getDoctorAppointments(Long doctorId, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return appointmentService.getDoctorAppointmentsForPatient(doctorId, startOfDay, endOfDay);
    }

    protected Optional<Patient> getPatientByPhone(String patientPhone) {
        return patientRepository.findByPhone(patientPhone);
    }

    public Patient getPatient(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found"));
    }
}
