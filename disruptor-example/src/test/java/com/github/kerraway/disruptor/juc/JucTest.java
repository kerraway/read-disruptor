package com.github.kerraway.disruptor.juc;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import sun.misc.Unsafe;

import java.util.Collection;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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

  /**
   * Atomic 系列类提供了原子性操作，用于保证多线程下的安全。
   * <p>
   * Atomic 系列类都是基于 {@link Unsafe} 类实现的，{@link Unsafe} 类的作用是：
   * 1. 内存操作
   * 2. 字段的定位与修改
   * 3. 挂起与恢复
   * 4. CAS 操作（乐观锁），例如 {@link Unsafe#compareAndSwapInt(Object, long, int, int)}
   *
   * @see AtomicInteger
   * @see AtomicLong
   * @see AtomicBoolean
   */
  @Test
  public void atomicTest() {
    AtomicInteger atomicInteger = new AtomicInteger(1);
    logger.info("{}", atomicInteger.get());
    logger.info("{}", atomicInteger.compareAndSet(2, 3));
    logger.info("{}", atomicInteger.get());
    logger.info("{}", atomicInteger.compareAndSet(1, 2));
    logger.info("{}", atomicInteger.get());
  }

  /**
   * {@link CountDownLatch} 用于协调多线程的运行，调用 {@link CountDownLatch#await()} 的线程处于等待状态，
   * 等到其他线程都执行了 {@link CountDownLatch#countDown()} 后，被阻塞线程会继续运行。
   *
   * @see CountDownLatch
   */
  @Test
  public void countDownLatchTest() throws InterruptedException {
    int total = 5;
    ExecutorService executor = Executors.newFixedThreadPool(total);
    CountDownLatch latch = new CountDownLatch(total);
    Random random = new Random();
    for (int i = 0; i < total; i++) {
      int finalI = i;
      executor.submit(() -> {
        try {
          TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
        } catch (InterruptedException e) {
          logger.error("InterruptedException occurred.", e);
        }
        logger.info("Thread {} end.", finalI);
        latch.countDown();
      });
    }

    //阻塞，等待其他所有线程都调用 latch.countDown() 方法
    latch.await();

    executor.shutdown();
    logger.info("Main thread end.");
  }

  /**
   * {@link CyclicBarrier} 用于协调多线程的运行，调用 {@link CyclicBarrier#await()} 的线程处于等待状态，
   * 等到所有线程都执行了该方法，也就是说所有线程都就绪了，所有线程会一起继续运行。
   *
   * @see CyclicBarrier
   */
  @Test
  public void cyclicBarrierTest() throws InterruptedException {
    int total = 5;
    ExecutorService executor = Executors.newFixedThreadPool(total);
    CyclicBarrier cyclicBarrier = new CyclicBarrier(total);
    Random random = new Random();
    for (int i = 0; i < total; i++) {
      int finalI = i;
      executor.submit(() -> {
        try {
          TimeUnit.MILLISECONDS.sleep(random.nextInt(1000));
        } catch (InterruptedException e) {
          logger.error("InterruptedException occurred.", e);
        }
        logger.info("Thread {} ready.", finalI);
        try {
          //阻塞，等待其他线程就绪
          cyclicBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
          e.printStackTrace();
        }
        logger.info("Thread {} end.", finalI);
      });
    }

    TimeUnit.SECONDS.sleep(2);
    executor.shutdown();
    logger.info("Main thread end.");
  }

  /**
   * {@link Semaphore} 用于控制允许并发执行的最大线程数。
   * <p>
   * 注意：任务已经被提交给线程池，只是在执行的时候被控制并发数，所以任务会堆积在线程池
   * 的任务队列 {@link ThreadPoolExecutor#workQueue} 中。
   *
   * @see Semaphore
   */
  @Test
  public void semaphoreTest() throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(20);
    Semaphore semaphore = new Semaphore(10);
    for (int i = 0; i < 100; i++) {
      int finalI = i;
      executor.submit(() -> {
        try {
          semaphore.acquire();
          TimeUnit.MILLISECONDS.sleep(1000);
          logger.info("{}. thread: {}", finalI, Thread.currentThread().getName());
        } catch (InterruptedException e) {
          logger.error("InterruptedException occurred.", e);
        } finally {
          semaphore.release();
        }
      });
    }

    TimeUnit.SECONDS.sleep(10);
    executor.shutdown();
    logger.info("Main thread end.");
  }

  /**
   * @see Future
   * @see Callable
   */
  @Test
  public void futureTest() throws ExecutionException, InterruptedException {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<String> future = executor.submit(new Callable<String>() {
      @Override
      public String call() throws Exception {
        try {
          logger.info("Callable#call() method start.");
          TimeUnit.SECONDS.sleep(2);
          return "Hello future and callable.";
        } finally {
          logger.info("Callable#call() method end.");
        }
      }
    });

    String result = null;
    try {
      result = future.get(100, TimeUnit.MILLISECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      logger.warn("Exception occurred.", e);
    }
    logger.info("Result from future: {}", result);

    result = future.get();
    logger.info("Result from future: {}", result);

    executor.shutdown();
  }

}
