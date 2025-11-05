package com.selimhorri.app.business.payment.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

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
import com.selimhorri.app.business.payment.model.PaymentDto;
import com.selimhorri.app.business.payment.model.response.PaymentPaymentServiceDtoCollectionResponse;
import com.selimhorri.app.business.payment.service.PaymentClientService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	@Autowired
	private AuthUtil authUtil;
	private final PaymentClientService paymentClientService;

	@GetMapping
	public ResponseEntity<PaymentPaymentServiceDtoCollectionResponse> findAll() {
		return ResponseEntity.ok(this.paymentClientService.findAll().getBody());
	}

	@GetMapping("/{paymentId}")
	public ResponseEntity<PaymentDto> findById(@PathVariable("paymentId") final String paymentId,
			HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(paymentId, ResourceType.PAYMENTS);
		authUtil.canActivate(request, userId, userDetails);
		return ResponseEntity.ok(this.paymentClientService.findById(paymentId).getBody());
	}

	@PostMapping
	public ResponseEntity<PaymentDto> save(@RequestBody final PaymentDto paymentDto, HttpServletRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(paymentDto.getOrderDto().getOrderId().toString(), ResourceType.ORDERS);
		authUtil.canActivate(request, userId, userDetails);
		return ResponseEntity.ok(this.paymentClientService.save(paymentDto).getBody());
	}

	@PutMapping("/{paymentId}")
	public ResponseEntity<PaymentDto> updateStatus(
			@PathVariable("paymentId") @NotBlank(message = "Input must not be blank") @Valid final String paymentId) {
		return ResponseEntity.ok(this.paymentClientService.updateStatus(paymentId).getBody());
	}

	@DeleteMapping("/{paymentId}")
	public ResponseEntity<Boolean> deleteById(@PathVariable("paymentId") final String paymentId, HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
		String userId = authUtil.getOwner(paymentId, ResourceType.PAYMENTS);
		authUtil.canActivate(request, userId, userDetails);
		return ResponseEntity.ok(this.paymentClientService.deleteById(paymentId).getBody());
	}

}
