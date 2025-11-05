package com.selimhorri.app.business.user.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.selimhorri.app.business.auth.enums.ResourceType;
import com.selimhorri.app.business.auth.util.AuthUtil;
import com.selimhorri.app.business.user.model.AddressDto;
import com.selimhorri.app.business.user.model.response.AddressUserServiceCollectionDtoResponse;
import com.selimhorri.app.business.user.service.AddressClientService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {
	@Autowired
	private AuthUtil authUtil;
	private final AddressClientService addressClientService;

	@GetMapping
	public ResponseEntity<AddressUserServiceCollectionDtoResponse> findAll() {
		return ResponseEntity.ok(this.addressClientService.findAll().getBody());
	}

	@GetMapping("/{addressId}")
	public ResponseEntity<AddressDto> findById(@PathVariable("addressId") final String addressId,
			HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(addressId, ResourceType.ADDRESSES);
		authUtil.canActivate(request, userId, userDetails);
		return ResponseEntity.ok(this.addressClientService.findById(addressId).getBody());
	}

	@PostMapping
	public ResponseEntity<AddressDto> save(@RequestBody final AddressDto addressDto, HttpServletRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {
		authUtil.canActivate(request, addressDto.getUserDto().getUserId().toString(), userDetails);
		return ResponseEntity.ok(this.addressClientService.save(addressDto).getBody());
	}

	@PutMapping("/{addressId}")
	public ResponseEntity<AddressDto> update(@PathVariable("addressId") final String addressId,
			@RequestBody final AddressDto addressDto, HttpServletRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(addressId, ResourceType.ADDRESSES);
		authUtil.canActivate(request, userId, userDetails);
		return ResponseEntity.ok(this.addressClientService.update(addressId, addressDto).getBody());
	}

	@DeleteMapping("/{addressId}")
	public ResponseEntity<Boolean> deleteById(@PathVariable("addressId") final String addressId, HttpServletRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(addressId, ResourceType.ADDRESSES);
		authUtil.canActivate(request, userId, userDetails);
		return ResponseEntity.ok(this.addressClientService.deleteById(addressId).getBody());
	}

}
