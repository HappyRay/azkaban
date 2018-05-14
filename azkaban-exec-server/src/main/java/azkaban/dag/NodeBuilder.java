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

public class NodeBuilder {

  private final String name;

  private final NodeProcessor nodeProcessor;

  private final DagBuilder dagBuilder;

  // The nodes that depend on this node.
  private final List<NodeBuilder> children = new ArrayList<>();

  public NodeBuilder(final String name, final NodeProcessor nodeProcessor,
      final DagBuilder dagBuilder) {
    this.name = name;
    this.nodeProcessor = nodeProcessor;
    this.dagBuilder = dagBuilder;
  }

  private void addChild(final NodeBuilder builder) {
    checkBuildersBelongToSameDag(builder);
    this.children.add(builder);
  }

  /**
   * Checks if the given NodeBuilder belongs to the same DagBuilder as the current NodeBuilder.
   */
  private void checkBuildersBelongToSameDag(final NodeBuilder builder) {
    if (builder.dagBuilder != this.dagBuilder) {
      throw new DagException(String.format("Can't add a dependency from %s to %s since they "
          + "belong to different DagBuilders.", builder, this));
    }
  }

  public void addChildren(final NodeBuilder... builders) {
    for (final NodeBuilder builder : builders) {
      addChild(builder);
    }
  }

  List<NodeBuilder> getChildren() {
    return this.children;
  }

  Node build() {
    return new Node(this.name, this.nodeProcessor);
  }

  @Override
  public String toString() {
    return String.format("NodeBuilder (%s) in %s", this.name, this.dagBuilder);
  }
}
