package de.fiereu

import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

/**
 * A simple ArrayList that utilizes multi-threading for filtering.
 *
 * @param T The type of the ArrayList
 * @see ArrayList
 * @see Thread
 */
class FastFilterArrayList<T>(
    /**
     * The maximum amount of threads that can be used for filtering.
     *
     * @see Thread
     * @note Don't set this too high, as it will cause a lot of overhead.
     * @default Runtime.getRuntime().availableProcessors()
     */
    private val maxThreadCount: Int = Runtime.getRuntime().availableProcessors(),
) : ArrayList<T>() {

  constructor(list: List<T>) : this() {
    addAll(list)
  }

  /**
   * Filters the ArrayList using the given predicate.
   * This method is multi-threaded. The amount of threads used is determined by the maxThreadCount.
   * @param predicate The predicate to filter the ArrayList with.
   * @return The filtered ArrayList.
   * @see Thread
   */
  fun fastFilter(predicate: (T) -> Boolean): FastFilterArrayList<T> {
    val filtered = Collections.synchronizedList(FastFilterArrayList<T>())
    val threads = ArrayList<Thread>()
    val chunkSize = size / maxThreadCount
    for (i in 0 until maxThreadCount) {
      val thread = Thread {
        val start = i * chunkSize
        val end = if (i == maxThreadCount - 1) size else start + chunkSize
        for (j in start until end) {
          val element = get(j)
          if (predicate(element)) {
            filtered.add(element)
          }
        }
      }
      threads.add(thread)
      thread.start()
    }
    for (thread in threads) {
      thread.join()
    }
    return FastFilterArrayList(filtered)
  }
}
