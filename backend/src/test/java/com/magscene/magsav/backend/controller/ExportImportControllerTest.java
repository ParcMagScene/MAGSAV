package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ExportImportController
 */
@ExtendWith(MockitoExtension.class)
class ExportImportControllerTest {

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private RepairRepository repairRepository;

    @Mock
    private RMARepository rmaRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private PersonnelRepository personnelRepository;

    @InjectMocks
    private ExportImportController exportImportController;

    @Test
    void getExportStatistics_ShouldReturnStats() {
        // Arrange
        when(serviceRequestRepository.count()).thenReturn(24L);
        when(repairRepository.count()).thenReturn(10L);
        when(rmaRepository.count()).thenReturn(5L);
        when(clientRepository.count()).thenReturn(6L);
        when(projectRepository.count()).thenReturn(6L);
        when(equipmentRepository.count()).thenReturn(100L);
        when(vehicleRepository.count()).thenReturn(10L);
        when(personnelRepository.count()).thenReturn(8L);

        // Act
        ResponseEntity<?> response = exportImportController.getExportStatistics();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Note: certains compteurs peuvent être appelés plusieurs fois
        verify(serviceRequestRepository, atLeastOnce()).count();
        verify(repairRepository, atLeastOnce()).count();
        verify(rmaRepository, atLeastOnce()).count();
        verify(clientRepository, atLeastOnce()).count();
        verify(projectRepository, atLeastOnce()).count();
        verify(equipmentRepository, atLeastOnce()).count();
        verify(vehicleRepository, atLeastOnce()).count();
        verify(personnelRepository, atLeastOnce()).count();
    }

    @Test
    void exportEquipmentCSV_ShouldReturnCsvContent() {
        // Arrange
        when(equipmentRepository.findAll()).thenReturn(java.util.Collections.emptyList());

        // Act
        ResponseEntity<?> response = exportImportController.exportEquipmentCSV();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(equipmentRepository, times(1)).findAll();
    }

    @Test
    void exportVehiclesCSV_ShouldReturnCsvContent() {
        // Arrange
        when(vehicleRepository.findAll()).thenReturn(java.util.Collections.emptyList());

        // Act
        ResponseEntity<?> response = exportImportController.exportVehiclesCSV();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(vehicleRepository, times(1)).findAll();
    }

    @Test
    void exportPersonnelCSV_ShouldReturnCsvContent() {
        // Arrange
        when(personnelRepository.findAll()).thenReturn(java.util.Collections.emptyList());

        // Act
        ResponseEntity<?> response = exportImportController.exportPersonnelCSV();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(personnelRepository, times(1)).findAll();
    }
}
