// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.grid.distributor.model;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;
import static org.openqa.selenium.grid.data.SessionClosedEvent.SESSION_CLOSED;
import static org.openqa.selenium.grid.distributor.model.Slot.Status.ACTIVE;
import static org.openqa.selenium.grid.distributor.model.Slot.Status.AVAILABLE;
import static org.openqa.selenium.grid.data.Availability.DOWN;
import static org.openqa.selenium.grid.data.Availability.DRAINING;
import static org.openqa.selenium.grid.data.Availability.UP;

import com.google.common.collect.ImmutableList;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.events.EventBus;
import org.openqa.selenium.grid.data.CreateSessionRequest;
import org.openqa.selenium.grid.data.CreateSessionResponse;
import org.openqa.selenium.grid.data.DistributorStatus;
import org.openqa.selenium.grid.data.NodeId;
import org.openqa.selenium.grid.data.NodeStatus;
import org.openqa.selenium.grid.data.Session;

import org.openqa.selenium.grid.data.Availability;

import org.openqa.selenium.grid.node.HealthCheck;
import org.openqa.selenium.grid.node.Node;
import org.openqa.selenium.internal.Require;
import org.openqa.selenium.remote.SessionId;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static java.util.stream.Collectors.groupingBy;

public class Host {

  private static final Logger LOG = Logger.getLogger("Selenium Host");
  private final Node node;
  private final NodeId nodeId;
  private final URI uri;
  private final Runnable performHealthCheck;

  // Used any time we need to read or modify the mutable state of this host
  private final ReadWriteLock lock = new ReentrantReadWriteLock(/* fair */ true);
  private Availability status;
  private List<Slot> slots;
  private int maxSessionCount;

  public Host(EventBus bus, Node node) {
    this.node = Require.nonNull("Node", node);

    this.nodeId = node.getId();
    this.uri = node.getUri();

    this.status = DOWN;
    this.slots = ImmutableList.of();

    HealthCheck healthCheck = node.getHealthCheck();

    this.performHealthCheck = () -> {
      HealthCheck.Result result = healthCheck.check();
      Availability current = result.isAlive() ? UP : DOWN;
      Availability previous = setHostStatus(current);

      //If the node has been set to maintenance mode, set the status here as draining
      if (node.isDraining() || previous == DRAINING) {
        // We want to continue to allow the node to drain.
        setHostStatus(DRAINING);
        return;
      }

      if (current != previous) {
        LOG.info(String.format(
            "Changing status of node %s from %s to %s. Reason: %s",
            node.getId().toString(),
            previous,
            current,
            result.getMessage()));
      }
    };

    bus.addListener(SESSION_CLOSED, event -> {
      SessionId id = event.getData(SessionId.class);
      this.slots.forEach(slot -> slot.onEnd(id));
    });

    update(node.getStatus());
  }

  public void update(NodeStatus status) {
    Require.nonNull("Node status", status);

    Lock writeLock = lock.writeLock();
    writeLock.lock();
    try {
      // This is grossly inefficient. But we're on a modern processor and we're expecting 10s to 100s
      // of nodes, so this is probably ok.
      Set<NodeStatus.Active> sessions = status.getCurrentSessions();
      Map<Capabilities, Integer> actives = sessions.parallelStream().collect(
          groupingBy(NodeStatus.Active::getStereotype, summingInt(active -> 1)));

      ImmutableList.Builder<Slot> slots = ImmutableList.builder();
      status.getStereotypes().forEach((caps, count) -> {
        if (actives.containsKey(caps)) {
          Integer activeCount = actives.get(caps);
          for (int i = 0; i < activeCount; i++) {
            slots.add(new Slot(node, caps, ACTIVE));
          }
          count -= activeCount;
        }

        for (int i = 0; i < count; i++) {
          slots.add(new Slot(node, caps, AVAILABLE));
        }
      });
      this.slots = slots.build();

      // By definition, we can never have more sessions than we have slots available
      this.maxSessionCount = Math.min(this.slots.size(), status.getMaxSessionCount());
    } finally {
      writeLock.unlock();
    }
  }

  public NodeId getId() {
    return nodeId;
  }

  public DistributorStatus.NodeSummary asSummary() {
    Map<Capabilities, Integer> stereotypes = new HashMap<>();
    Map<Capabilities, Integer> used = new HashMap<>();
    Set<Session> activeSessions = new HashSet<>();

    slots.forEach(slot -> {
      stereotypes.compute(slot.getStereotype(), (key, curr) -> curr == null ? 1 : curr + 1);
      if (slot.getStatus() != AVAILABLE) {
        used.compute(slot.getStereotype(), (key, curr) -> curr == null ? 1 : curr + 1);
        activeSessions.add(slot.getCurrentSession());
      }
    });

    return new DistributorStatus.NodeSummary(
        nodeId,
        uri,
        status,
        maxSessionCount,
        stereotypes,
        used,
        activeSessions);
  }

  public Availability getHostStatus() {
    return status;
  }

  /**
   * @return The previous status of the node.
   */
  private Availability setHostStatus(Availability status) {
    Availability toReturn = this.status;
    this.status = Require.nonNull("Status", status);
    return toReturn;
  }

  /**
   * @return The previous status of the node if it not able to drain else returning draining status.
   */
  public Availability drainHost() {
    Availability prev = this.status;

    // Drain the node
    if (!node.isDraining()) {
      node.drain();
    }

    // For some reason, it is still not draining then do not update the host status
    if (!node.isDraining()) {
      return prev;
    } else {
      this.status = DRAINING;
      return DRAINING;
    }
  }

  /**
   * @return Whether or not the host has slots available for the requested capabilities.
   */
  public boolean hasCapacity(Capabilities caps) {
    Lock read = lock.readLock();
    read.lock();
    try {
      long count = slots.stream()
          .filter(slot -> slot.isSupporting(caps))
          .filter(slot -> slot.getStatus() == AVAILABLE)
          .count();

      return count > 0;
    } finally {
      read.unlock();
    }
  }

  public float getLoad() {
    Lock read = lock.readLock();
    read.lock();
    try {
      float inUse = slots.parallelStream()
          .filter(slot -> slot.getStatus() != AVAILABLE)
          .count();

      return (inUse / (float) maxSessionCount) * 100f;
    } finally {
      read.unlock();
    }
  }

  public long getLastSessionCreated() {
    Lock read = lock.readLock();
    read.lock();
    try {
      return slots.parallelStream()
          .mapToLong(Slot::getLastSessionCreated)
          .max()
          .orElse(0);
    } finally {
      read.unlock();
    }
  }

  public Supplier<CreateSessionResponse> reserve(CreateSessionRequest sessionRequest) {
    Require.nonNull("Session creation request", sessionRequest);

    Lock write = lock.writeLock();
    write.lock();
    try {
      Slot toReturn = slots.stream()
          .filter(slot -> slot.isSupporting(sessionRequest.getCapabilities()))
          .filter(slot -> slot.getStatus() == AVAILABLE)
          .findFirst()
          .orElseThrow(() -> new SessionNotCreatedException("Unable to reserve an instance"));

      return toReturn.onReserve(sessionRequest);
    } finally {
      write.unlock();
    }
  }

  public void runHealthCheck() {
    performHealthCheck.run();
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeId, uri);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Host)) {
      return false;
    }

    Host that = (Host) obj;
    return this.node.equals(that.node);
  }
}
