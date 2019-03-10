package com.github.kerraway.disruptor.quickstart;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kerraway
 * @date 2019/3/10
 */
@Data
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class OrderEvent {

  /**
   * 订单 ID
   */
  private long id;

}
