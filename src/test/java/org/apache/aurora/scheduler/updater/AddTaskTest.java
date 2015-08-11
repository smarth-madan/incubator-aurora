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
package org.apache.aurora.scheduler.updater;

import com.twitter.common.testing.easymock.EasyMockTest;

import org.apache.aurora.gen.InstanceKey;
import org.apache.aurora.gen.JobUpdateInstructions;
import org.apache.aurora.gen.JobUpdateSettings;
import org.apache.aurora.gen.JobUpdateStatus;
import org.apache.aurora.scheduler.base.JobKeys;
import org.apache.aurora.scheduler.state.StateManager;
import org.apache.aurora.scheduler.storage.entities.IInstanceKey;
import org.apache.aurora.scheduler.storage.entities.IJobUpdateInstructions;
import org.junit.Before;
import org.junit.Test;

import static org.apache.aurora.scheduler.storage.Storage.MutableStoreProvider;

public class AddTaskTest extends EasyMockTest {
  private static final IJobUpdateInstructions INSTRUCTIONS = IJobUpdateInstructions.build(
      new JobUpdateInstructions()
          .setSettings(
              new JobUpdateSettings()
                  .setMinWaitInInstanceRunningMs(1000)));
  private static final IInstanceKey INSTANCE =
      IInstanceKey.build(new InstanceKey(JobKeys.from("role", "env", "job").newBuilder(), 0));

  private MutableStoreProvider storeProvider;
  private StateManager stateManager;
  private InstanceActionHandler handler;

  @Before
  public void setUp() {
    stateManager = createMock(StateManager.class);
    storeProvider = createMock(MutableStoreProvider.class);
    handler = new InstanceActionHandler.AddTask();
  }

  @Test(expected = IllegalStateException.class)
  public void testInstanceNotFound() throws Exception {
    control.replay();

    handler.getReevaluationDelay(
        INSTANCE,
        INSTRUCTIONS,
        storeProvider,
        stateManager,
        JobUpdateStatus.ROLLING_BACK);
  }
}
