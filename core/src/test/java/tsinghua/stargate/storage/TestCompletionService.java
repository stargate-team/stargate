/*
 * Copyright 2017 The Tsinghua University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tsinghua.stargate.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

public class TestCompletionService {

  @Test
  public void testCompletionService() {
    new TestCompletionService().run();
  }

  private void run() {
    ExecutorService pool = Executors.newFixedThreadPool(10);
    CompletionService<Integer> completionServcie =
        new ExecutorCompletionService<>(pool);

    List<Future<Integer>> res = new ArrayList<>();
    try {
      for (int i = 0; i < 10; i++) {
        res.add(completionServcie.submit(new TestCompletionService.Task(i)));
      }

      long ref = 0;
      List<Integer> count = new ArrayList<>();

      while (true) {
        for (int i = 0; i < 10; i++) {
          Future<Integer> tmp = completionServcie.poll();

          String s;
          if (tmp == null) {
            s = i + " null";
          } else {
            s = i + " " + tmp.get().toString();
            count.add(tmp.get());
          }

          if (ref % 50000000 == 0) {
            System.out.println(s);
            System.out.println(i + " " + res.get(i).isDone());
            ref = 0;
          }
        }

        ref++;
        if (count.size() == 10) {
          break;
        }
      }

    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    } finally {
      pool.shutdown();
    }
  }

  public static class Task implements Callable<Integer> {

    private int i;

    Task(int i) {
      this.i = i;
    }

    @Override
    public Integer call() throws Exception {
      Thread.sleep(new Random().nextInt(20000));
      System.out.println(Thread.currentThread().getName() + "   " + i);
      return i;
    }
  }
}