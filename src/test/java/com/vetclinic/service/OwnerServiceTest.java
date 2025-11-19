package com.vetclinic.service;

import com.vetclinic.dto.owner.CreateOwnerRequest;
import com.vetclinic.dto.owner.OwnerDTO;
import com.vetclinic.dto.owner.UpdateOwnerRequest;
import com.vetclinic.entity.Owner;
import com.vetclinic.exception.DuplicateResourceException;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.repository.OwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerServiceTest {

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private OwnerService ownerService;

    private Owner testOwner;
    private CreateOwnerRequest createRequest;
    private UpdateOwnerRequest updateRequest;

    @BeforeEach
    void setUp() {
        testOwner = new Owner();
        testOwner.setId(1L);
        testOwner.setFirstName("John");
        testOwner.setLastName("Doe");
        testOwner.setEmail("john@example.com");
        testOwner.setPhone("1234567890");
        testOwner.setAlternativePhone("0987654321");
        testOwner.setAddress("123 Main St");
        testOwner.setCity("Springfield");
        testOwner.setPostalCode("12345");
        testOwner.setDocumentType("DNI");
        testOwner.setDocumentNumber("12345678");
        testOwner.setNotes("Regular customer");
        testOwner.setIsActive(true);

        createRequest = new CreateOwnerRequest();
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setEmail("john@example.com");
        createRequest.setPhone("1234567890");
        createRequest.setAlternativePhone("0987654321");
        createRequest.setAddress("123 Main St");
        createRequest.setCity("Springfield");
        createRequest.setPostalCode("12345");
        createRequest.setDocumentType("DNI");
        createRequest.setDocumentNumber("12345678");
        createRequest.setNotes("Regular customer");

        updateRequest = new UpdateOwnerRequest();
        updateRequest.setFirstName("John Updated");
        updateRequest.setPhone("1111111111");
    }

    @Test
    void createOwner_Success() {
        // Arrange
        when(ownerRepository.existsByEmailAndIsActiveTrue(anyString())).thenReturn(false);
        when(ownerRepository.existsByDocumentNumberAndIsActiveTrue(anyString())).thenReturn(false);
        when(ownerRepository.save(any(Owner.class))).thenReturn(testOwner);

        // Act
        OwnerDTO result = ownerService.createOwner(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testOwner.getFirstName(), result.getFirstName());
        assertEquals(testOwner.getEmail(), result.getEmail());

        verify(ownerRepository).existsByEmailAndIsActiveTrue(anyString());
        verify(ownerRepository).existsByDocumentNumberAndIsActiveTrue(anyString());
        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    void createOwner_DuplicateEmail_ThrowsDuplicateResourceException() {
        // Arrange
        when(ownerRepository.existsByEmailAndIsActiveTrue(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> ownerService.createOwner(createRequest));

        verify(ownerRepository).existsByEmailAndIsActiveTrue(anyString());
        verify(ownerRepository, never()).save(any(Owner.class));
    }

    @Test
    void createOwner_DuplicateDocumentNumber_ThrowsDuplicateResourceException() {
        // Arrange
        when(ownerRepository.existsByEmailAndIsActiveTrue(anyString())).thenReturn(false);
        when(ownerRepository.existsByDocumentNumberAndIsActiveTrue(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> ownerService.createOwner(createRequest));

        verify(ownerRepository).existsByEmailAndIsActiveTrue(anyString());
        verify(ownerRepository).existsByDocumentNumberAndIsActiveTrue(anyString());
        verify(ownerRepository, never()).save(any(Owner.class));
    }

    @Test
    void createOwner_WithoutDocumentNumber_Success() {
        // Arrange
        createRequest.setDocumentNumber(null);
        when(ownerRepository.existsByEmailAndIsActiveTrue(anyString())).thenReturn(false);
        when(ownerRepository.save(any(Owner.class))).thenReturn(testOwner);

        // Act
        OwnerDTO result = ownerService.createOwner(createRequest);

        // Assert
        assertNotNull(result);
        verify(ownerRepository).existsByEmailAndIsActiveTrue(anyString());
        verify(ownerRepository, never()).existsByDocumentNumberAndIsActiveTrue(anyString());
        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    void getAllOwners_Success() {
        // Arrange
        when(ownerRepository.findAllByIsActiveTrueOrderByLastNameAsc())
                .thenReturn(List.of(testOwner));

        // Act
        List<OwnerDTO> result = ownerService.getAllOwners();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOwner.getFirstName(), result.get(0).getFirstName());

        verify(ownerRepository).findAllByIsActiveTrueOrderByLastNameAsc();
    }

    @Test
    void getAllOwners_Empty_ReturnsEmptyList() {
        // Arrange
        when(ownerRepository.findAllByIsActiveTrueOrderByLastNameAsc())
                .thenReturn(List.of());

        // Act
        List<OwnerDTO> result = ownerService.getAllOwners();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(ownerRepository).findAllByIsActiveTrueOrderByLastNameAsc();
    }

    @Test
    void getOwnersPage_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Owner> ownerPage = new PageImpl<>(List.of(testOwner));
        when(ownerRepository.findAllByIsActiveTrue(pageable)).thenReturn(ownerPage);

        // Act
        Page<OwnerDTO> result = ownerService.getOwnersPage(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(ownerRepository).findAllByIsActiveTrue(pageable);
    }

    @Test
    void getOwnerById_Success() {
        // Arrange
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));

        // Act
        OwnerDTO result = ownerService.getOwnerById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testOwner.getFirstName(), result.getFirstName());

        verify(ownerRepository).findById(anyLong());
    }

    @Test
    void getOwnerById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> ownerService.getOwnerById(999L));

        verify(ownerRepository).findById(anyLong());
    }

    @Test
    void getOwnerById_NotActive_ThrowsResourceNotFoundException() {
        // Arrange
        testOwner.setIsActive(false);
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> ownerService.getOwnerById(1L));

        verify(ownerRepository).findById(anyLong());
    }

    @Test
    void searchOwners_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Owner> ownerPage = new PageImpl<>(List.of(testOwner));
        when(ownerRepository.searchOwners(anyString(), any(Pageable.class)))
                .thenReturn(ownerPage);

        // Act
        Page<OwnerDTO> result = ownerService.searchOwners("John", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(ownerRepository).searchOwners(anyString(), any(Pageable.class));
    }

    @Test
    void updateOwner_Success() {
        // Arrange
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));
        when(ownerRepository.save(any(Owner.class))).thenReturn(testOwner);

        // Act
        OwnerDTO result = ownerService.updateOwner(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(ownerRepository).findById(anyLong());
        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    void updateOwner_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> ownerService.updateOwner(999L, updateRequest));

        verify(ownerRepository).findById(anyLong());
        verify(ownerRepository, never()).save(any(Owner.class));
    }

    @Test
    void updateOwner_DuplicateEmail_ThrowsDuplicateResourceException() {
        // Arrange
        updateRequest.setEmail("newemail@example.com");
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));
        when(ownerRepository.existsByEmailAndIsActiveTrue(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> ownerService.updateOwner(1L, updateRequest));

        verify(ownerRepository).findById(anyLong());
        verify(ownerRepository).existsByEmailAndIsActiveTrue(anyString());
        verify(ownerRepository, never()).save(any(Owner.class));
    }

    @Test
    void updateOwner_DuplicateDocumentNumber_ThrowsDuplicateResourceException() {
        // Arrange
        updateRequest.setDocumentNumber("98765432");
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));
        when(ownerRepository.existsByDocumentNumberAndIsActiveTrue(anyString()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> ownerService.updateOwner(1L, updateRequest));

        verify(ownerRepository).findById(anyLong());
        verify(ownerRepository).existsByDocumentNumberAndIsActiveTrue(anyString());
        verify(ownerRepository, never()).save(any(Owner.class));
    }

    @Test
    void deleteOwner_Success() {
        // Arrange
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));
        when(ownerRepository.save(any(Owner.class))).thenReturn(testOwner);

        // Act
        ownerService.deleteOwner(1L);

        // Assert
        verify(ownerRepository).findById(anyLong());
        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    void deleteOwner_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> ownerService.deleteOwner(999L));

        verify(ownerRepository).findById(anyLong());
        verify(ownerRepository, never()).save(any(Owner.class));
    }

    @Test
    void countActiveOwners_Success() {
        // Arrange
        when(ownerRepository.countByIsActiveTrue()).thenReturn(15L);

        // Act
        long result = ownerService.countActiveOwners();

        // Assert
        assertEquals(15L, result);

        verify(ownerRepository).countByIsActiveTrue();
    }

    @Test
    void getOwnersByCity_Success() {
        // Arrange
        when(ownerRepository.findByCityAndIsActiveTrueOrderByLastNameAsc(anyString()))
                .thenReturn(List.of(testOwner));

        // Act
        List<OwnerDTO> result = ownerService.getOwnersByCity("Springfield");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(ownerRepository).findByCityAndIsActiveTrueOrderByLastNameAsc(anyString());
    }

    @Test
    void getOwnersByCity_Empty_ReturnsEmptyList() {
        // Arrange
        when(ownerRepository.findByCityAndIsActiveTrueOrderByLastNameAsc(anyString()))
                .thenReturn(List.of());

        // Act
        List<OwnerDTO> result = ownerService.getOwnersByCity("NonexistentCity");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(ownerRepository).findByCityAndIsActiveTrueOrderByLastNameAsc(anyString());
    }
}