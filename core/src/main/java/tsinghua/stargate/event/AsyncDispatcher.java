/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tsinghua.stargate.event;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import tsinghua.stargate.annotation.InterfaceAudience.Public;
import tsinghua.stargate.annotation.InterfaceStability.Evolving;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.exception.StarGateRuntimeException;
import tsinghua.stargate.service.AbstractService;
import tsinghua.stargate.util.ShutdownHookManager;

@Public
@Evolving
public class AsyncDispatcher extends AbstractService implements Dispatcher {

  private final BlockingQueue<Event> eventQueue;
  private volatile boolean stopped;
  private Thread eventHandlingThread;
  protected final Map<Class<? extends Enum>, EventHandler> eventDispatchers;
  private boolean exitOnDispatchException;

  public AsyncDispatcher() {
    this(new LinkedBlockingQueue<>());
  }

  public AsyncDispatcher(BlockingQueue<Event> eventQueue) {
    super("AsyncDispatcher");
    this.stopped = false;
    this.eventQueue = eventQueue;
    this.eventDispatchers = new HashMap<>();
  }

  Runnable createThread() {
    return new Runnable() {
      public void run() {
        while (!AsyncDispatcher.this.stopped
            && !Thread.currentThread().isInterrupted()) {
          Event event;
          try {
            event = AsyncDispatcher.this.eventQueue.take();
          } catch (InterruptedException var3) {
            if (!AsyncDispatcher.this.stopped) {
              warn("AsyncDispatcher thread interrupted", var3);
            }

            return;
          }

          if (event != null) {
            AsyncDispatcher.this.dispatch(event);
          }
        }
      }
    };
  }

  protected void serviceInit(Configuration conf) throws Exception {
    info("Init service '{}'", this.getClass().getSimpleName());
    this.exitOnDispatchException =
        conf.getBoolean("stargate.dispatcher.exit-on-error", true);

    super.serviceInit(conf);
  }

  protected void serviceStart() throws Exception {
    this.eventHandlingThread = new Thread(this.createThread());
    this.eventHandlingThread.setName("AsyncDispatcher event handler");
    this.eventHandlingThread.start();
    info("Successfully started service '{}'", this.getClass().getSimpleName());

    super.serviceStart();
  }

  protected void serviceStop() throws Exception {
    this.stopped = true;
    if (this.eventHandlingThread != null) {
      this.eventHandlingThread.interrupt();

      try {
        this.eventHandlingThread.join();
      } catch (InterruptedException var2) {
        warn("Interrupted Exception while stopping", var2);
      }
    }
    info("Successfully stopped service '{}'", this.getClass().getSimpleName());

    super.serviceStop();
  }

  @SuppressWarnings("unchecked")
  protected void dispatch(Event event) {
    debug("Dispatching the event " + event.getClass().getName() + "."
        + event.toString());

    Class type = event.getType().getDeclaringClass();

    try {
      EventHandler handler = this.eventDispatchers.get(type);
      if (handler == null) {
        throw new Exception("No handler registered for " + type);
      }

      handler.handle(event);
    } catch (Throwable var4) {
      error("Error in dispatcher thread", var4);
      if (this.exitOnDispatchException
          && !ShutdownHookManager.get().isShutdownInProgress()) {
        info("Exiting, bye...");
        System.exit(-1);
      }
    }

  }

  @SuppressWarnings("unchecked")
  public void register(Class<? extends Enum> eventType, EventHandler handler) {
    EventHandler<Event> registeredHandler =
        (EventHandler) this.eventDispatchers.get(eventType);
    info("Registering " + eventType.getSimpleName() + " for "
        + handler.getClass().getSimpleName());
    if (registeredHandler == null) {
      this.eventDispatchers.put(eventType, handler);
    } else {
      AsyncDispatcher.MultiListenerHandler multiHandler;
      if (!(registeredHandler instanceof AsyncDispatcher.MultiListenerHandler)) {
        multiHandler = new AsyncDispatcher.MultiListenerHandler();
        multiHandler.addHandler(registeredHandler);
        multiHandler.addHandler(handler);
        this.eventDispatchers.put(eventType, multiHandler);
      } else {
        multiHandler = (AsyncDispatcher.MultiListenerHandler) registeredHandler;
        multiHandler.addHandler(handler);
      }
    }
  }

  public EventHandler getEventHandler() {
    return new AsyncDispatcher.GenericEventHandler();
  }

  static class MultiListenerHandler implements EventHandler<Event> {
    List<EventHandler<Event>> listOfHandlers = new ArrayList<>();

    MultiListenerHandler() {
    }

    @SuppressWarnings("unchecked")
    public void handle(Event event) {
      Iterator i$ = this.listOfHandlers.iterator();

      while (i$.hasNext()) {
        EventHandler<Event> handler = (EventHandler) i$.next();
        handler.handle(event);
      }
    }

    void addHandler(EventHandler<Event> handler) {
      this.listOfHandlers.add(handler);
    }
  }

  class GenericEventHandler implements EventHandler<Event> {
    GenericEventHandler() {
    }

    public void handle(Event event) {
      int qSize = AsyncDispatcher.this.eventQueue.size();
      if (qSize != 0 && qSize % 1000 == 0) {
        info("Size of event-queue is " + qSize);
      }

      int remCapacity = AsyncDispatcher.this.eventQueue.remainingCapacity();
      if (remCapacity < 1000) {
        warn("Very low remaining capacity in the event-queue: " + remCapacity);
      }

      try {
        AsyncDispatcher.this.eventQueue.put(event);
      } catch (InterruptedException var5) {
        if (!AsyncDispatcher.this.stopped) {
          warn("AsyncDispatcher thread interrupted", var5);
        }

        throw new StarGateRuntimeException(var5);
      }
    }
  }
}
