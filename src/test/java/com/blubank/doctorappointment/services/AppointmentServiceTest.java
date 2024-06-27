package com.blubank.doctorappointment.services;

import com.blubank.doctorappointment.exceptions.BadRequestException;
import com.blubank.doctorappointment.exceptions.NotFoundException;
import com.blubank.doctorappointment.models.Appointment;
import com.blubank.doctorappointment.models.Doctor;
import com.blubank.doctorappointment.models.Patient;
import com.blubank.doctorappointment.repositories.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @InjectMocks
    private AppointmentService appointmentService;

    private static final Long VALID_APPOINTMENT_ID = 1L;
    private static final Long INVALID_APPOINTMENT_ID = 999L;

    private Patient patient;
    private Doctor doctor;
    private Appointment appointment;

    @BeforeEach
    public void setUp() {
        patient = new Patient();
        patient.setName("John Doe");
        patient.setPhone("123456789");

        doctor = new Doctor();
        doctor.setName("Jane Smith");

        LocalDateTime startTime = LocalDateTime.of(2023, 4, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 4, 1, 11, 0);

        appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(null); // Ensure appointment is not booked initially
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
    }

    @Test
    public void addAppointmentsSuccessfully() {
        LocalDateTime startTime = LocalDateTime.of(2023, 4, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 4, 1, 12, 0);

        when(appointmentRepository.saveAll(any(List.class))).thenReturn(List.of(
                new Appointment(), new Appointment(), new Appointment()));

        List<Appointment> appointments = appointmentService.addAppointments(doctor, startTime, endTime);

        assertEquals(3, appointments.size());
        verify(appointmentRepository, times(1)).saveAll(any(List.class));
    }
    @Test
    public void addAppointmentsWhenEndTimeIsExactly30MinutesAfterStartTime() {
        LocalDateTime startTime = LocalDateTime.of(2023, 4, 1, 10, 0);
        LocalDateTime endTime = startTime.plusMinutes(30);

        when(appointmentRepository.saveAll(any(List.class))).thenReturn(List.of(new Appointment()));

        List<Appointment> appointments = appointmentService.addAppointments(doctor, startTime, endTime);

        assertEquals(1, appointments.size());
        verify(appointmentRepository, times(1)).saveAll(any(List.class));
    }
    @Test
    public void addAppointmentsWithEmptySlot() {
        LocalDateTime startTime = LocalDateTime.of(2023, 4, 1, 10, 30);
        LocalDateTime endTime = LocalDateTime.of(2023, 4, 1, 11, 0);

        when(appointmentRepository.saveAll(any(List.class))).thenReturn(List.of());

        List<Appointment> appointments = appointmentService.addAppointments(doctor, startTime, endTime);

        assertEquals(0, appointments.size());
        verify(appointmentRepository, times(1)).saveAll(any(List.class));
    }
    @Test
    public void bookAppointmentSuccessfully() {
        when(appointmentRepository.findById(VALID_APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        Appointment bookedAppointment = appointmentService.bookAppointment(VALID_APPOINTMENT_ID, patient);

        verify(appointmentRepository, times(1)).findById(VALID_APPOINTMENT_ID);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));

        assertEquals(patient, bookedAppointment.getPatient());
    }
    @Test
    public void bookAppointmentAlreadyTaken() {
        when(appointmentRepository.findById(VALID_APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        appointment.setPatient(new Patient()); // Simulate an already booked appointment
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            appointmentService.bookAppointment(VALID_APPOINTMENT_ID, patient);
        });
        assertEquals("Appointment is already taken", exception.getMessage());
    }
    @Test
    public void bookAppointmentWithInvalidId() {
        when(appointmentRepository.findById(INVALID_APPOINTMENT_ID)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            appointmentService.bookAppointment(INVALID_APPOINTMENT_ID, patient);
        });
        assertEquals("Appointment not found", exception.getMessage());
        verify(appointmentRepository, times(1)).findById(INVALID_APPOINTMENT_ID);
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    public void testDeleteAppointment_ExistingAppointment_Success() {
        when(appointmentRepository.findById(VALID_APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        appointmentService.deleteAppointment(VALID_APPOINTMENT_ID);
        verify(appointmentRepository, times(1)).delete(any(Appointment.class));
    }
    @Test
    public void testDeleteAppointment_TakenAppointment_ThrowsBadRequestException() {
        appointment.setPatient(patient);
        when(appointmentRepository.findById(VALID_APPOINTMENT_ID))
                .thenReturn(Optional.of(appointment));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> appointmentService.deleteAppointment(VALID_APPOINTMENT_ID));

        assertEquals("Cannot delete taken appointment",exception.getMessage());
    }
    @Test
    public void testDeleteAppointment_NonExistingAppointment_NotFoundException() {
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> appointmentService.deleteAppointment(INVALID_APPOINTMENT_ID));
        assertEquals("Appointment not found", exception.getMessage());
        verify(appointmentRepository, never()).delete(any(Appointment.class));
    }

}
