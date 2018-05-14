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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.util.Pair;
import org.junit.After;
import org.junit.Test;

/**
 * Tests {@link DagService}
 *
 * <p>Naming conventions: nodes are named with letters such as a, b. Dags are named with 'f'
 * prefix. e.g. "fa". A sub DAG has the prefix "sf" such as "sfb". A sub DAG is a node within the
 * parent DAG.
 */
public class DagServiceTest {

  private final DagService dagService = new DagService();
  private final StatusChangeRecorder statusChangeRecorder = new StatusChangeRecorder();

  // The names of the nodes that are supposed to fail.
  private final Set<String> nodesToFail = new HashSet<>();
  private final TestNodeProcessor nodeProcessor = new TestNodeProcessor(this.dagService,
      this.statusChangeRecorder, this.nodesToFail);
  private final CountDownLatch dagFinishedLatch = new CountDownLatch(1);
  private final DagProcessor dagProcessor = new TestDagProcessor(this.dagFinishedLatch,
      this.statusChangeRecorder);
  private final DagBuilder dagBuilder = new DagBuilder("fa", this.dagProcessor);
  private final List<Pair<String, Status>> expectedSequence = new ArrayList<>();


  @After
  public void tearDown() {
    this.dagService.shutdownAndAwaitTermination();
  }

  /**
   * Tests a DAG with one node which will run successfully.
   */
  @Test
  public void oneNodeSuccess() throws Exception {
    createNodeInTestDag("a");
    addToExpectedSequence("fa", Status.RUNNING);
    addToExpectedSequence("a", Status.RUNNING);
    addToExpectedSequence("a", Status.SUCCESS);
    addToExpectedSequence("fa", Status.SUCCESS);

    runAndVerify();
  }

  /**
   * Tests a DAG with two nodes which will run successfully.
   * a
   * |
   * b
   */
  @Test
  public void twoNodesSuccess() throws Exception {
    final NodeBuilder aBuilder = createNodeInTestDag("a");
    final NodeBuilder bBuilder = createNodeInTestDag("b");
    bBuilder.addParents(aBuilder);
    addToExpectedSequence("fa", Status.RUNNING);
    addToExpectedSequence("a", Status.RUNNING);
    addToExpectedSequence("a", Status.SUCCESS);
    addToExpectedSequence("b", Status.RUNNING);
    addToExpectedSequence("b", Status.SUCCESS);
    addToExpectedSequence("fa", Status.SUCCESS);

    runAndVerify();
  }

  /**
   * Tests a DAG with three nodes which will run successfully.
   * <pre>
   *    a
   *  /  \
   * b    c
   * </pre>
   */
  @Test
  public void threeNodesSuccess() throws Exception {
    final NodeBuilder aBuilder = createNodeInTestDag("a");
    final NodeBuilder bBuilder = createNodeInTestDag("b");
    final NodeBuilder cNode = createNodeInTestDag("c");
    aBuilder.addChildren(bBuilder, cNode);

    addToExpectedSequence("fa", Status.RUNNING);
    addToExpectedSequence("a", Status.RUNNING);
    addToExpectedSequence("a", Status.SUCCESS);
    addToExpectedSequence("b", Status.RUNNING);
    addToExpectedSequence("c", Status.RUNNING);
    addToExpectedSequence("b", Status.SUCCESS);
    addToExpectedSequence("c", Status.SUCCESS);
    addToExpectedSequence("fa", Status.SUCCESS);

    runAndVerify();

  }

  /**
   * Tests a DAG with one node which will fail.
   */
  @Test
  public void oneNodeFailure() throws Exception {
    final NodeBuilder aBuilder = createNodeInTestDag("a");
    this.nodesToFail.add(aBuilder.getName());
    addToExpectedSequence("fa", Status.RUNNING);
    addToExpectedSequence("a", Status.RUNNING);
    addToExpectedSequence("a", Status.FAILURE);
    addToExpectedSequence("fa", Status.FAILURE);

    runAndVerify();
  }

  /**
   * Tests a DAG with two nodes, fails the first one.
   *
   * Expects the child node to be marked canceled.
   *
   * a (fail)
   * |
   * b
   */
  @Test
  public void twoNodesFailFirst() throws Exception {
    final NodeBuilder aBuilder = createNodeInTestDag("a");
    final NodeBuilder bBuilder = createNodeInTestDag("b");
    bBuilder.addParents(aBuilder);
    this.nodesToFail.add(aBuilder.getName());

    addToExpectedSequence("fa", Status.RUNNING);
    addToExpectedSequence("a", Status.RUNNING);
    addToExpectedSequence("a", Status.FAILURE);
    addToExpectedSequence("b", Status.CANCELED);
    addToExpectedSequence("fa", Status.FAILURE);

    runAndVerify();
  }

  /**
   * Tests a DAG with three nodes with one failure.
   *
   * Expects the sibling nodes to finish.
   *
   * <pre>
   *       a
   *   /      \
   * b (fail)    c
   * </pre>
   */
  @Test
  public void threeNodesFailSecond() throws Exception {
    final NodeBuilder aBuilder = createNodeInTestDag("a");
    final NodeBuilder bBuilder = createNodeInTestDag("b");
    final NodeBuilder cBuilder = createNodeInTestDag("c");
    aBuilder.addChildren(bBuilder, cBuilder);

    this.nodesToFail.add(bBuilder.getName());

    addToExpectedSequence("fa", Status.RUNNING);
    addToExpectedSequence("a", Status.RUNNING);
    addToExpectedSequence("a", Status.SUCCESS);
    addToExpectedSequence("b", Status.RUNNING);
    addToExpectedSequence("c", Status.RUNNING);
    addToExpectedSequence("b", Status.FAILURE);
    addToExpectedSequence("c", Status.SUCCESS);
    addToExpectedSequence("fa", Status.FAILURE);

    runAndVerify();

  }

  /**
   * Tests a DAG with one subDag, all successful.
   *
   * <pre>
   *   sfb
   *   |
   *   c
   *
   * subDag: fb
   * a b
   * </pre>
   */
  @Test
  public void simple_sub_dag_success_case() throws Exception {
    final TestSubDagProcessor testSubDagProcessor = new TestSubDagProcessor
        (this.dagService, this.statusChangeRecorder);
    final DagBuilder subDagBuilder = new DagBuilder("fb", testSubDagProcessor);
    createNodeInDag("a", subDagBuilder);
    createNodeInDag("b", subDagBuilder);
    final Dag bDag = subDagBuilder.build();

    final TestSubDagNodeProcessor testSubDagNodeProcessor = new TestSubDagNodeProcessor
        (this.dagService, this.statusChangeRecorder, bDag);
    final NodeBuilder subDagNodeBuilder = this.dagBuilder
        .createNode("sfb", testSubDagNodeProcessor);
    testSubDagProcessor.setNode(subDagNodeBuilder.getNode());

    final NodeBuilder cBuilder = createNodeInTestDag("c");
    cBuilder.addParents(subDagNodeBuilder);

    addToExpectedSequence("fa", Status.RUNNING);
    addToExpectedSequence("sfb", Status.RUNNING);
    addToExpectedSequence("fb", Status.RUNNING);
    addToExpectedSequence("a", Status.RUNNING);
    addToExpectedSequence("b", Status.RUNNING);
    addToExpectedSequence("a", Status.SUCCESS);
    addToExpectedSequence("b", Status.SUCCESS);
    addToExpectedSequence("fb", Status.SUCCESS);
    addToExpectedSequence("sfb", Status.SUCCESS);
    addToExpectedSequence("c", Status.RUNNING);
    addToExpectedSequence("c", Status.SUCCESS);
    addToExpectedSequence("fa", Status.SUCCESS);

    runAndVerify();

  }

  private void createNodeInDag(final String name, final DagBuilder subDagBuilder) {
    subDagBuilder.createNode(name, this.nodeProcessor);
  }

  /**
   * Tests killing a dag.
   */
  @Test
  public void kill_a_node() throws Exception {
    final CountDownLatch nodeRunningLatch = new CountDownLatch(1);
    final TestKillNodeProcessor killNodeProcessor = new TestKillNodeProcessor(this.dagService,
        this.statusChangeRecorder, nodeRunningLatch);
    this.dagBuilder.createNode("a", killNodeProcessor);
    final Dag dag = this.dagBuilder.build();

    addToExpectedSequence("fa", Status.RUNNING);
    addToExpectedSequence("a", Status.RUNNING);
    addToExpectedSequence("fa", Status.KILLING);
    addToExpectedSequence("a", Status.KILLING);
    addToExpectedSequence("a", Status.KILLED);
    addToExpectedSequence("fa", Status.KILLED);

    this.dagService.startDag(dag);

    // Make sure the node is running before killing the DAG.
    nodeRunningLatch.await(1, TimeUnit.SECONDS);
    this.dagService.killDag(dag);

    final boolean isWaitSuccessful = this.dagFinishedLatch.await(1, TimeUnit.SECONDS);
    // Make sure the dag finishes.
    assertThat(isWaitSuccessful).isTrue();
    verifyStatusSequence();
  }

  private void addToExpectedSequence(final String name, final Status status) {
    this.expectedSequence.add(new Pair<>(name, status));
  }

  private void runDag() throws InterruptedException {
    final Dag dag = this.dagBuilder.build();
    this.dagService.startDag(dag);
    final boolean isWaitSuccessful = this.dagFinishedLatch.await(2, TimeUnit.SECONDS);

    // Make sure the dag finishes.
    assertThat(isWaitSuccessful).isTrue();
  }

  private void verifyStatusSequence() {
    this.statusChangeRecorder.verifySequence(this.expectedSequence);
  }

  private void runAndVerify() throws InterruptedException {
    runDag();
    verifyStatusSequence();
  }

  /**
   * Creates a node and add to the test dag.
   */
  private NodeBuilder createNodeInTestDag(final String name) {
    return this.dagBuilder.createNode(name, this.nodeProcessor);
  }
}

