package com.github.kerraway.disruptor.advanced.multi;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author kerraway
 * @date 2019/3/16
 */
@Data
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Order {

  private String id;
  private String name;
  private BigDecimal price;

}
