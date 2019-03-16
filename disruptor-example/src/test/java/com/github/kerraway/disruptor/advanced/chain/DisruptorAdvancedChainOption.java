package com.github.kerraway.disruptor.advanced.chain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author kerraway
 * @date 2019/3/11
 */
@Getter
@AllArgsConstructor
public enum DisruptorAdvancedChainOption {

  /**
   * 串行操作
   */
  SERIAL("串行操作"),
  /**
   * 并行操作一
   */
  PARALLEL_I("并行操作一"),
  /**
   * 并行操作二
   */
  PARALLEL_II("并行操作二"),
  /**
   * 菱形操作一
   */
  DIAMOND_I("菱形操作一"),
  /**
   * 菱形操作二
   */
  DIAMOND_II("菱形操作二"),
  /**
   * 六边形操作
   */
  HEXAGON("六边形操作");

  private String desc;

}
