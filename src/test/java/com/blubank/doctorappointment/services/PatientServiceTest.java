package com.blubank.doctorappointment.services;

import com.blubank.doctorappointment.exceptions.BadRequestException;
import com.blubank.doctorappointment.exceptions.ConflictException;
import com.blubank.doctorappointment.exceptions.NotFoundException;
import com.blubank.doctorappointment.models.Appointment;
import com.blubank.doctorappointment.models.Patient;
import com.blubank.doctorappointment.payloads.requests.CreatePatientDto;
import com.blubank.doctorappointment.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private AppointmentService appointmentService;
    @InjectMocks
    private PatientService patientService;

    private Long doctorId;
    private LocalDateTime date;

    @BeforeEach
    public void setUp() {
        doctorId = 1L;
        date = LocalDateTime.now();
    }

    @Test
    public void testGetDoctorAppointmentsForGivenDay_NoOpenAppointments() {
        // Arrange
        when(appointmentService.getDoctorAppointmentsForPatient(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<Appointment> openAppointments = patientService.getDoctorAppointments(doctorId, date);

        // Assert
        assertEquals(0, openAppointments.size());
        verify(appointmentService, times(1))
                .getDoctorAppointmentsForPatient(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }
    @Test
    public void testGetDoctorAppointmentsForGivenDay_WithOpenAppointments() {
        // Arrange
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        Appointment appointment = new Appointment();
        appointment.setStartTime(startOfDay.plusHours(1));
        appointment.setEndTime(startOfDay.plusHours(1).plusMinutes(30));

        when(appointmentService.getDoctorAppointmentsForPatient(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(appointment));

        // Act
        List<Appointment> openAppointments = patientService.getDoctorAppointments(doctorId, date);

        // Assert
        assertEquals(1, openAppointments.size());
        assertEquals(appointment, openAppointments.get(0));
        verify(appointmentService, times(1))
                .getDoctorAppointmentsForPatient(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    public void testBookAppointment_MissingPhoneNumber() {
        // Arrange
        CreatePatientDto dto = new CreatePatientDto();
        dto.setName("Bla Bla");

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> patientService.bookAppointment(1L, dto));
    }
    @Test
    public void testBookAppointment_MissingName() {
        // Arrange
        CreatePatientDto dto = new CreatePatientDto();
        dto.setPhone("1234567890");

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> patientService.bookAppointment(1L, dto));
    }
    @Test
    public void testBookAppointment_AppointmentAlreadyTaken() {
        // Arrange
        CreatePatientDto dto = new CreatePatientDto();
        dto.setName("Bla Bla");
        dto.setPhone("1234567890");
        Patient patient = new Patient();
        patient.setName(dto.getName());
        patient.setPhone(dto.getPhone());

        when(patientRepository.findByPhone(dto.getPhone())).thenReturn(Optional.of(patient));
        when(appointmentService.bookAppointment(anyLong(), any(Patient.class)))
                .thenThrow(new BadRequestException("Appointment already taken"));

        // Act & Assert
      BadRequestException exception =  assertThrows(BadRequestException.class,
              () -> patientService.bookAppointment(1L, dto));
      assertEquals("Appointment already taken",exception.getMessage());
    }

    @Test
    public void testBookAppointment_AppointmentDeleted() {
        // Arrange
        CreatePatientDto dto = new CreatePatientDto();
        dto.setName("Bla Bla");
        dto.setPhone("1234567890");
        Patient patient = new Patient();
        patient.setName(dto.getName());
        patient.setPhone(dto.getPhone());

        when(patientRepository.findByPhone(dto.getPhone())).thenReturn(Optional.of(patient));
        when(appointmentService.bookAppointment(anyLong(), any(Patient.class)))
                .thenThrow(new NotFoundException("Appointment not found"));

        // Act & Assert
       NotFoundException exception = assertThrows(NotFoundException.class,
               () -> patientService.bookAppointment(1L, dto));
       assertEquals("Appointment not found",exception.getMessage());
    }
    @Test
    public void testBookAppointment_ConcurrencyIssue() {
        // Arrange
        CreatePatientDto dto = new CreatePatientDto();
        dto.setName("bla bla bla");
        dto.setPhone("1234567890");
        Patient patient = new Patient();
        patient.setName(dto.getName());
        patient.setPhone(dto.getPhone());

        when(patientRepository.findByPhone(dto.getPhone())).thenReturn(Optional.of(patient));
        when(appointmentService.bookAppointment(anyLong(), any(Patient.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Appointment.class,
                        "Appointment is being modified by another transaction"));

        // Act & Assert
      ConflictException exception =  assertThrows(ConflictException.class,
              () -> patientService.bookAppointment(1L, dto));

      assertEquals("Appointment is being modified by another transaction", exception.getMessage());
    }
    @Test
    public void testBookAppointment_Success() {
        // Arrange
        CreatePatientDto dto = new CreatePatientDto();
        dto.setName("hoooommmm");
        dto.setPhone("1234567890");
        Patient patient = new Patient();
        patient.setName(dto.getName());
        patient.setPhone(dto.getPhone());

        Appointment appointment = new Appointment();
        appointment.setId(1L);

        when(patientRepository.findByPhone(dto.getPhone())).thenReturn(Optional.of(patient));
        when(appointmentService.bookAppointment(anyLong(), any(Patient.class))).thenReturn(appointment);

        // Act
        Appointment bookedAppointment = patientService.bookAppointment(1L, dto);

        // Assert
        assertNotNull(bookedAppointment);
        assertEquals(appointment, bookedAppointment);
    }
    @Test
    public void testGetPatientAppointments_NoAppointments() {
        // Arrange
        String phoneNumber = "1234567890";
        Patient patient = new Patient();
        patient.setPhone(phoneNumber);

        when(patientRepository.findByPhone(phoneNumber)).thenReturn(Optional.of(patient));

        List<Appointment> appointments = patientService.getPatientAppointments(phoneNumber);

        assertTrue(appointments.isEmpty());

    }

    @Test
    public void testGetPatientAppointments_MultipleAppointments() {
        // Arrange
        String phoneNumber = "1234567890";
        Patient patient = new Patient();
        patient.setPhone(phoneNumber);

        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        patient.setAppointments(List.of(appointment1, appointment2));

        when(patientRepository.findByPhone(phoneNumber)).thenReturn(Optional.of(patient));

        // Act
        List<Appointment> appointments = patientService.getPatientAppointments(phoneNumber);

        // Assert
        assertEquals(2, appointments.size());
        assertEquals(List.of(appointment1, appointment2), appointments);
        verify(patientRepository, times(1)).findByPhone(phoneNumber);
    }
}