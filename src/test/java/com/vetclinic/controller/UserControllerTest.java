package com.vetclinic.controller;

import com.vetclinic.dto.user.CreateUserRequest;
import com.vetclinic.dto.user.UpdateUserRequest;
import com.vetclinic.dto.user.UserDTO;
import com.vetclinic.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserDTO userDTO;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userDTO = UserDTO.builder()
                .id(userId.toString())
                .username("testuser")
                .email("test@test.com")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .build();

        createRequest = CreateUserRequest.builder()
                .username("newuser")
                .email("newuser@test.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .build();

        updateRequest = UpdateUserRequest.builder()
                .email("updated@test.com")
                .firstName("Updated")
                .build();
    }

    @Test
    void testGetAllUsers_Success() {
        Page<UserDTO> page = new PageImpl<>(Collections.singletonList(userDTO));
        when(userService.getAllUsers(any())).thenReturn(page);

        userController.getAllUsers(PageRequest.of(0, 10));

        verify(userService, times(1)).getAllUsers(any());
    }

    @Test
    void testCreateUser_Success() {
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userDTO);

        userController.createUser(createRequest);

        verify(userService, times(1)).createUser(any(CreateUserRequest.class));
    }

    @Test
    void testGetUserById_Success() {
        when(userService.getUserById(userId)).thenReturn(userDTO);

        userController.getUserById(userId);

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void testGetUserByUsername_Success() {
        when(userService.getUserByUsername("testuser")).thenReturn(userDTO);

        userController.getUserByUsername("testuser");

        verify(userService, times(1)).getUserByUsername("testuser");
    }

    @Test
    void testUpdateUser_Success() {
        when(userService.updateUser(any(UUID.class), any())).thenReturn(userDTO);

        userController.updateUser(userId, updateRequest);

        verify(userService, times(1)).updateUser(any(UUID.class), any(UpdateUserRequest.class));
    }

    @Test
    void testDeleteUser_Success() {
        doNothing().when(userService).deleteUser(userId);

        userController.deleteUser(userId);

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void testUnlockUser_Success() {
        doNothing().when(userService).unlockUser(userId);

        userController.unlockUser(userId);

        verify(userService, times(1)).unlockUser(userId);
    }
}