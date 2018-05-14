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

import java.util.ArrayList;
import java.util.List;

/**
 * A builder to build DAGs.
 *
 * <p>Usage:
 *
 * <p>Use the createNode method to create NodeBuilder instances. Call methods on NodeBuilder to
 * add dependencies among them. Call build method to build a Dag.
 */
public class DagBuilder {

  private final String name;
  private final List<NodeBuilder> builders = new ArrayList<>();
  private final Dag dag;

  // True if the final dag has been finalized.
  private boolean isDagFinalized;

  public DagBuilder(final String name, final DagProcessor dagProcessor) {
    this.name = name;
    this.dag = new Dag(name, dagProcessor);
  }

  public NodeBuilder createNode(final String name, final NodeProcessor nodeProcessor) {
    final Node node = new Node(name, nodeProcessor, this.dag);
    this.dag.addNode(node);
    final NodeBuilder builder = new NodeBuilder(name, this, node);
    this.builders.add(builder);
    return builder;
  }

  /**
   * Builds the dag.
   *
   * <p>Once this method is called, subsequent calls via NodeBuilder to modify the nodes's
   * relationships in the dag will have no effect on the returned Dag object. Multiple calls to this
   * method will return the same Dag object.
   * </p>
   *
   * @return the finalized Dag
   */
  public Dag build() {
    if (!this.isDagFinalized) {
      updateNodesRelationships();
      this.isDagFinalized = true;
    }
    return this.dag;
  }

  private void updateNodesRelationships() {
    for (final NodeBuilder builder : this.builders) {
      addParentNodes(builder);
    }
  }

  /**
   * Adds parent nodes to the node associated with the builder.
   */
  private void addParentNodes(final NodeBuilder builder) {
    final Node node = builder.getNode();
    for (final NodeBuilder parentBuilder : builder.getParents()) {
      final Node parentNode = parentBuilder.getNode();

      // The NodeBuilders should have checked if the NodeBuilders belong to the same DagBuilder.
      node.addParent(parentNode);
    }
  }

  @Override
  public String toString() {
    return String.format("DagBuilder (%s)", this.name);
  }
}
