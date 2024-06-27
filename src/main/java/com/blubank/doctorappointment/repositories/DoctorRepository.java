package com.blubank.doctorappointment.repositories;

import com.blubank.doctorappointment.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
