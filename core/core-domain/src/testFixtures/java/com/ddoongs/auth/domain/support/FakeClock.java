package com.ddoongs.auth.domain.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class FakeClock extends Clock {

  private final ZoneId zone;
  private Instant currentInstant;

  public FakeClock(Instant fixedInstant, ZoneId zone) {
    this.currentInstant = fixedInstant;
    this.zone = zone;
  }

  public static FakeClock nowAt(Instant fixedInstant) {
    return new FakeClock(fixedInstant, ZoneId.systemDefault());
  }

  public static FakeClock nowAt(Instant fixedInstant, ZoneId zone) {
    return new FakeClock(fixedInstant, zone);
  }

  @Override
  public ZoneId getZone() {
    return zone;
  }

  @Override
  public Clock withZone(ZoneId zone) {
    return new FakeClock(currentInstant, zone);
  }

  @Override
  public Instant instant() {
    return currentInstant;
  }

  public void setInstant(Instant newInstant) {
    this.currentInstant = newInstant;
  }

  public void plus(Duration duration) {
    this.currentInstant = this.currentInstant.plus(duration);
  }

  public void minus(Duration duration) {
    this.currentInstant = this.currentInstant.minus(duration);
  }

  public void resetToNow() {
    this.currentInstant = Instant.now();
  }
}
