package com.github.kerraway.disruptor.quickstart;

import com.lmax.disruptor.EventFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author kerraway
 * @date 2019/3/10
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class OrderEventFactory implements EventFactory<OrderEvent> {

  /**
   * 该方法就是为了返回空的数据对象
   *
   * @return 空的 {@link OrderEvent} 对象
   */
  @Override
  public OrderEvent newInstance() {
    return new OrderEvent();
  }

}
