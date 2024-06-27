package com.blubank.doctorappointment.services;

import com.blubank.doctorappointment.exceptions.BadRequestException;
import com.blubank.doctorappointment.exceptions.ConflictException;
import com.blubank.doctorappointment.exceptions.NotFoundException;
import com.blubank.doctorappointment.models.Appointment;
import com.blubank.doctorappointment.models.Doctor;
import com.blubank.doctorappointment.payloads.requests.CreateDoctorDto;
import com.blubank.doctorappointment.repositories.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final AppointmentService appointmentService;

    public Doctor create(CreateDoctorDto dto) {
        Doctor doctor = new Doctor();
        doctor.setName(dto.getName());
        return doctorRepository.save(doctor);
    }

    public List<Appointment> addAppointments(Long doctorId, LocalDateTime startTime, LocalDateTime endTime) {
        Doctor doctor = getDoctor(doctorId);
        boolean isNotValidTimeRange = endTime.isBefore(startTime)
                || Duration.between(startTime, endTime).toHours() > 24;
        if (isNotValidTimeRange) {
            throw new BadRequestException("Invalid time range");
        }
        if(Duration.between(startTime, endTime).toMinutes() < 30) return new ArrayList<>();
        return appointmentService.addAppointments(doctor, startTime, endTime);
    }

    public List<Appointment> getDoctorAppointments(Long doctorId, LocalDateTime date) {
        Doctor doctor = getDoctor(doctorId);
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return appointmentService.getDoctorAppointments(doctor, startOfDay, endOfDay);
    }

    public Doctor getDoctor(Long doctorId) {
        return doctorRepository.findById(doctorId).orElseThrow(() -> new NotFoundException("Doctor not found"));
    }

    @Transactional(readOnly = true)
    public void deleteAppointment(Long appointmentId) {
        try {
            appointmentService.deleteAppointment(appointmentId);
        }catch (ObjectOptimisticLockingFailureException e){
            throw new ConflictException("Appointment is being modified by another transaction");
        }

    }
}
