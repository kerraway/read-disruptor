package com.github.kerraway.disruptor.performance;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author kerraway
 * @date 2019/3/10
 */
@lombok.Data
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class Data implements Serializable {

  private static final long serialVersionUID = -3140425127575463618L;

  private Integer id;
  private String name;

}
