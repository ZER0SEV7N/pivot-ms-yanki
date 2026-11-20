package com.pe.pivot.yanki.infrastructure.adapter.in.web;

import com.pe.pivot.yanki.domain.port.in.RetrieveYankiPaymentUseCase;
import com.pe.pivot.yanki.domain.port.in.SendYankiPaymentUseCase;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiPaymentRequest;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiPaymentResponse;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.mapper.YankiWebMapper;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Yanki phone-to-phone payments (/api/v1/yanki/payments).
 */
@RestController
@RequestMapping("/api/v1/yanki/payments")
@RequiredArgsConstructor
public class YankiPaymentController {

  private final SendYankiPaymentUseCase sendPaymentUseCase;
  private final RetrieveYankiPaymentUseCase retrievePaymentUseCase;
  private final YankiWebMapper mapper;

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Single<YankiPaymentResponse> sendPayment(@Valid @RequestBody YankiPaymentRequest request) {
    return sendPaymentUseCase.sendPayment(mapper.toDomain(request))
            .map(mapper::toPaymentResponse);
  }

  @GetMapping("/{transactionId}")
  public Single<YankiPaymentResponse> getPaymentByTxId(@PathVariable String transactionId) {
    return retrievePaymentUseCase.findByTransactionId(transactionId)
            .map(mapper::toPaymentResponse);
  }

  @GetMapping("/phone/{phoneNumber}")
  public Flowable<YankiPaymentResponse> getPaymentsByPhone(@PathVariable String phoneNumber) {
    return retrievePaymentUseCase.findByPhoneNumber(phoneNumber)
            .map(mapper::toPaymentResponse);
  }
}