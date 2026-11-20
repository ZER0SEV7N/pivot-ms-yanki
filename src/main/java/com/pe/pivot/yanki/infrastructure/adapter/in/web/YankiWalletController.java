package com.pe.pivot.yanki.infrastructure.adapter.in.web;

import com.pe.pivot.yanki.domain.port.in.CreateYankiWalletUseCase;
import com.pe.pivot.yanki.domain.port.in.DeleteYankiWalletUseCase;
import com.pe.pivot.yanki.domain.port.in.RetrieveYankiWalletUseCase;
import com.pe.pivot.yanki.domain.port.in.UpdateYankiWalletUseCase;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.LinkDebitCardRequest;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.TopUpRequest;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiWalletRequest;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiWalletResponse;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiWalletUpdateRequest;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.mapper.YankiWebMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Yanki mobile wallet management (/api/v1/yanki/wallets).
 */
@RestController
@RequestMapping("/api/v1/yanki/wallets")
@RequiredArgsConstructor
public class YankiWalletController {

  private final CreateYankiWalletUseCase createWalletUseCase;
  private final RetrieveYankiWalletUseCase retrieveWalletUseCase;
  private final UpdateYankiWalletUseCase updateWalletUseCase;
  private final DeleteYankiWalletUseCase deleteWalletUseCase;
  private final YankiWebMapper mapper;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Single<YankiWalletResponse> createWallet(@Valid @RequestBody YankiWalletRequest request) {
    return createWalletUseCase.create(mapper.toDomain(request))
            .map(mapper::toWalletResponse);
  }

  @GetMapping
  public Flowable<YankiWalletResponse> getAllWallets() {
    return retrieveWalletUseCase.findAll().map(mapper::toWalletResponse);
  }

  @GetMapping("/{phoneNumber}")
  public Single<YankiWalletResponse> getWalletByPhone(@PathVariable String phoneNumber) {
    return retrieveWalletUseCase.findByPhoneNumber(phoneNumber)
            .map(mapper::toWalletResponse);
  }

  @PutMapping("/{phoneNumber}")
  public Single<YankiWalletResponse> updateWallet(
          @PathVariable String phoneNumber, @Valid @RequestBody YankiWalletUpdateRequest request) {
    return updateWalletUseCase.update(phoneNumber, mapper.toDomain(request))
            .map(mapper::toWalletResponse);
  }

  @PatchMapping("/{phoneNumber}/link-card")
  public Single<YankiWalletResponse> linkDebitCard(
          @PathVariable String phoneNumber, @Valid @RequestBody LinkDebitCardRequest request) {
    return updateWalletUseCase.linkDebitCard(phoneNumber, request.debitCardId())
            .map(mapper::toWalletResponse);
  }

  @PatchMapping("/{phoneNumber}/top-up")
  public Single<YankiWalletResponse> topUpWallet(
          @PathVariable String phoneNumber, @Valid @RequestBody TopUpRequest request) {
    return updateWalletUseCase.topUp(phoneNumber, request.amount())
            .map(mapper::toWalletResponse);
  }

  @DeleteMapping("/{phoneNumber}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Completable deleteWallet(@PathVariable String phoneNumber) {
    return deleteWalletUseCase.delete(phoneNumber);
  }
}