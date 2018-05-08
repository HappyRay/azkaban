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

/**
 * A NodeProcessor that bridges the sub DAG and the parent DAG.
 */
public class TestSubDagNodeProcessor implements NodeProcessor {

  private final DagService dagService;
  private final StatusChangeRecorder statusChangeRecorder;
  private final Dag dag;


  TestSubDagNodeProcessor(final DagService dagService,
      final StatusChangeRecorder statusChangeRecorder,
      final Dag dag
  ) {
    this.dagService = dagService;
    this.statusChangeRecorder = statusChangeRecorder;
    this.dag = dag;
  }


  /**
   * Triggers the sub DAG state change when the sub DAG node in the parent DAG's status changes.
   *
   * @param node the node to change
   * @param status the new status
   */
  @Override
  public void changeStatus(final Node node, final Status status) {
    System.out.println(node);
    this.statusChangeRecorder.recordNode(node);

    switch (status) {
      case RUNNING:
        this.dagService.startDag(this.dag);
        break;
      case KILLING:
        this.dagService.killDag(this.dag);
        break;
    }
  }
}
