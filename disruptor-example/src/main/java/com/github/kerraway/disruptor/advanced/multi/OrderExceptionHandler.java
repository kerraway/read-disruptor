package com.github.kerraway.disruptor.advanced.multi;

import com.lmax.disruptor.ExceptionHandler;

/**
 * @author kerraway
 * @date 2019/3/16
 */
public class OrderExceptionHandler implements ExceptionHandler<Order> {

  @Override
  public void handleEventException(Throwable ex, long sequence, Order event) {
  }

  @Override
  public void handleOnStartException(Throwable ex) {
  }

  @Override
  public void handleOnShutdownException(Throwable ex) {
  }

}
