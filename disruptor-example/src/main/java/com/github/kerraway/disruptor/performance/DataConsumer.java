package com.github.kerraway.disruptor.performance;

import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;

/**
 * @author kerraway
 * @date 2019/3/10
 */
@Slf4j
public class DataConsumer implements EventHandler<Data> {

  private final Instant start;
  private int count;
  private final int total;

  DataConsumer(int total) {
    this.start = Instant.now();
    this.count = 0;
    this.total = total;
  }

  @Override
  public void onEvent(Data event, long sequence, boolean endOfBatch) {
    count++;
    if (count == total) {
      Instant end = Instant.now();
      logger.info("Disruptor, total: {}, cost: {} ms.", total, Duration.between(start, end).toMillis());
    }
  }
}
