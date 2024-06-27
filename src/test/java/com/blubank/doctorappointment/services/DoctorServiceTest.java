package com.blubank.doctorappointment.services;

import com.blubank.doctorappointment.exceptions.BadRequestException;
import com.blubank.doctorappointment.exceptions.NotFoundException;
import com.blubank.doctorappointment.models.Appointment;
import com.blubank.doctorappointment.models.Doctor;
import com.blubank.doctorappointment.models.Patient;
import com.blubank.doctorappointment.repositories.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor doctor;
    private Appointment appointment;
    private Patient patient;

    @BeforeEach
    void setUp() {
        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Dr. Who");

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDoctor(doctor);
        appointment.setStartTime(LocalDateTime.now().plusDays(1));
        appointment.setEndTime(appointment.getStartTime().plusMinutes(30));

        patient = new Patient();
        patient.setId(1L);
        patient.setName("Some Who");
        patient.setPhone("1234567890");
    }

    @Test
    void givenEndDateSoonerThanStartDate_whenAddAppointments_thenThrowBadRequestException() {
        Long doctorId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2023, 6, 20, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 6, 20, 9, 0);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                doctorService.addAppointments(doctorId, startTime, endTime)
        );

        assertEquals("Invalid time range", exception.getMessage());
    }

    @Test
    void givenTimePeriodLessThan30Minutes_whenAddAppointments_thenReturnEmptyList() {
        Long doctorId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2023, 6, 20, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 6, 20, 10, 20);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        List<Appointment> appointments = doctorService.addAppointments(doctorId, startTime, endTime);

        assertTrue(appointments.isEmpty());
    }

    @Test
    void givenNoAppointments_whenGetDoctorAppointments_thenReturnEmptyList() {
        Long doctorId = 1L;
        LocalDateTime date = LocalDateTime.of(2023, 6, 20, 10, 0);
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(appointmentService.getDoctorAppointments(doctor, startOfDay, endOfDay)).thenReturn(Collections.emptyList());

        List<Appointment> appointments = doctorService.getDoctorAppointments(doctorId, date);

        assertTrue(appointments.isEmpty());
    }

    @Test
    void givenTakenAppointments_whenGetDoctorAppointments_thenReturnAppointmentsWithPatientDetails() {
        Long doctorId = 1L;
        LocalDateTime date = LocalDateTime.of(2023, 6, 20, 10, 0);
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        appointment.setPatient(patient);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(appointmentService.getDoctorAppointments(doctor, startOfDay, endOfDay)).thenReturn(List.of(appointment));

        List<Appointment> appointments = doctorService.getDoctorAppointments(doctorId, date);

        assertFalse(appointments.isEmpty());
        assertEquals("Some Who", appointments.get(0).getPatient().getName());
        assertEquals("1234567890", appointments.get(0).getPatient().getPhone());
    }

    @Test
    void givenNoOpenAppointment_whenDeleteAppointment_thenThrowNotFoundException() {
        Long appointmentId = 1L;

        doThrow(new NotFoundException("Appointment not found")).when(appointmentService).deleteAppointment(appointmentId);
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                doctorService.deleteAppointment(appointmentId)
        );

        assertEquals("Appointment not found", exception.getMessage());
    }

    @Test
    void givenTakenAppointment_whenDeleteAppointment_thenThrowBadRequestException() {
        Long appointmentId = 1L;

        doThrow(new BadRequestException("Cannot delete taken appointment")).when(appointmentService).deleteAppointment(appointmentId);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                doctorService.deleteAppointment(appointmentId)
        );
        assertEquals("Cannot delete taken appointment", exception.getMessage());
    }

}