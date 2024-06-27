package com.blubank.doctorappointment.repositories;

import com.blubank.doctorappointment.models.Appointment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AppointmentPersistenceTest {
    @Autowired
    private AppointmentRepository repository;

    private Appointment savedEntity;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:doctor_appointment;MODE=Oracle");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

    }

    @BeforeEach
    void setupDb() {
        repository.deleteAll();

        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStartTime(LocalDateTime.now().plusDays(1));
        appointment.setEndTime(appointment.getStartTime().plusMinutes(30));
        savedEntity = repository.save(appointment);

        assertEqualsAppointment(appointment, savedEntity);
    }

    @Test
    @Transactional(value = Transactional.TxType.NEVER)
    void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        Appointment entity1 = repository.findById(savedEntity.getId()).get();
        Appointment entity2 = repository.findById(savedEntity.getId()).get();

        LocalDateTime entity1StartDate = LocalDateTime.now().plusDays(2);
        LocalDateTime entity2StartDate = LocalDateTime.now().plusDays(3);
        // Update the entity using the first entity object
        entity1.setStartTime(entity1StartDate);
        repository.save(entity1);

        // Update the entity using the second entity object.
        // This should fail since the second entity now holds an old version number,
        // an Optimistic Lock Error
        assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setStartTime(entity2StartDate);
            repository.save(entity2);
        });

        // Get the updated entity from the database and verify its new state
        Appointment updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long) updatedEntity.getVersion());
        assertEquals(entity1StartDate, updatedEntity.getStartTime());
    }

    private void assertEqualsAppointment(Appointment expectedEntity, Appointment actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getStartTime(), actualEntity.getStartTime());
        assertEquals(expectedEntity.getEndTime(), actualEntity.getEndTime());
    }
}
