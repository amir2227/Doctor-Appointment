package com.blubank.doctorappointment.services;

import com.blubank.doctorappointment.exceptions.BadRequestException;
import com.blubank.doctorappointment.exceptions.NotFoundException;
import com.blubank.doctorappointment.models.Appointment;
import com.blubank.doctorappointment.models.Doctor;
import com.blubank.doctorappointment.models.Patient;
import com.blubank.doctorappointment.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;

    protected List<Appointment> addAppointments(Doctor doctor, LocalDateTime startTime, LocalDateTime endTime) {
        List<Appointment> appointments = new ArrayList<>();
        while (startTime.plusMinutes(30).isBefore(endTime) || startTime.plusMinutes(30).equals(endTime)) {
            Appointment appointment = new Appointment();
            appointment.setDoctor(doctor);
            appointment.setStartTime(startTime);
            appointment.setEndTime(startTime.plusMinutes(30));
            appointments.add(appointment);
            startTime = startTime.plusMinutes(30);
        }
        return appointmentRepository.saveAll(appointments);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Appointment bookAppointment(Long appointmentId, Patient patient) {
        Appointment appointment = getAppointment(appointmentId);
        if (appointment.getPatient() != null) {
            throw new BadRequestException("Appointment already taken");
        }
        appointment.setPatient(patient);
        return appointmentRepository.save(appointment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void deleteAppointment(Long appointmentId) {
        Appointment appointment = getAppointment(appointmentId);
        if (appointment.getPatient() != null) {
            throw new BadRequestException("Cannot delete taken appointment");
        }
        appointmentRepository.delete(appointment);
    }

    protected List<Appointment> getDoctorAppointments(Doctor doctor, LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findByDoctorAndStartTimeBetween(doctor, startDate, endDate);
    }

    protected List<Appointment> getDoctorAppointmentsForPatient(Long doctorId, LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findByDoctor_IdAndPatientIsNullAndStartTimeBetween(doctorId, startDate, endDate);
    }

    protected Appointment getAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));
    }

}
