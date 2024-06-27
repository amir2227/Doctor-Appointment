package com.blubank.doctorappointment.controllers;

import com.blubank.doctorappointment.mappers.AppointmentMapper;
import com.blubank.doctorappointment.mappers.PatientMapper;
import com.blubank.doctorappointment.models.Appointment;
import com.blubank.doctorappointment.models.Patient;
import com.blubank.doctorappointment.payloads.requests.CreatePatientDto;
import com.blubank.doctorappointment.payloads.responses.AppointmentRes;
import com.blubank.doctorappointment.payloads.responses.PatientRes;
import com.blubank.doctorappointment.services.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Patient")
@RequiredArgsConstructor
@RestController
@RequestMapping(PatientController.BASE_URL)
public class PatientController {
    public static final String  BASE_URL = "/v1/patient";
    private static final String CREATED_DESC = "Entity created successfully." +
            " Returns the location of the newly created entity in the 'Location' header.";

    private final PatientService patientService;

    @Operation(summary = "create a patient", description = CREATED_DESC)
    @PostMapping
    public ResponseEntity<Void> createPatient(@RequestBody @Valid CreatePatientDto dto){
        Patient patient = patientService.create(dto);
        return ResponseEntity.created(URI.create(BASE_URL+ "/"+patient.getId())).build();
    }

    @Operation(summary = "get a patient by id")
    @GetMapping("{patientId}")
    public ResponseEntity<PatientRes> getPatient(@PathVariable("patientId") Long patientId){
        Patient patient = patientService.getPatient(patientId);
        return ResponseEntity.ok(PatientMapper.mapToResponse(patient));
    }

    @Operation(summary = "get doctor open appointments")
    @GetMapping("doctor/{doctorId}/appointments")
    public ResponseEntity<List<AppointmentRes>> getDoctorAppointments(@PathVariable("doctorId") Long doctorId, @RequestParam LocalDateTime date) {
        List<Appointment> doctorAppointments = patientService.getDoctorAppointments(doctorId, date);
        return ResponseEntity.ok(doctorAppointments
                .stream()
                .map(AppointmentMapper::mapToPatientResponse)
                .collect(Collectors.toList()));
    }

    @Operation(summary = "book an appointment", description = CREATED_DESC)
    @PostMapping("/appointments/{appointmentId}")
    public ResponseEntity<Void> bookAppointment(@PathVariable Long appointmentId,@RequestBody @Valid CreatePatientDto dto) {
        patientService.bookAppointment(appointmentId, dto);
        return ResponseEntity.created(URI.create(BASE_URL+"/appointments/"+dto.getPhone())).build();
    }

    @Operation(summary = "get all appointments with patient phone number")
    @GetMapping("/appointments/{patientPhone}")
    public ResponseEntity<List<AppointmentRes>> getPatientAppointments(@PathVariable String patientPhone) {
        List<Appointment> patientAppointments = patientService.getPatientAppointments(patientPhone);
        return ResponseEntity.ok(patientAppointments
                .stream()
                .map(AppointmentMapper::mapToPatientResponse)
                .collect(Collectors.toList()));
    }
}
