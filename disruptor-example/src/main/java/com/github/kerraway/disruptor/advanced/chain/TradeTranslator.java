package com.github.kerraway.disruptor.advanced.chain;

import com.lmax.disruptor.EventTranslator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Random;

/**
 * @author kerraway
 * @date 2019/3/11
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class TradeTranslator implements EventTranslator<Trade> {

  private static final Random RANDOM = new Random();

  @Override
  public void translateTo(Trade event, long sequence) {
    event.setId(RANDOM.nextInt(Integer.MAX_VALUE));
    event.setPrice(new BigDecimal(RANDOM.nextInt(1000)));

    logger.info("TradeTranslator: {}", event);
  }

}
