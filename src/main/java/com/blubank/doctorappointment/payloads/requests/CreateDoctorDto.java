package com.blubank.doctorappointment.payloads.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class CreateDoctorDto {
    @NotEmpty
    @Size(min = 3, max = 64)
    private String name;
}
