package com.selimhorri.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.User;
import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.repository.CredentialRepository;
import com.selimhorri.app.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CredentialRepository credentialRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;
    private Credential credential;
    private CredentialDto credentialDto;

    @BeforeEach
    void setUp() {
        this.credential = Credential.builder()
                .credentialId(1)
                .username("john.doe")
                .password("password123")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        this.credentialDto = CredentialDto.builder()
                .credentialId(1)
                .username("john.doe")
                .password("password123")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        this.user = User.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .credential(this.credential)
                .build();

        this.userDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .credentialDto(this.credentialDto)
                .build();
    }

    @Test
    void findAll_shouldReturnUserList() {
        // given
        when(this.userRepository.findAll()).thenReturn(Collections.singletonList(this.user));

        // when
        List<UserDto> users = this.userService.findAll();

        // then
        assertThat(users).isNotNull();
        assertThat(users.size()).isEqualTo(1);
        assertThat(users.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void findById_shouldReturnUser() {
        // given
        when(this.userRepository.findById(1)).thenReturn(Optional.of(this.user));

        // when
        UserDto foundUser = this.userService.findById(1);

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUserId()).isEqualTo(1);
    }

    @Test
    void save_shouldCreateUser() {
        // given
        when(this.userRepository.save(any(User.class))).thenReturn(this.user);

        // when
        UserDto savedUser = this.userService.save(this.userDto);

        // then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getFirstName()).isEqualTo("John");
    }

    @Test
    void update_shouldUpdateUser() {
        // given
        User updatedUser = User.builder()
                .userId(1)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .credential(this.credential)
                .build();

        when(this.userRepository.findById(1)).thenReturn(Optional.of(this.user));
        when(this.userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto updatedInfo = UserDto.builder()
                .userId(1)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .credentialDto(this.credentialDto)
                .build();

        // when
        UserDto updatedUserDto = this.userService.update(updatedInfo);

        // then
        assertThat(updatedUserDto).isNotNull();
        assertThat(updatedUserDto.getFirstName()).isEqualTo("Jane");
    }

    @Test
    void deleteById_shouldDeleteUser() {
        // given
        when(this.userRepository.findById(1)).thenReturn(Optional.of(this.user));

        // when
        this.userService.deleteById(1);

        // then - verify that save and deleteByCredentialId were called
        org.mockito.Mockito.verify(this.userRepository).findById(1);
        org.mockito.Mockito.verify(this.userRepository).save(any(User.class));
        org.mockito.Mockito.verify(this.credentialRepository).deleteByCredentialId(1);
    }
}