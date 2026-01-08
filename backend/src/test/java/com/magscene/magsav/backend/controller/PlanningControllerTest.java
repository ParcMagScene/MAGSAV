package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Personnel;
import com.magscene.magsav.backend.entity.Vehicle;
import com.magscene.magsav.backend.repository.PersonnelRepository;
import com.magscene.magsav.backend.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour PlanningController
 */
@ExtendWith(MockitoExtension.class)
class PlanningControllerTest {

    @Mock
    private PersonnelRepository personnelRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private PlanningController planningController;

    private Personnel testPersonnel;
    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        testPersonnel = new Personnel();
        testPersonnel.setId(1L);
        testPersonnel.setFirstName("Jean");
        testPersonnel.setLastName("Dupont");
        testPersonnel.setDepartment("Technique");
        testPersonnel.setStatus(Personnel.PersonnelStatus.ACTIVE);

        testVehicle = new Vehicle();
        testVehicle.setId(1L);
        testVehicle.setLicensePlate("AB-123-CD");
        testVehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
    }

    @Test
    void getPlanningStatistics_ShouldReturnStats() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59);

        when(personnelRepository.countByStatus(Personnel.PersonnelStatus.ACTIVE)).thenReturn(10L);
        when(vehicleRepository.countByStatus(Vehicle.VehicleStatus.AVAILABLE)).thenReturn(5L);
        when(personnelRepository.findByStatus(Personnel.PersonnelStatus.ACTIVE))
                .thenReturn(Arrays.asList(testPersonnel));
        when(vehicleRepository.findAll())
                .thenReturn(Arrays.asList(testVehicle));

        // Act
        ResponseEntity<?> response = planningController.getPlanningStatistics(start, end);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void checkAvailability_ShouldReturnAvailableResources() {
        // Arrange
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(7);

        when(personnelRepository.findByStatus(Personnel.PersonnelStatus.ACTIVE))
                .thenReturn(Arrays.asList(testPersonnel));
        when(vehicleRepository.findByStatus(Vehicle.VehicleStatus.AVAILABLE))
                .thenReturn(Arrays.asList(testVehicle));

        // Act
        ResponseEntity<?> response = planningController.checkAvailability(start, end);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void detectConflicts_ShouldIdentifyOverlaps() {
        // Arrange
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(30);

        // Act
        ResponseEntity<?> response = planningController.detectConflicts(start, end);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getCompleteSchedule_ShouldReturnAllEvents() {
        // Arrange
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(7);

        // Act
        ResponseEntity<?> response = planningController.getCompleteSchedule(start, end, null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

}
