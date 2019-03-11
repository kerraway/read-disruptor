package com.github.kerraway.disruptor.advanced;

import com.lmax.disruptor.dsl.Disruptor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.concurrent.CountDownLatch;

/**
 * @author kerraway
 * @date 2019/3/11
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class TradePublisher implements Runnable {

  private final Disruptor<Trade> disruptor;
  private final CountDownLatch countDownLatch;
  private final int total;

  @Override
  public void run() {
    TradeTranslator translator = new TradeTranslator();
    for (int i = 0; i < total; i++) {
      //发布任务
      disruptor.publishEvent(translator);
    }
    countDownLatch.countDown();
  }

}
