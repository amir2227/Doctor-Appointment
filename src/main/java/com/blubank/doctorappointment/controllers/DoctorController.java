package com.blubank.doctorappointment.controllers;

import com.blubank.doctorappointment.mappers.AppointmentMapper;
import com.blubank.doctorappointment.mappers.DoctorMapper;
import com.blubank.doctorappointment.models.Appointment;
import com.blubank.doctorappointment.models.Doctor;
import com.blubank.doctorappointment.payloads.requests.AddAppointmentDto;
import com.blubank.doctorappointment.payloads.requests.CreateDoctorDto;
import com.blubank.doctorappointment.payloads.responses.AppointmentRes;
import com.blubank.doctorappointment.payloads.responses.DoctorRes;
import com.blubank.doctorappointment.services.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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


@Tag(name = "Doctor")
@RequiredArgsConstructor
@RestController
@RequestMapping(DoctorController.BASE_URL)
public class DoctorController {
    public static final String  BASE_URL = "/v1/doctor";
    private static final String CREATED_DESC = "Entity created successfully." +
            " Returns the location of the newly created entity in the 'Location' header.";
    private final DoctorService doctorService;

    @Operation(summary = "create a doctor", description = CREATED_DESC)
    @PostMapping
    public ResponseEntity<Void> createDoctor(@RequestBody @Valid CreateDoctorDto dto){
        Doctor doctor = doctorService.create(dto);
        return ResponseEntity.created(URI.create(BASE_URL+ "/"+doctor.getId())).build();
    }

    @GetMapping("{doctorId}")
    public ResponseEntity<DoctorRes> getDoctor(@PathVariable("doctorId") Long doctorId){
        Doctor doctor = doctorService.getDoctor(doctorId);
        return ResponseEntity.ok(DoctorMapper.mapToResponse(doctor));
    }

    @Operation(summary = "add daily appointments for a doctor" , description = CREATED_DESC)
    @PostMapping("/{doctorId}/appointments")
    public ResponseEntity<Void> addAppointments(@PathVariable("doctorId") Long doctorId,@RequestBody @Valid AddAppointmentDto dto) {
        doctorService.addAppointments(doctorId, dto.getStartTime(),dto.getEndTime());
        return ResponseEntity.created(URI.create(BASE_URL+ "/"+doctorId+"/appointments")).build();
    }
    @Operation(summary = "get daily appointments for a doctor")
    @GetMapping("/{doctorId}/appointments")
    public ResponseEntity<List<AppointmentRes>> getDoctorAppointments(@PathVariable("doctorId") Long doctorId, @RequestParam LocalDateTime date) {
        List<Appointment> doctorAppointments = doctorService.getDoctorAppointments(doctorId, date);
        return ResponseEntity.ok(doctorAppointments
                .stream()
                .map(AppointmentMapper::mapToDoctorResponse)
                .collect(Collectors.toList()));
    }

    @Operation(summary = "delete appointment")
    @DeleteMapping("/appointments/{appointmentId}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable("appointmentId") Long appointmentId) {
        doctorService.deleteAppointment(appointmentId);
        return ResponseEntity.ok().build();
    }
}
