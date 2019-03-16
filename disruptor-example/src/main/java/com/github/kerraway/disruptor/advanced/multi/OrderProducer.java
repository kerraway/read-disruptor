package com.github.kerraway.disruptor.advanced.multi;

import com.lmax.disruptor.RingBuffer;
import lombok.Data;

/**
 * @author kerraway
 * @date 2019/3/16
 */
@Data
public class OrderProducer {

  private final RingBuffer<Order> ringBuffer;

  OrderProducer(RingBuffer<Order> ringBuffer) {
    this.ringBuffer = ringBuffer;
  }

  public void sendData(String uuid) {
    long sequence = ringBuffer.next();
    try {
      Order order = ringBuffer.get(sequence);
      order.setId(uuid);
    } finally {
      ringBuffer.publish(sequence);
    }
  }
}
