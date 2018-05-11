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

  // The nodes that depend on this node.
  private final List<NodeBuilder> children = new ArrayList<>();

  public NodeBuilder(final String name, final NodeProcessor nodeProcessor) {
    this.name = name;
    this.nodeProcessor = nodeProcessor;
  }

  private void addChild(final NodeBuilder builder) {
    this.children.add(builder);
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
    return "NodeBuilder {name='" + this.name + '\'' + '}';
  }
}
