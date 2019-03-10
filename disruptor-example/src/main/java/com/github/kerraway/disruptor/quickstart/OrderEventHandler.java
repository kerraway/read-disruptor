package com.github.kerraway.disruptor.quickstart;

import com.lmax.disruptor.EventHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author kerraway
 * @date 2019/3/10
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class OrderEventHandler implements EventHandler<OrderEvent> {

  @Override
  public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) throws Exception {
    TimeUnit.MILLISECONDS.sleep(1000);
    logger.info("消费者拿到订单 ID {}", event.getId());
  }

}
