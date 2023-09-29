package bridge.common;

import java.util.concurrent.TimeUnit;

public class RateLimiter {
  private final long capacity;
  private final long refillRate;
  private long tokens;
  private long lastRefillTimestamp;

  public RateLimiter(long capacity, long refillRate) {
    this.capacity = capacity;
    this.refillRate = refillRate;
    this.tokens = capacity;
    this.lastRefillTimestamp = System.currentTimeMillis();
  }

  public synchronized boolean allowRequest() {
    refillTokens();
    if (tokens > 0) {
      tokens--;
      return true;
    } else {
      return false; // No tokens available, rate limit exceeded
    }
  }

  private void refillTokens() {
    long now = System.currentTimeMillis();
    long timePassed = now - lastRefillTimestamp;
    if (timePassed > 0) {
      long tokensToAdd = timePassed * refillRate / TimeUnit.SECONDS.toMillis(1);
      tokens = Math.min(capacity, tokens + tokensToAdd);
      lastRefillTimestamp = now;
    }
  }
}

