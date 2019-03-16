package com.github.kerraway.disruptor.advanced.chain;

import com.lmax.disruptor.EventHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * @author kerraway
 * @date 2019/3/11
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class TradeHandler2 implements EventHandler<Trade> {

  @Override
  public void onEvent(Trade event, long sequence, boolean endOfBatch) throws Exception {
    logger.info("Trade handler 2: set uuid");
    Thread.sleep(1500);
    event.setUuid(UUID.randomUUID().toString());
  }

}
