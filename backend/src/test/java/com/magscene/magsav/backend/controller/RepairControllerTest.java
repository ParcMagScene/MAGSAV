package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Repair;
import com.magscene.magsav.backend.entity.Repair.RepairStatus;
import com.magscene.magsav.backend.entity.Repair.RepairPriority;
import com.magscene.magsav.backend.repository.RepairRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour RepairController
 */
@ExtendWith(MockitoExtension.class)
class RepairControllerTest {

    @Mock
    private RepairRepository repairRepository;

    @InjectMocks
    private RepairController repairController;

    private Repair testRepair;

    @BeforeEach
    void setUp() {
        testRepair = new Repair();
        testRepair.setId(1L);
        testRepair.setRepairNumber("REP-TEST-001");
        testRepair.setEquipmentName("Console Yamaha QL5");
        testRepair.setStatus(RepairStatus.INITIATED);
        testRepair.setPriority(RepairPriority.NORMAL);
        testRepair.setProblemDescription("Console ne s'allume pas");
        testRepair.setCustomerName("Client Test");
        testRepair.setRequestDate(LocalDate.now());
    }

    @Test
    void getAllRepairs_ShouldReturnListOfRepairs() {
        // Arrange
        List<Repair> repairs = Arrays.asList(testRepair);
        when(repairRepository.findAll()).thenReturn(repairs);

        // Act
        ResponseEntity<List<Repair>> response = repairController.getAllRepairs();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getRepairNumber()).isEqualTo("REP-TEST-001");
        verify(repairRepository, times(1)).findAll();
    }

    @Test
    void getRepairById_WhenExists_ShouldReturnRepair() {
        // Arrange
        when(repairRepository.findById(1L)).thenReturn(Optional.of(testRepair));

        // Act
        ResponseEntity<Repair> response = repairController.getRepairById(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRepairNumber()).isEqualTo("REP-TEST-001");
        verify(repairRepository, times(1)).findById(1L);
    }

    @Test
    void getRepairById_WhenNotExists_ShouldReturnNotFound() {
        // Arrange
        when(repairRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Repair> response = repairController.getRepairById(999L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(repairRepository, times(1)).findById(999L);
    }

    @Test
    void createRepair_ShouldSaveAndReturnRepair() {
        // Arrange
        Repair newRepair = new Repair();
        newRepair.setEquipmentName("Enceinte L-Acoustics");
        newRepair.setStatus(RepairStatus.INITIATED);
        newRepair.setProblemDescription("Enceinte défectueuse");
        newRepair.setCustomerName("Nouveau Client");

        when(repairRepository.save(any(Repair.class))).thenReturn(testRepair);

        // Act
        ResponseEntity<Repair> response = repairController.createRepair(newRepair);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        verify(repairRepository, times(1)).save(any(Repair.class));
    }

    @Test
    void updateRepair_WhenExists_ShouldUpdateAndReturn() {
        // Arrange
        Repair updatedRepair = new Repair();
        updatedRepair.setEquipmentName("Console Yamaha QL5 Mise à jour");
        updatedRepair.setStatus(RepairStatus.IN_PROGRESS);
        updatedRepair.setDiagnosis("Carte mère défectueuse");

        when(repairRepository.findById(1L)).thenReturn(Optional.of(testRepair));
        when(repairRepository.save(any(Repair.class))).thenReturn(testRepair);

        // Act
        ResponseEntity<Repair> response = repairController.updateRepair(1L, updatedRepair);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(repairRepository, times(1)).findById(1L);
        verify(repairRepository, times(1)).save(any(Repair.class));
    }

    @Test
    void deleteRepair_WhenExists_ShouldDelete() {
        // Arrange
        when(repairRepository.findById(1L)).thenReturn(Optional.of(testRepair));
        doNothing().when(repairRepository).deleteById(1L);

        // Act
        ResponseEntity<Void> response = repairController.deleteRepair(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(repairRepository, times(1)).findById(1L);
        verify(repairRepository, times(1)).deleteById(1L);
    }

    @Test
    void getRepairsByStatus_ShouldReturnFilteredList() {
        // Arrange
        List<Repair> repairs = Arrays.asList(testRepair);
        when(repairRepository.findByStatus(RepairStatus.INITIATED)).thenReturn(repairs);

        // Act
        ResponseEntity<List<Repair>> response = repairController.getRepairsByStatus(RepairStatus.INITIATED);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(repairRepository, times(1)).findByStatus(RepairStatus.INITIATED);
    }

    @Test
    void getRepairsByPriority_ShouldReturnFilteredList() {
        // Arrange
        List<Repair> repairs = Arrays.asList(testRepair);
        when(repairRepository.findByPriority(RepairPriority.NORMAL)).thenReturn(repairs);

        // Act
        ResponseEntity<List<Repair>> response = repairController.getRepairsByPriority(RepairPriority.NORMAL);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(repairRepository, times(1)).findByPriority(RepairPriority.NORMAL);
    }

    @Test
    void getRepairStats_ShouldReturnStats() {
        // Arrange
        when(repairRepository.count()).thenReturn(10L);
        when(repairRepository.countByStatus(RepairStatus.INITIATED)).thenReturn(2L);
        when(repairRepository.countByStatus(RepairStatus.IN_PROGRESS)).thenReturn(3L);
        when(repairRepository.countByStatus(RepairStatus.COMPLETED)).thenReturn(5L);
        when(repairRepository.countByStatus(RepairStatus.CANCELLED)).thenReturn(0L);

        // Act
        ResponseEntity<?> response = repairController.getRepairStats();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
