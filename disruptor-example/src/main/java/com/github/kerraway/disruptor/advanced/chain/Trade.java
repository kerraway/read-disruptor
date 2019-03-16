package com.github.kerraway.disruptor.advanced.chain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kerraway
 * @date 2019/3/11
 */
@Data
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Trade {

  private Integer id;
  private String uuid;
  private String name;
  private BigDecimal price;
  private AtomicInteger count;

}
