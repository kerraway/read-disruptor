package com.github.kerraway.disruptor.juc;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Collection;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author kerraway
 * @date 2019/3/16
 */
@Slf4j
public class JucTest {

  /**
   * {@link ConcurrentHashMap.Segment} 继承了 {@link ReentrantLock}，
   * {@link ConcurrentHashMap} 借助它可以实现对数据的分段锁定，通过减少锁的粒度，来达到提高并发性能的目的。
   * <p>
   * {@link ConcurrentHashMap#DEFAULT_CAPACITY} 等于 16，所以默认是将数据分成 16 段。
   *
   * @see ConcurrentHashMap
   */
  @Test
  public void concurrentHashMapTest() {
    ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>();
    concurrentHashMap.put("key1", "value1");
    String value = concurrentHashMap.get("key1");
    logger.info("value: {}", value);
  }

  /**
   * CopyOnWrite 的原理是写操作（{@link CopyOnWriteArrayList#add(Object)}）都是将原来的数据复制一份，
   * 然后修改副本数据，最后将指向原数据的指针指向副本数据，在写操作时，会使用 {@link ReentrantLock} 加锁，
   * 锁对象由容器对象持有，例如 {@link CopyOnWriteArrayList#lock}。
   * <p>
   * 读操作（{@link CopyOnWriteArrayList#get(int)}），无特殊逻辑。
   * <p>
   * 本质上，是读写分离思想的体现。
   * <p>
   * 使用场景：读多写少，数据量不是很大。
   *
   * @see CopyOnWriteArrayList
   * @see CopyOnWriteArraySet
   */
  @Test
  public void copyOnWriteTest() {
    CopyOnWriteArrayList<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
    copyOnWriteArrayList.add(1);
    Integer num = copyOnWriteArrayList.get(0);
    logger.info("num: {}", num);
  }

  /**
   * {@link SynchronousQueue} 是没有容量的，{@link SynchronousQueue#take()} 方法和 {@link SynchronousQueue#put(Object)}
   * 方法是相互阻塞的：只有 put 一个元素之后，take 才能取出元素；并且只有 take 取出元素之后，put 才能放入下一个元素。
   *
   * @see SynchronousQueue
   */
  @Test
  public void synchronousQueueTest() throws InterruptedException {
    SynchronousQueue<Integer> synchronousQueue = new SynchronousQueue<>();
    final Random random = new Random();
    new Thread(() -> {
      for (int i = 0; i < 5; i++) {
        try {
          TimeUnit.MILLISECONDS.sleep(random.nextInt(10));
          Integer take = synchronousQueue.take();
          logger.info("take: {}", take);
        } catch (InterruptedException e) {
          logger.error("InterruptedException occurred.", e);
        }
      }
    }).start();

    new Thread(() -> {
      for (int i = 0; i < 10; i++) {
        try {
          logger.info("put: {}", i);
          synchronousQueue.put(i);
        } catch (InterruptedException e) {
          logger.error("InterruptedException occurred.", e);
        }
      }
    }).start();

    TimeUnit.SECONDS.sleep(2);
  }

  /**
   * {@link PriorityBlockingQueue} 是一个最小优先级队列，优先使用 {@link PriorityBlockingQueue#comparator} 排序，
   * 如果 {@link PriorityBlockingQueue#comparator} 为 null，则根据将元素强转成 {@link Comparable} 进行排序，所以
   * 元素需要实现 {@link Comparable} 接口。
   * <p>
   * 排序操作，是在调用 {@link PriorityBlockingQueue#offer(Object)} 时，借助
   * {@link PriorityBlockingQueue#siftUpUsingComparator(int, Object, Object[], Comparator)} 或者
   * {@link PriorityBlockingQueue#siftUpComparable(int, Object, Object[])}
   * （{@link PriorityBlockingQueue#comparator} 为 null 时）实现，
   * <pre>
   *    //PriorityBlockingQueue#offer(Object) 示例代码
   *    Comparator<? super E> cmp = comparator;
   *    if (cmp == null)
   *        siftUpComparable(n, e, array);
   *    else
   *        siftUpUsingComparator(n, e, array, cmp);
   * </pre>
   * 以及调用 {@link PriorityBlockingQueue#removeAt(int)} 时，借助
   * {@link PriorityBlockingQueue#siftDownUsingComparator(int, Object, Object[], int, Comparator)} 或者
   * {@link PriorityBlockingQueue#siftDownComparable(int, Object, Object[], int)}
   * （{@link PriorityBlockingQueue#comparator} 为 null 时），和
   * {@link PriorityBlockingQueue#siftUpUsingComparator(int, Object, Object[], Comparator)} 或者
   * {@link PriorityBlockingQueue#siftUpComparable(int, Object, Object[])}
   * （{@link PriorityBlockingQueue#comparator} 为 null 时）实现。
   * <pre>
   *    //PriorityBlockingQueue#removeAt(int) 示例代码
   *    if (cmp == null)
   *        siftDownComparable(i, moved, array, n);
   *    else
   *        siftDownUsingComparator(i, moved, array, n, cmp);
   *    if (array[i] == moved) {
   *        if (cmp == null)
   *            siftUpComparable(i, moved, array);
   *        else
   *            siftUpUsingComparator(i, moved, array, cmp);
   *    }
   * </pre>
   * 在使用构造函数 {@link PriorityBlockingQueue#PriorityBlockingQueue(Collection)} 构建队列时，会使用
   * {@link PriorityBlockingQueue#heapify()} 方法做排序操作。
   * <p>
   * 注：最小最大是相对的，通过改变构造函数传入的 {@link Comparator} 或者元素实现的 {@link Comparable#compareTo(Object)}
   * 方法，能够让 {@link PriorityBlockingQueue} 的元素优先级反转。
   *
   * @see PriorityBlockingQueue
   */
  @Test
  public void priorityBlockingQueueTest() throws InterruptedException {
    PriorityBlockingQueue<Integer> priorityBlockingQueue = new PriorityBlockingQueue<>();
    final Random random = new Random();
    for (int i = 0; i < 100; i++) {
      priorityBlockingQueue.put(random.nextInt(10000));
    }
    logger.info("PriorityBlockingQueue: {}", priorityBlockingQueue);
    for (int i = 0; i < 10; i++) {
      Integer take = priorityBlockingQueue.take();
      logger.info("{}. take {}", i, take);
    }
  }

  /**
   * 放入 {@link DelayQueue} 的元素，需要实现 {@link Delayed} 接口，而 {@link Delayed} 接口继承了 {@link Comparable} 接口。
   * <p>
   * {@link DelayQueue} 会根据元素的过期时间排序，过期时间短的元素排在前面，这样在取出元素时，只需要查看第一个元素是否满足过期时间要求即可。
   * <p>
   * 当调用 {@link DelayQueue#take()} 获取元素时，{@link DelayQueue} 会调用 {@link Delayed#getDelay(TimeUnit)} 方法获取元素的
   * 过期时间，如果过期时间小于等于 0，那么意味着该元素可以被取出。
   * <p>
   * 参考 https://www.baeldung.com/java-delay-queue
   *
   * @see DelayQueue
   */
  @Test
  public void delayQueueTest() throws InterruptedException {
    DelayQueue<DelayElement> delayQueue = new DelayQueue<>();
    int total = 10;
    for (int i = 0; i < total; i++) {
      //0 -> 1000 ms, 1 -> 900 ms, ... 9 -> 100 ms
      delayQueue.put(new DelayElement(i, (10 - i) * 100));
    }
    for (int i = 0; i < total; i++) {
      /**
       * {@link DelayQueue#take()} 取不到元素时，会阻塞
       */
      DelayElement take = delayQueue.take();
      logger.info("{}. take {}, expiration {} ms", i, take, take.getDelay(TimeUnit.MILLISECONDS));
    }
  }

}
