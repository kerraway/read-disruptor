package com.github.kerraway.disruptor.juc;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Volatile 作用：
 * 1. 多线程间的可见性
 * 2. 阻止指令重排序
 * <p>
 * Volatile 示例：
 * <p>
 * <code>isRunning = true</code>
 * <p>
 * 如果 isRunning 变量被 volatile 修饰，则当变量改变时，强制线程执行引擎去主内存中读取，而不是在线程工作内存中读取。
 * <pre>
 * +------------------------------------------------------+
 * |  +-----------------+                                 |
 * |  | isRunning = true|          主 内 存                |
 * |  +-----------------+                                 |
 * +------------------------------------------------+-----+
 *            |                                     ^
 *            |                                     |
 *        READ LOAD                            STORE WRITE
 *            |                                     |
 *            v                                     |
 * +------------------------------------------------+-----+
 * |  +-----------------+                                 |
 * |  | isRunning = true|       线 程 工 作 内 存           |
 * |  +-----------------+                                 |
 * +------------------------------------------------+-----+
 *            |                                     ^
 *            |                                     |
 *          ASSIGN                                 USED
 *            |                                     |
 *            v                                     |
 * +----------+-------------------------------------+-----+
 * |                                                      |
 * |                  线 程 执 行 引 擎                     |
 * |                                                      |
 * +------------------------------------------------------+
 * </pre>
 *
 * @author kerraway
 * @date 2019/3/17
 */
@Slf4j
public class VolatileTest {

  private volatile boolean isRunning = true;

  /**
   * Ref https://stackoverflow.com/questions/5816790/the-code-example-which-can-prove-volatile-declare-should-be-used
   * <p>
   * Here is an example of why volatile is necessary.
   * <p>
   * If you remove the keyword volatile, thread 1 may never terminate. (When I tested on Java 1.6 Hotspot on Linux,
   * this was indeed the case - your results may vary as the JVM is not obliged to do any caching of variables not
   * marked volatile.)
   */
  @Test
  public void test() throws InterruptedException {
    new Thread(() -> {
      int counter = 0;
      while (isRunning) {
        counter++;
      }
      logger.info("Thread 1 finished. Counted up to {}.", counter);
    }, "thread-1").start();

    new Thread(() -> {
      // Sleep for a bit so that thread 1 has a chance to start
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        logger.error("InterruptedException occurred.", e);
      }
      logger.info("Thread 2 finishing");
      isRunning = false;
    }, "thread-2").start();

    TimeUnit.SECONDS.sleep(5);
    logger.info("Main thread end.");
  }

}