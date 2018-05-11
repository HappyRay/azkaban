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
import java.util.List;
import java.util.Map;

/**
 * A builder to build DAGs.
 *
 * <p>Usage:
 *
 * <p>Use the createNode method to create NodeBuilder instances. Call methods on NodeBuilder to
 * connect them. Call build method to build a DAG.
 *
 * <p>Calling build multiple times will result in multiple independent DAGs be returned.
 */
public class DagBuilder {

  private final String name;
  private final DagProcessor dagProcessor;
  private final List<NodeBuilder> builders = new ArrayList<>();

  public DagBuilder(final String name, final DagProcessor dagProcessor) {
    this.name = name;
    this.dagProcessor = dagProcessor;
  }

  public NodeBuilder createNode(final String name, final NodeProcessor nodeProcessor) {
    final NodeBuilder builder = new NodeBuilder(name, nodeProcessor);
    this.builders.add(builder);
    return builder;
  }

  public Dag build() {
    final Dag dag = new Dag(this.name, this.dagProcessor);
    final Map<NodeBuilder, Node> builderNodeMap = createBuilderToNodeMap(dag);
    updateNodesRelationships(builderNodeMap);
    return dag;
  }

  private Map<NodeBuilder, Node> createBuilderToNodeMap(final Dag dag) {
    final Map<NodeBuilder, Node> builderNodeMap = new HashMap<>();
    for (final NodeBuilder builder : this.builders) {
      final Node node = builder.build();
      dag.addNode(node);
      builderNodeMap.put(builder, node);
    }
    return builderNodeMap;
  }

  private void updateNodesRelationships(final Map<NodeBuilder, Node> builderNodeMap) {
    for (final NodeBuilder builder : this.builders) {
      addChildrenNodes(builderNodeMap, builder);
    }
  }

  /**
   * Adds child nodes to the node associated with the builder.
   *
   * @throws DagException if a node builder that is not associated with this builder is detected
   */
  private void addChildrenNodes(final Map<NodeBuilder, Node> builderNodeMap,
      final NodeBuilder builder) {
    final Node node = builderNodeMap.get(builder);
    for (final NodeBuilder childBuilder : builder.getChildren()) {
      final Node childNode = builderNodeMap.get(childBuilder);
      if (childNode == null) {
        throw new DagException(
            String.format("Can't find the node builder (%s) in the Dag builder (%s).",
                childBuilder, this));
      }
      node.addChild(childNode);
    }
  }

  @Override
  public String toString() {
    return "DagBuilder{name='" + this.name + '\'' + '}';
  }
}
