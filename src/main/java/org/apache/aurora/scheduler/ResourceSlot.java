/**
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
package org.apache.aurora.scheduler;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.twitter.common.quantity.Amount;
import com.twitter.common.quantity.Data;

import org.apache.aurora.scheduler.mesos.ExecutorSettings;
import org.apache.aurora.scheduler.preemptor.PreemptionVictim;
import org.apache.aurora.scheduler.storage.entities.ITaskConfig;
import org.apache.mesos.Protos;

import static org.apache.mesos.Protos.Offer;

/**
 * Resource containing class that is aware of executor overhead.
 */
public final class ResourceSlot {
  // TODO(zmanji): Remove this class and overhead in 0.8.0 (AURORA-906)

  private final Resources resources;

  /**
   * Minimum resources required to run Thermos. In the wild Thermos needs about 0.01 CPU and
   * about 170MB (peak usage) of RAM. The RAM requirement has been rounded up to a power of 2.
   */
  @VisibleForTesting
  public static final Resources MIN_THERMOS_RESOURCES = new Resources(
      0.01,
      Amount.of(256L, Data.MB),
      Amount.of(1L, Data.MB),
      0);

  private ResourceSlot(Resources r) {
    this.resources = r;
  }

  public static ResourceSlot from(ITaskConfig task, ExecutorSettings executorSettings) {
    return from(Resources.from(task), executorSettings);
  }

  public static ResourceSlot from(PreemptionVictim victim, ExecutorSettings executorSettings) {
    return from(victim.getResources(), executorSettings);
  }

  private static ResourceSlot from(Resources resources, ExecutorSettings executorSettings) {
    // Apply a flat 'tax' of executor overhead resources to the task.
    Resources requiredTaskResources = sum(
        resources,
        executorSettings.getExecutorOverhead());

    // Upsize tasks smaller than the minimum resources required to run the executor.
    return new ResourceSlot(maxElements(requiredTaskResources, MIN_THERMOS_RESOURCES));
  }

  /**
   * Generates a Resource where each resource component is a max out of the two components.
   *
   * @param a A resource to compare.
   * @param b A resource to compare.
   *
   * @return Returns a Resources instance where each component is a max of the two components.
   */
  @VisibleForTesting
  static Resources maxElements(Resources a, Resources b) {
    double maxCPU = Math.max(a.getNumCpus(), b.getNumCpus());
    Amount<Long, Data> maxRAM = Amount.of(
        Math.max(a.getRam().as(Data.MB), b.getRam().as(Data.MB)),
        Data.MB);
    Amount<Long, Data> maxDisk = Amount.of(
        Math.max(a.getDisk().as(Data.MB), b.getDisk().as(Data.MB)),
        Data.MB);
    int maxPorts = Math.max(a.getNumPorts(), b.getNumPorts());

    return new Resources(maxCPU, maxRAM, maxDisk, maxPorts);
  }

  public static ResourceSlot from(Offer offer) {
    return new ResourceSlot(Resources.from(offer));
  }

  public double getNumCpus() {
    return resources.getNumCpus();
  }

  public Amount<Long, Data> getRam() {
    return resources.getRam();
  }

  public Amount<Long, Data> getDisk() {
    return resources.getDisk();
  }

  public int getNumPorts() {
    return resources.getNumPorts();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ResourceSlot)) {
      return false;
    }

    ResourceSlot other = (ResourceSlot) o;
    return resources.equals(other.resources);
  }

  @Override
  public int hashCode() {
    return resources.hashCode();
  }

  public static ResourceSlot sum(ResourceSlot... rs) {
    return sum(Arrays.asList(rs));
  }

  public static ResourceSlot sum(Iterable<ResourceSlot> rs) {
    Resources sum = Resources.NONE;

    for (ResourceSlot r : rs) {
      double numCpus = sum.getNumCpus() + r.getNumCpus();
      Amount<Long, Data> disk =
          Amount.of(sum.getDisk().as(Data.BYTES) + r.getDisk().as(Data.BYTES), Data.BYTES);
      Amount<Long, Data> ram =
          Amount.of(sum.getRam().as(Data.BYTES) + r.getRam().as(Data.BYTES), Data.BYTES);
      int ports = sum.getNumPorts() + r.getNumPorts();
      sum = new Resources(numCpus, ram, disk, ports);
    }

    return new ResourceSlot(sum);
  }

  @VisibleForTesting
  public static Resources sum(Resources a, Resources b) {
    return sum(ImmutableList.of(new ResourceSlot(a), new ResourceSlot(b))).resources;
  }

  public static ResourceSlot subtract(ResourceSlot a, Resources b) {
    return new ResourceSlot(subtract(a.resources, b));
  }

  @VisibleForTesting
  static Resources subtract(Resources a, Resources b) {
    return new Resources(
        a.getNumCpus() - b.getNumCpus(),
        Amount.of(a.getRam().as(Data.MB) - b.getRam().as(Data.MB), Data.MB),
        Amount.of(a.getDisk().as(Data.MB) - b.getDisk().as(Data.MB), Data.MB),
        a.getNumPorts() - b.getNumPorts());
  }

  public List<Protos.Resource> toResourceList(Set<Integer> selectedPorts) {
    return resources.toResourceList(selectedPorts);
  }

  public static final Ordering<ResourceSlot> ORDER = new Ordering<ResourceSlot>() {
    @Override
    public int compare(ResourceSlot left, ResourceSlot right) {
      return RESOURCE_ORDER.compare(left.resources, right.resources);
    }
  };

  /**
   * A Resources object is greater than another iff _all_ of its resource components are greater
   * or equal. A Resources object compares as equal if some but not all components are greater than
   * or equal to the other.
   */
  public static final Ordering<Resources> RESOURCE_ORDER = new Ordering<Resources>() {
    @Override
    public int compare(Resources left, Resources right) {
      int diskC = left.getDisk().compareTo(right.getDisk());
      int ramC = left.getRam().compareTo(right.getRam());
      int portC = Integer.compare(left.getNumPorts(), right.getNumPorts());
      int cpuC = Double.compare(left.getNumCpus(), right.getNumCpus());

      FluentIterable<Integer> vector =
          FluentIterable.from(ImmutableList.of(diskC, ramC, portC, cpuC));

      if (vector.allMatch(IS_ZERO))  {
        return 0;
      }

      if (vector.filter(Predicates.not(IS_ZERO)).allMatch(e -> e > 0)) {
        return 1;
      }

      if (vector.filter(Predicates.not(IS_ZERO)).allMatch(e -> e < 0)) {
        return -1;
      }

      return 0;
    }
  };

  private static final Predicate<Integer> IS_ZERO = e -> e == 0;
}
