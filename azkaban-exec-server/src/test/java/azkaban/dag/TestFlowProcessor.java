/*
 * Copyright 2017 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package azkaban.dag;

import java.util.concurrent.CountDownLatch;

public class TestFlowProcessor implements FlowProcessor {

  private final StatusChangeRecorder statusChangeRecorder;
  CountDownLatch flowFinishedLatch;


  public TestFlowProcessor(final CountDownLatch flowFinishedLatch,
      StatusChangeRecorder statusChangeRecorder) {
    this.flowFinishedLatch = flowFinishedLatch;
    this.statusChangeRecorder = statusChangeRecorder;
  }

  @Override
  public void changeStatus(final Flow flow, final Status status) {
    System.out.println(flow);
    statusChangeRecorder.recordFlow(flow);
    if (Status.isTerminal(status)) {
      this.flowFinishedLatch.countDown();
    }
  }
}