package com.github.kerraway.disruptor.advanced;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kerraway
 * @date 2019/3/11
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class TradeHandler1 implements EventHandler<Trade>, WorkHandler<Trade> {

  /**
   * @param event
   * @param sequence
   * @param endOfBatch
   * @throws Exception
   * @see EventHandler#onEvent(Object, long, boolean)
   */
  @Override
  public void onEvent(Trade event, long sequence, boolean endOfBatch) throws Exception {
    this.onEvent(event);
  }

  /**
   * @param event
   * @throws Exception
   * @see WorkHandler#onEvent(Object)
   */
  @Override
  public void onEvent(Trade event) throws Exception {
    logger.info("Trade handler 1: set name");
    Thread.sleep(1000);
    event.setName("H1");
  }
}
