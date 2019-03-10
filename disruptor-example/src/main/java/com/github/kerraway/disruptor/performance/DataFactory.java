package com.github.kerraway.disruptor.performance;

import com.lmax.disruptor.EventFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author kerraway
 * @date 2019/3/10
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class DataFactory implements EventFactory<Data> {

  @Override
  public Data newInstance() {
    return new Data();
  }

}
