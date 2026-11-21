package com.pe.pivot.yanki.infrastructure.adapter.out.cache;

import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.port.out.YankiWalletRepositoryPort;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

/**
 * Decorator adding Reactive Cache-Aside by phoneNumber over YankiWalletPersistenceAdapter.
 */
@Slf4j
@Primary
@Component
public class CachedYankiWalletRepositoryAdapter implements YankiWalletRepositoryPort {

  // Cache key prefix for storing YankiWallets in Redis by phone number
  private static final String KEY_PHONE_PREFIX = "yanki:wallet:phone:";

  // Delegate repository for actual persistence operations
  private final YankiWalletRepositoryPort delegate;
  private final ReactiveRedisTemplate<String, YankiWallet> walletRedisTemplate;
  private final Duration cacheTtl;

  /**
   * Constructor for CachedYankiWalletRepositoryAdapter.
   *
   * @param delegate - the repository implementation to delegate persistence operations to
   * @param walletRedisTemplate - the ReactiveRedisTemplate for caching YankiWallets
   * @param ttlMinutes - the time-to-live for cached entries in minutes
   */
  public CachedYankiWalletRepositoryAdapter(
          @Qualifier("yankiWalletPersistenceAdapter") YankiWalletRepositoryPort delegate,
          ReactiveRedisTemplate<String, YankiWallet> walletRedisTemplate,
          @Value("${application.yanki.cache-ttl-minutes:30}") long ttlMinutes) {
    this.delegate = delegate;
    this.walletRedisTemplate = walletRedisTemplate;
    this.cacheTtl = Duration.ofMinutes(ttlMinutes);
  }

  /**
   * Saves a YankiWallet to the underlying repository and caches it in Redis.
   *
   * @param wallet - the YankiWallet domain object to be saved
   * @return - Single emitting the saved YankiWallet domain object
   */
  @Override
  public Single<YankiWallet> save(YankiWallet wallet) {
    return delegate.save(wallet).flatMap(this::cacheWallet);
  }

  /**
   * Retrieves all YankiWallet records from the underlying repository.
   *
   * @return - Flowable emitting all YankiWallet domain objects
   */
  @Override
  public Flowable<YankiWallet> findAll() {
    return delegate.findAll();
  }

  /**
   * Finds a YankiWallet by its unique identifier.
   *
   * @param phoneNumber - the unique identifier (phone number) of the YankiWallet
   * @return - Maybe emitting the found YankiWallet or empty if not found
   */
  @Override
  public Maybe<YankiWallet> findByPhoneNumber(String phoneNumber) {
    String key = KEY_PHONE_PREFIX + phoneNumber;
    // Attempt to retrieve the YankiWallet from Redis cache first
    return RxJava3Adapter.monoToMaybe(
                    walletRedisTemplate.opsForValue().get(key)
                            .doOnNext(w -> log.debug(
                                    "Redis cache HIT for Yanki phone: {}", phoneNumber))
                            .onErrorResume(ex -> {
                              log.warn("Redis read failed for {}, falling back to MongoDB: {}",
                                      key, ex.getMessage());
                              return Mono.empty();
                            }))
            .switchIfEmpty(Maybe.defer(() -> delegate.findByPhoneNumber(phoneNumber)
                    .flatMap(wallet -> cacheWallet(wallet).toMaybe())));
  }

  /**
   * Checks if a YankiWallet exists with the specified phone number.
   *
   * @param phoneNumber - the phone number to check for existence
   * @return - a Single emitting true if a wallet exists, false otherwise
   */
  @Override
  public Single<Boolean> existsByPhoneNumber(String phoneNumber) {
    return delegate.existsByPhoneNumber(phoneNumber);
  }

  /**
   * Checks if a YankiWallet exists with the specified document number.
   *
   * @param imei - the IMEI to check for existence
   * @return - a Single emitting true if a wallet exists, false otherwise
   */
  @Override
  public Single<Boolean> existsByImei(String imei) {
    return delegate.existsByImei(imei);
  }

  /**
   * Checks if a YankiWallet exists with the specified document number.
   *
   * @param documentNumber - the document number to check for existence
   * @return - a Single emitting true if a wallet exists, false otherwise
   */
  @Override
  public Single<Boolean> existsByDocumentNumber(String documentNumber) {
    return delegate.existsByDocumentNumber(documentNumber);
  }

  /**
   * Caches the given YankiWallet in Redis with a specified TTL.
   *
   * @param wallet - the YankiWallet domain object to be cached
   * @return - Single emitting the cached YankiWallet domain object
   */
  private Single<YankiWallet> cacheWallet(YankiWallet wallet) {
    String key = KEY_PHONE_PREFIX + wallet.getPhoneNumber();
    return RxJava3Adapter.monoToSingle(
            walletRedisTemplate.opsForValue().set(key, wallet, cacheTtl)
                    .onErrorResume(ex -> {
                      log.warn("Redis write failed for {}: {}", key, ex.getMessage());
                      return Mono.just(false);
                    })
                    .thenReturn(wallet));
  }
}