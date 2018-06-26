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

package azkaban.execapp;

import azkaban.dag.Dag;
import azkaban.dag.DagBuilder;
import azkaban.dag.DagProcessor;
import azkaban.dag.DagService;
import azkaban.dag.Node;
import azkaban.dag.NodeBuilder;
import azkaban.dag.NodeProcessor;
import azkaban.dag.Status;
import azkaban.project.NodeBean;
import azkaban.project.NodeBeanLoader;
import azkaban.utils.ExecutorServiceUtils;
import java.io.File;
import org.junit.Test;

/**
 * Tests for running flows.
 */
public class FlowRunner2Test {

  @Test
  public void runSimpleV2Flow() throws Exception {
    final File flowFile = loadFlowFileFromResource();
    final NodeBeanLoader beanLoader = new NodeBeanLoader();
    final NodeBean nodeBean = beanLoader.load(flowFile);
    final Dag dag = createDag(nodeBean);
    final DagService dagService = new DagService(new ExecutorServiceUtils());
    dagService.startDag(dag);
    dagService.shutdownAndAwaitTermination();
  }

  private Dag createDag(final NodeBean nodeBean) {
    final String flowName = nodeBean.getName();
    final SimpleDagProcessor dagProcessor = new SimpleDagProcessor();
    final DagBuilder builder = new DagBuilder(flowName, dagProcessor);
    addNodes(builder, nodeBean);
    // todo: add node dependencies.
    return builder.build();

  }

  private void addNodes(final DagBuilder builder, final NodeBean nodeBean) {
    for (final NodeBean node : nodeBean.getNodes()) {
      addNode(builder, node);
    }
  }

  private void addNode(final DagBuilder builder, final NodeBean node) {
    final String nodeName = node.getName();
    final SimpleNodeProcessor nodeProcessor = new SimpleNodeProcessor();
    final NodeBuilder nodeBuilder = builder.createNode(nodeName, nodeProcessor);
  }

  private File loadFlowFileFromResource() {
    final ClassLoader loader = getClass().getClassLoader();
    return new File(loader.getResource("hello_world_flow.flow").getFile());
  }

  static class SimpleDagProcessor implements DagProcessor {

    @Override
    public void changeStatus(final Dag dag, final Status status) {
      System.out.println(dag + " status changed to " + status);
    }
  }

  static class SimpleNodeProcessor implements NodeProcessor {

    @Override
    public void changeStatus(final Node node, final Status status) {
      System.out.println(node + " status changed to " + status);
    }
  }


}
