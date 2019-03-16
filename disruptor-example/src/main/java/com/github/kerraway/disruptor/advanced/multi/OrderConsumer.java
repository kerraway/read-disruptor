package com.github.kerraway.disruptor.advanced.multi;

import com.lmax.disruptor.WorkHandler;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kerraway
 * @date 2019/3/16
 */
@Data
@Slf4j
public class OrderConsumer implements WorkHandler<Order> {

  private static final Random RANDOM = new Random();

  private final String consumerId;
  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private final AtomicInteger count;

  OrderConsumer(String consumerId) {
    this.consumerId = consumerId;
    this.count = new AtomicInteger(0);
  }

  @Override
  public void onEvent(Order event) throws Exception {
    Thread.sleep(RANDOM.nextInt(5));
    logger.debug("消费者 ID：{}，订单 ID：{}", this.consumerId, event.getId());
    count.incrementAndGet();
  }

  public int getCount() {
    return count.get();
  }
}
