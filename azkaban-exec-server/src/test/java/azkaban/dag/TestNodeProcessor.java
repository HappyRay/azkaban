/*
 * Copyright 2018 LinkedIn Corp.
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

import java.util.Set;

public class TestNodeProcessor implements NodeProcessor {

  private final DagService dagService;
  private final StatusChangeRecorder statusChangeRecorder;
  private final Set<Node> nodesToFail;

  TestNodeProcessor(final DagService dagService,
      final StatusChangeRecorder statusChangeRecorder, final Set<Node> nodesToFail) {
    this.dagService = dagService;
    this.statusChangeRecorder = statusChangeRecorder;
    this.nodesToFail = nodesToFail;
  }

  @Override
  public void changeStatus(final Node node, final Status status) {
    System.out.println(node);
    this.statusChangeRecorder.recordNode(node);

    switch (status) {
      case RUNNING:
        if (this.nodesToFail.contains(node)) {
          this.dagService.markNodeFailed(node);
        } else {
          this.dagService.markNodeSuccess(node);
        }
        break;
      case KILLING:
        this.dagService.markNodeFailed(node);
        break;
      default:
        // todo: save status
        break;
    }
  }
}
