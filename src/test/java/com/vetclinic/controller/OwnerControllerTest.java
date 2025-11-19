package com.vetclinic.controller;

import com.vetclinic.dto.owner.CreateOwnerRequest;
import com.vetclinic.dto.owner.OwnerDTO;
import com.vetclinic.dto.owner.UpdateOwnerRequest;
import com.vetclinic.service.OwnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    @Mock
    private OwnerService ownerService;

    @InjectMocks
    private OwnerController ownerController;

    private OwnerDTO ownerDTO;
    private CreateOwnerRequest createRequest;
    private UpdateOwnerRequest updateRequest;

    @BeforeEach
    void setUp() {
        ownerDTO = new OwnerDTO();
        ownerDTO.setId(1L);
        ownerDTO.setFirstName("John");
        ownerDTO.setLastName("Doe");
        ownerDTO.setEmail("john@test.com");
        ownerDTO.setPhone("1234567890");
        ownerDTO.setIsActive(true);

        createRequest = new CreateOwnerRequest();
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setEmail("john@test.com");
        createRequest.setPhone("1234567890");

        updateRequest = new UpdateOwnerRequest();
        updateRequest.setFirstName("John Updated");
        updateRequest.setPhone("9876543210");
    }

    @Test
    void testCreateOwner_Success() {
        when(ownerService.createOwner(any(CreateOwnerRequest.class))).thenReturn(ownerDTO);

        ownerController.createOwner(createRequest);

        verify(ownerService, times(1)).createOwner(any(CreateOwnerRequest.class));
    }

    @Test
    void testGetAllOwners_Success() {
        List<OwnerDTO> owners = Collections.singletonList(ownerDTO);
        when(ownerService.getAllOwners()).thenReturn(owners);

        ownerController.getAllOwners();

        verify(ownerService, times(1)).getAllOwners();
    }

    @Test
    void testGetOwnersPage_Success() {
        Page<OwnerDTO> page = new PageImpl<>(Collections.singletonList(ownerDTO));
        when(ownerService.getOwnersPage(any())).thenReturn(page);

        ownerController.getOwnersPage(0, 10, "id", "asc");

        verify(ownerService, times(1)).getOwnersPage(any());
    }

    @Test
    void testGetOwnerById_Success() {
        when(ownerService.getOwnerById(1L)).thenReturn(ownerDTO);

        ownerController.getOwnerById(1L);

        verify(ownerService, times(1)).getOwnerById(1L);
    }

    @Test
    void testSearchOwners_Success() {
        Page<OwnerDTO> page = new PageImpl<>(Collections.singletonList(ownerDTO));
        when(ownerService.searchOwners(any(), any())).thenReturn(page);

        ownerController.searchOwners("John", 0, 10);

        verify(ownerService, times(1)).searchOwners(any(), any());
    }

    @Test
    void testUpdateOwner_Success() {
        when(ownerService.updateOwner(anyLong(), any())).thenReturn(ownerDTO);

        ownerController.updateOwner(1L, updateRequest);

        verify(ownerService, times(1)).updateOwner(eq(1L), any(UpdateOwnerRequest.class));
    }

    @Test
    void testDeleteOwner_Success() {
        doNothing().when(ownerService).deleteOwner(1L);

        ownerController.deleteOwner(1L);

        verify(ownerService, times(1)).deleteOwner(1L);
    }

    @Test
    void testCountOwners_Success() {
        when(ownerService.countActiveOwners()).thenReturn(25L);

        ownerController.countOwners();

        verify(ownerService, times(1)).countActiveOwners();
    }

    @Test
    void testGetOwnersByCity_Success() {
        List<OwnerDTO> owners = Collections.singletonList(ownerDTO);
        when(ownerService.getOwnersByCity("Springfield")).thenReturn(owners);

        ownerController.getOwnersByCity("Springfield");

        verify(ownerService, times(1)).getOwnersByCity("Springfield");
    }
}