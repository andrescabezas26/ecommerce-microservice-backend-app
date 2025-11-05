package com.selimhorri.app.business.order.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.selimhorri.app.business.auth.enums.ResourceType;
import com.selimhorri.app.business.auth.util.AuthUtil;
import com.selimhorri.app.business.order.model.CartDto;
import com.selimhorri.app.business.order.model.response.CartOrderServiceDtoCollectionResponse;
import com.selimhorri.app.business.order.service.CartClientService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

	@Autowired
	private AuthUtil authUtil;
	private final CartClientService cartClientService;

	@GetMapping
	public ResponseEntity<CartOrderServiceDtoCollectionResponse> findAll() {
		return ResponseEntity.ok(this.cartClientService.findAll().getBody());
	}

	@GetMapping("/{cartId}")
	public ResponseEntity<CartDto> findById(
			@PathVariable("cartId") @NotBlank(message = "Input must not be blank!") @Valid final String cartId,
			HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(cartId, ResourceType.CARTS);
		authUtil.canActivate(request, userId, userDetails);
		return ResponseEntity.ok(this.cartClientService.findById(cartId).getBody());
	}

	@PostMapping
	public ResponseEntity<CartDto> save(
			@RequestBody @NotNull(message = "Input must not be NULL!") @Valid final CartDto cartDto,
			HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
		authUtil.canActivate(request, cartDto.getUserId().toString(), userDetails);
		return ResponseEntity.ok(this.cartClientService.save(cartDto).getBody());
	}

	@DeleteMapping("/{cartId}")
	public ResponseEntity<Boolean> deleteById(@PathVariable("cartId") final String cartId, HttpServletRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(cartId, ResourceType.CARTS);
		authUtil.canActivate(request, userId, userDetails);
		this.cartClientService.deleteById(cartId).getBody();
		return ResponseEntity.ok(true);
	}

}
