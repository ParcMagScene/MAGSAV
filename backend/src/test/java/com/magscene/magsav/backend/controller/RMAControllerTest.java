package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.RMA;
import com.magscene.magsav.backend.entity.RMA.RMAStatus;
import com.magscene.magsav.backend.entity.RMA.RMAReasonType;
import com.magscene.magsav.backend.entity.RMA.RMAPriority;
import com.magscene.magsav.backend.repository.RMARepository;
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
 * Tests unitaires pour RMAController
 */
@ExtendWith(MockitoExtension.class)
class RMAControllerTest {

    @Mock
    private RMARepository rmaRepository;

    @InjectMocks
    private RMAController rmaController;

    private RMA testRMA;

    @BeforeEach
    void setUp() {
        testRMA = new RMA();
        testRMA.setId(1L);
        testRMA.setRmaNumber("RMA-TEST-001");
        testRMA.setEquipmentName("Console DiGiCo SD9");
        testRMA.setEquipmentSerialNumber("SN12345");
        testRMA.setReason(RMAReasonType.MANUFACTURING_DEFECT);
        testRMA.setStatus(RMAStatus.INITIATED);
        testRMA.setPriority(RMAPriority.NORMAL);
        testRMA.setDescription("Produit défectueux");
        testRMA.setCustomerName("Client Test");
        testRMA.setRequestDate(LocalDate.now());
    }

    @Test
    void getAllRMAs_ShouldReturnListOfRMAs() {
        // Arrange
        List<RMA> rmas = Arrays.asList(testRMA);
        when(rmaRepository.findAll()).thenReturn(rmas);

        // Act
        ResponseEntity<List<RMA>> response = rmaController.getAllRMAs();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getRmaNumber()).isEqualTo("RMA-TEST-001");
        verify(rmaRepository, times(1)).findAll();
    }

    @Test
    void getRMAById_WhenExists_ShouldReturnRMA() {
        // Arrange
        when(rmaRepository.findById(1L)).thenReturn(Optional.of(testRMA));

        // Act
        ResponseEntity<RMA> response = rmaController.getRMAById(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRmaNumber()).isEqualTo("RMA-TEST-001");
        verify(rmaRepository, times(1)).findById(1L);
    }

    @Test
    void getRMAById_WhenNotExists_ShouldReturnNotFound() {
        // Arrange
        when(rmaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<RMA> response = rmaController.getRMAById(999L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(rmaRepository, times(1)).findById(999L);
    }

    @Test
    void createRMA_ShouldSaveAndReturnRMA() {
        // Arrange
        RMA newRMA = new RMA();
        newRMA.setEquipmentName("Enceinte L-Acoustics");
        newRMA.setReason(RMAReasonType.MANUFACTURING_DEFECT);
        newRMA.setStatus(RMAStatus.INITIATED);
        newRMA.setDescription("Retour produit défectueux");
        newRMA.setCustomerName("Nouveau Client");

        when(rmaRepository.save(any(RMA.class))).thenReturn(testRMA);

        // Act
        ResponseEntity<RMA> response = rmaController.createRMA(newRMA);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        verify(rmaRepository, times(1)).save(any(RMA.class));
    }

    @Test
    void updateRMA_WhenExists_ShouldUpdateAndReturn() {
        // Arrange
        RMA updatedRMA = new RMA();
        updatedRMA.setEquipmentName("Console DiGiCo SD9 Mise à jour");
        updatedRMA.setStatus(RMAStatus.AUTHORIZED);
        updatedRMA.setAnalysisNotes("Analyse terminée");

        when(rmaRepository.findById(1L)).thenReturn(Optional.of(testRMA));
        when(rmaRepository.save(any(RMA.class))).thenReturn(testRMA);

        // Act
        ResponseEntity<RMA> response = rmaController.updateRMA(1L, updatedRMA);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(rmaRepository, times(1)).findById(1L);
        verify(rmaRepository, times(1)).save(any(RMA.class));
    }

    @Test
    void deleteRMA_WhenExists_ShouldDelete() {
        // Arrange
        when(rmaRepository.findById(1L)).thenReturn(Optional.of(testRMA));
        doNothing().when(rmaRepository).deleteById(1L);

        // Act
        ResponseEntity<Void> response = rmaController.deleteRMA(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(rmaRepository, times(1)).findById(1L);
        verify(rmaRepository, times(1)).deleteById(1L);
    }

    @Test
    void getRMAsByStatus_ShouldReturnFilteredList() {
        // Arrange
        List<RMA> rmas = Arrays.asList(testRMA);
        when(rmaRepository.findByStatus(RMAStatus.INITIATED)).thenReturn(rmas);

        // Act
        ResponseEntity<List<RMA>> response = rmaController.getRMAsByStatus(RMAStatus.INITIATED);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(rmaRepository, times(1)).findByStatus(RMAStatus.INITIATED);
    }

    @Test
    void getRMAsByReason_ShouldReturnFilteredList() {
        // Arrange
        List<RMA> rmas = Arrays.asList(testRMA);
        when(rmaRepository.findByReason(RMAReasonType.MANUFACTURING_DEFECT)).thenReturn(rmas);

        // Act
        ResponseEntity<List<RMA>> response = rmaController.getRMAsByReason(RMAReasonType.MANUFACTURING_DEFECT);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(rmaRepository, times(1)).findByReason(RMAReasonType.MANUFACTURING_DEFECT);
    }

    @Test
    void authorizeRMA_WhenExists_ShouldAuthorize() {
        // Arrange
        when(rmaRepository.findById(1L)).thenReturn(Optional.of(testRMA));
        when(rmaRepository.save(any(RMA.class))).thenReturn(testRMA);

        // Act
        ResponseEntity<RMA> response = rmaController.authorizeRMA(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(rmaRepository, times(1)).findById(1L);
        verify(rmaRepository, times(1)).save(any(RMA.class));
    }

    @Test
    void getRMAsByPeriod_ShouldReturnFilteredList() {
        // Arrange
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();
        List<RMA> rmas = Arrays.asList(testRMA);
        when(rmaRepository.findByRequestDateBetween(start, end)).thenReturn(rmas);

        // Act
        ResponseEntity<List<RMA>> response = rmaController.getRMAsByPeriod(start, end);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(rmaRepository, times(1)).findByRequestDateBetween(start, end);
    }

    @Test
    void getRMAStats_ShouldReturnStats() {
        // Arrange
        when(rmaRepository.count()).thenReturn(15L);
        when(rmaRepository.countByStatus(RMAStatus.INITIATED)).thenReturn(3L);
        when(rmaRepository.countByStatus(RMAStatus.AUTHORIZED)).thenReturn(5L);
        when(rmaRepository.countByStatus(RMAStatus.IN_TRANSIT_RETURN)).thenReturn(2L);
        when(rmaRepository.countByStatus(RMAStatus.RECEIVED)).thenReturn(1L);
        when(rmaRepository.countByStatus(RMAStatus.UNDER_ANALYSIS)).thenReturn(1L);
        when(rmaRepository.countByStatus(RMAStatus.COMPLETED)).thenReturn(3L);
        when(rmaRepository.countByStatus(RMAStatus.REJECTED)).thenReturn(0L);

        // Act
        ResponseEntity<?> response = rmaController.getRMAStats();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
