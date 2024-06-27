package com.blubank.doctorappointment.repositories;

import com.blubank.doctorappointment.models.Appointment;
import com.blubank.doctorappointment.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
    List<Appointment> findByDoctorAndStartTimeBetween(Doctor doctor, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByDoctor_IdAndPatientIsNullAndStartTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
}
