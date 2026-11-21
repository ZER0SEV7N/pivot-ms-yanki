package com.pe.pivot.yanki.infrastructure.adapter.out.cache;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.port.out.YankiWalletRepositoryPort;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;

/**
 * Unit test suite for CachedYankiWalletRepositoryAdapter.
 */
class CachedYankiWalletRepositoryAdapterTest {

  private static final String KEY = "yanki:wallet:phone:999111222";
  private static final long TTL_MINUTES = 30L;

  private YankiWalletRepositoryPort delegate;
  private ReactiveRedisTemplate<String, YankiWallet> redisTemplate;
  private ReactiveValueOperations<String, YankiWallet> valueOperations;
  private CachedYankiWalletRepositoryAdapter adapter;
  private YankiWallet wallet;

  @BeforeEach
  void setUp() {
    delegate = mock(YankiWalletRepositoryPort.class);
    redisTemplate = mock(ReactiveRedisTemplate.class);
    valueOperations = mock(ReactiveValueOperations.class);
    adapter = new CachedYankiWalletRepositoryAdapter(
            delegate, redisTemplate, TTL_MINUTES);
    wallet = YankiWallet.builder()
            .phoneNumber("999111222")
            .build();
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
  }

  @Test
  @DisplayName("Should be save the wallet through the delegate and cache it (happy path)")
  void shouldBeSaveAndCacheWallet() {
    // Arrange
    when(delegate.save(wallet)).thenReturn(Single.just(wallet));
    when(valueOperations.set(KEY, wallet, Duration.ofMinutes(TTL_MINUTES)))
            .thenReturn(Mono.just(true));

    // Act
    TestObserver<YankiWallet> observer = adapter.save(wallet).test();

    // Assert
    observer.assertComplete();
    observer.assertNoErrors();
    observer.assertValue(wallet);
    verify(delegate).save(wallet);
    verify(valueOperations).set(KEY, wallet, Duration.ofMinutes(TTL_MINUTES));
  }

  @Test
  @DisplayName("Should be delegate find all wallets without touching the cache")
  void shouldBeDelegateFindAll() {
    // Arrange
    YankiWallet second = YankiWallet.builder().phoneNumber("999333444").build();
    when(delegate.findAll()).thenReturn(Flowable.just(wallet, second));

    // Act
    TestSubscriber<YankiWallet> subscriber = adapter.findAll().test();

    // Assert
    subscriber.assertComplete();
    subscriber.assertValues(wallet, second);
    verify(redisTemplate, never()).opsForValue();
  }

  @Test
  @DisplayName("Should be return a cached wallet on a cache hit")
  void shouldBeReturnCachedWallet() {
    // Arrange
    when(valueOperations.get(KEY)).thenReturn(Mono.just(wallet));

    // Act
    TestObserver<YankiWallet> observer = adapter.findByPhoneNumber("999111222").test();

    // Assert
    observer.assertComplete();
    observer.assertValue(wallet);
    verify(delegate, never()).findByPhoneNumber(any());
  }

  @Test
  @DisplayName("Should be load from the delegate and cache on a cache miss")
  void shouldBeLoadFromDelegateOnCacheMiss() {
    // Arrange
    when(valueOperations.get(KEY)).thenReturn(Mono.empty());
    when(delegate.findByPhoneNumber("999111222")).thenReturn(Maybe.just(wallet));
    when(valueOperations.set(KEY, wallet, Duration.ofMinutes(TTL_MINUTES)))
            .thenReturn(Mono.just(true));

    // Act
    TestObserver<YankiWallet> observer = adapter.findByPhoneNumber("999111222").test();

    // Assert
    observer.assertComplete();
    observer.assertValue(wallet);
    verify(delegate).findByPhoneNumber("999111222");
    verify(valueOperations).set(KEY, wallet, Duration.ofMinutes(TTL_MINUTES));
  }

  @Test
  @DisplayName("Should be fall back to MongoDB when the Redis read fails")
  void shouldBeFallBackWhenRedisReadFails() {
    // Arrange
    when(valueOperations.get(KEY))
            .thenReturn(Mono.error(new RuntimeException("redis down")));
    when(delegate.findByPhoneNumber("999111222")).thenReturn(Maybe.just(wallet));
    when(valueOperations.set(KEY, wallet, Duration.ofMinutes(TTL_MINUTES)))
            .thenReturn(Mono.just(true));

    // Act
    TestObserver<YankiWallet> observer = adapter.findByPhoneNumber("999111222").test();

    // Assert
    observer.assertComplete();
    observer.assertValue(wallet);
    verify(delegate).findByPhoneNumber("999111222");
  }

  @Test
  @DisplayName("Should be still return the wallet when the Redis write fails")
  void shouldBeReturnWalletWhenRedisWriteFails() {
    // Arrange
    when(delegate.save(wallet)).thenReturn(Single.just(wallet));
    when(valueOperations.set(eq(KEY), eq(wallet), any(Duration.class)))
            .thenReturn(Mono.error(new RuntimeException("redis down")));

    // Act
    TestObserver<YankiWallet> observer = adapter.save(wallet).test();

    // Assert
    observer.assertComplete();
    observer.assertNoErrors();
    observer.assertValue(wallet);
  }

  @Test
  @DisplayName("Should be delegate the existence checks without using the cache")
  void shouldBeDelegateExistenceChecks() {
    // Arrange
    when(delegate.existsByPhoneNumber("999111222")).thenReturn(Single.just(true));
    when(delegate.existsByImei("IMEI-1")).thenReturn(Single.just(false));
    when(delegate.existsByDocumentNumber("12345678")).thenReturn(Single.just(true));

    // Act
    TestObserver<Boolean> phone = adapter.existsByPhoneNumber("999111222").test();
    TestObserver<Boolean> imei = adapter.existsByImei("IMEI-1").test();
    TestObserver<Boolean> document = adapter.existsByDocumentNumber("12345678").test();

    // Assert
    phone.assertValue(true);
    imei.assertValue(false);
    document.assertValue(true);
  }
}
