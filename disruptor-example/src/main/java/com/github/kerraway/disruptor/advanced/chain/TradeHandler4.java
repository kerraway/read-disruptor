package com.github.kerraway.disruptor.advanced.chain;

import com.lmax.disruptor.EventHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * @author kerraway
 * @date 2019/3/11
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class TradeHandler4 implements EventHandler<Trade> {

  @Override
  public void onEvent(Trade event, long sequence, boolean endOfBatch) throws Exception {
    logger.info("Trade handler 4: set price to 10");
    Thread.sleep(1000);
    event.setPrice(BigDecimal.TEN);
  }

}
