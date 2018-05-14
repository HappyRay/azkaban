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
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class NodeBuilderTest {

  private final DagBuilder dagBuilder = mock(DagBuilder.class);
  private final NodeBuilder builder = createBuilder("builder");

  @Test
  public void addChildren() {
    // given
    final NodeBuilder builder2 = createBuilder("builder2");
    final NodeBuilder builder3 = createBuilder("builder3");

    // when
    this.builder.addChildren(builder2, builder3);
    final List<NodeBuilder> children = this.builder.getChildren();

    // then
    assertThat(children).isEqualTo(Arrays.asList(builder2, builder3));
  }

  private NodeBuilder createBuilder(final String name) {
    return new NodeBuilder(name, mock(NodeProcessor.class), this.dagBuilder);
  }

  @Test
  public void depend_on_node_in_a_different_dag_should_throw_exception() {
    // given
    final NodeBuilder builderInAnotherDag = new NodeBuilder("builder from another dag", mock
        (NodeProcessor.class), mock(DagBuilder.class));

    // when
    final Throwable thrown = catchThrowable(() -> {
      this.builder.addChildren(builderInAnotherDag);
    });

    // then
    assertThat(thrown).isInstanceOf(DagException.class);
  }

  @Test
  public void build() {
    // given

    // when
    final Node node = this.builder.build();

    // then
    assertThat(node.getName()).isEqualTo("builder");
  }

  @Test
  public void toStringTest() {
    // given

    // when
    final String stringRepresentation = this.builder.toString();

    // then
    assertThat(stringRepresentation).isEqualTo("NodeBuilder {name='builder'}");
  }
}
