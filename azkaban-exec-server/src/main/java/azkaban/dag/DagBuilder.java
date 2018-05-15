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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A builder to build DAGs.
 *
 * <p>Use the {@link DagBuilder#createNode} method to create NodeBuilder instances. Call
 * methods on NodeBuilder to add dependencies among them. Call the {@link DagBuilder#build()} method
 * to build a Dag.
 */
public class DagBuilder {

  private final String name;
  private final DagProcessor dagProcessor;

  private final List<NodeBuilder> builders = new ArrayList<>();
  private final Set<String> nodeNamesSet = new HashSet<>();

  public DagBuilder(final String name, final DagProcessor dagProcessor) {
    this.name = name;
    this.dagProcessor = dagProcessor;
  }

  public NodeBuilder createNode(final String name, final NodeProcessor nodeProcessor) {
    final NodeBuilder builder = new NodeBuilder(name, nodeProcessor, this);
    this.builders.add(builder);
    if (this.nodeNamesSet.contains(name)) {
      throw new DagException(String.format("Node names in a DAG %s need to be unique. The name "
          + "(%s) already exists.", this, name));
    }
    this.nodeNamesSet.add(name);

    return builder;
  }

  /**
   * Builds the dag.
   *
   * <p>Once this method is called, subsequent calls via NodeBuilder to modify the nodes's
   * relationships in the dag will have no effect on the returned Dag object.
   * </p>
   *
   * @return the Dag reflecting the current state of the DagBuilder
   */
  public Dag build() {
    final Dag dag = new Dag(this.name, this.dagProcessor);
    final Map<NodeBuilder, Node> builderNodeMap = createBuilderToNodeMap(dag);
    updateNodesRelationships(builderNodeMap);
    // todo HappyRay: circular dependency detection.
    return dag;
  }

  /**
   * Creates nodes using information stored in the current list of builders.
   *
   * <p>New nodes are created here to ensure they don't change even if their corresponding
   * NodeBuilders are modified after the {@link DagBuilder#build()} is called.
   *
   * @param dag the dag to associate the nodes with
   * @return the map from NodeBuilder to Node
   */
  private Map<NodeBuilder, Node> createBuilderToNodeMap(final Dag dag) {
    final Map<NodeBuilder, Node> builderNodeMap = new HashMap<>();
    for (final NodeBuilder builder : this.builders) {
      final Node node = builder.build(dag);
      builderNodeMap.put(builder, node);
    }
    return builderNodeMap;
  }

  private void updateNodesRelationships(final Map<NodeBuilder, Node> builderNodeMap) {
    for (final NodeBuilder builder : this.builders) {
      addParentNodes(builder, builderNodeMap);
    }
  }

  /**
   * Adds parent nodes to the node associated with the builder.
   */
  private void addParentNodes(final NodeBuilder builder,
      final Map<NodeBuilder, Node> builderToNodeMap) {
    final Node node = builderToNodeMap.get(builder);
    for (final NodeBuilder parentBuilder : builder.getParents()) {
      final Node parentNode = builderToNodeMap.get(parentBuilder);

      // The NodeBuilders should have checked if the NodeBuilders belong to the same DagBuilder.
      assert (parentNode != null);
      node.addParent(parentNode);
    }
  }

  @Override
  public String toString() {
    return String.format("DagBuilder (%s)", this.name);
  }
}
