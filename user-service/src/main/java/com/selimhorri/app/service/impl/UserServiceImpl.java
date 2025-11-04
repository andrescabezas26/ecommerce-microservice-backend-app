package com.selimhorri.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.helper.UserMappingHelper;
import com.selimhorri.app.repository.CredentialRepository;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final CredentialRepository credentialRepository;

	@Override
	public List<UserDto> findAll() {
		log.info("*** UserDto List, service; fetch all users with credentials *");
		return this.userRepository.findAll()
				.stream()
				.filter(user -> user.getCredential() != null) // Asumiendo que hay un getCredentials()
				.map(UserMappingHelper::map)
				.distinct()
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public UserDto findById(final Integer userId) {
		log.info("*** UserDto, service; fetch user by id with credentials *");
		return this.userRepository.findById(userId)
				.filter(user -> user.getCredential() != null) // Filtramos que tenga credenciales
				.map(UserMappingHelper::map)
				.orElseThrow(
						() -> new UserObjectNotFoundException(
								String.format("User with id: %d not found or has no credentials", userId)));
	}

	@Override
	public UserDto findByUsername(final String username) {
		log.info("*** UserDto, service; fetch user with username *");
		return UserMappingHelper.map(this.userRepository.findByCredentialUsername(username)
				.orElseThrow(() -> new UserObjectNotFoundException(
						String.format("User with username: %s not found", username))));
	}

	@Override
	public UserDto save(final UserDto userDto) {
		log.info("*** UserDto, service; save user *");
		userDto.setUserId(null); // para evitar sobrescribir
		return UserMappingHelper.map(this.userRepository.save(UserMappingHelper.mapOnlyUser(userDto)));
	}

	@Override
	public UserDto update(final UserDto userDto) {
		log.info("*** UserDto, service; update user ***");

		// Buscar el usuario y verificar que tenga credenciales
		User existingUser = this.userRepository.findById(userDto.getUserId())
				.filter(user -> user.getCredential() != null) // Solo si tiene credenciales
				.orElseThrow(() -> new EntityNotFoundException(
						"User not found or has no credentials (cannot update)"));

		// Actualizar campos permitidos
		existingUser.setFirstName(userDto.getFirstName());
		existingUser.setLastName(userDto.getLastName());
		existingUser.setImageUrl(userDto.getImageUrl());
		existingUser.setEmail(userDto.getEmail());
		existingUser.setPhone(userDto.getPhone());

		return UserMappingHelper.map(this.userRepository.save(existingUser));
	}

	@Override
	public UserDto update(final Integer userId, final UserDto userDto) {
		log.info("*** UserDto, service; update user with userId ***");

		// Verificar que el usuario existe y tiene credenciales
		User existingUser = this.userRepository.findById(userId)
				.filter(user -> user.getCredential() != null) // Solo si tiene credenciales
				.orElseThrow(() -> new EntityNotFoundException(
						"User not found with id: " + userId + " or has no credentials (cannot update)"));

		// Actualizar campos permitidos
		existingUser.setFirstName(userDto.getFirstName());
		existingUser.setLastName(userDto.getLastName());
		existingUser.setImageUrl(userDto.getImageUrl());
		existingUser.setEmail(userDto.getEmail());
		existingUser.setPhone(userDto.getPhone());

		return UserMappingHelper.map(this.userRepository.save(existingUser));
	}

	@Override
	@Transactional // Asegura que sea una transacción atómica
	public void deleteById(final Integer userId) {
		log.info("*** Void, service; delete credentials from user by id ***");

		// 1. Buscar el usuario y verificar que existe y tiene credenciales
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

		if (user.getCredential() == null) {
			throw new UserObjectNotFoundException("User with id: " + userId + " has no credentials to delete");
		}

		// 2. Obtener el ID de las credenciales para borrarlas
		Integer credentialsId = user.getCredential().getCredentialId();

		// 3. Desvincular las credenciales del usuario (para evitar inconsistencias)
		user.setCredential(null);
		userRepository.save(user); // Guardar el cambio

		// 4. Borrar las credenciales de la base de datos
		credentialRepository.deleteByCredentialId(credentialsId);
	}

}
