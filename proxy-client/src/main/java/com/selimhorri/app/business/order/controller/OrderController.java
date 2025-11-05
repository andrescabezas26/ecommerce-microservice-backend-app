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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.selimhorri.app.business.auth.enums.ResourceType;
import com.selimhorri.app.business.auth.util.AuthUtil;
import com.selimhorri.app.business.order.model.OrderDto;
import com.selimhorri.app.business.order.model.response.OrderOrderServiceDtoCollectionResponse;
import com.selimhorri.app.business.order.service.OrderClientService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	@Autowired
	private AuthUtil authUtil;
	private final OrderClientService orderClientService;

	@GetMapping
	public ResponseEntity<OrderOrderServiceDtoCollectionResponse> findAll() {
		return ResponseEntity.ok(this.orderClientService.findAll().getBody());
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderDto> findById(
			@PathVariable("orderId") @NotBlank(message = "Input must not be blank!") @Valid final String orderId,
			HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(orderId, ResourceType.ORDERS);
		authUtil.canActivate(request, userId, userDetails);
		return ResponseEntity.ok(this.orderClientService.findById(orderId).getBody());
	}

	@PostMapping
	public ResponseEntity<OrderDto> save(
			@RequestBody @NotNull(message = "Input must not be NULL!") @Valid final OrderDto orderDto,
			HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(orderDto.getCartDto().getCartId().toString(), ResourceType.CARTS);
		authUtil.canActivate(request, userId, userDetails);
		return ResponseEntity.ok(this.orderClientService.save(orderDto).getBody());
	}

	@PatchMapping("/{orderId}/status")
	public ResponseEntity<OrderDto> update(
			@PathVariable("orderId") @NotBlank(message = "Input must not be blank") @Valid final int orderId) {
				
		return ResponseEntity.ok(this.orderClientService.updateStatus(orderId).getBody());
	}

	@PutMapping("/{orderId}")
	public ResponseEntity<OrderDto> update(
			@PathVariable("orderId") @NotBlank(message = "Input must not be blank!") @Valid final String orderId,
			@RequestBody @NotNull(message = "Input must not be NULL!") @Valid final OrderDto orderDto,
			HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(orderId, ResourceType.ORDERS);
		authUtil.canActivate(request, userId, userDetails);
		return ResponseEntity.ok(this.orderClientService.update(orderId, orderDto).getBody());
	}

	@DeleteMapping("/{orderId}")
	public ResponseEntity<Boolean> deleteById(@PathVariable("orderId") final String orderId, HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(orderId, ResourceType.ORDERS);
		authUtil.canActivate(request, userId, userDetails);
		this.orderClientService.deleteById(orderId).getBody();
		return ResponseEntity.ok(true);
	}

}
