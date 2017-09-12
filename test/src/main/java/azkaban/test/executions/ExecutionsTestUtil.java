/*
 * Copyright 2017 LinkedIn Corp.
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

package azkaban.test.executions;

import static java.util.Objects.requireNonNull;

import com.google.common.io.Resources;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExecutionsTestUtil {

  private static final Path dataRootPath = getAzRootDir().resolve("test/execution-test-data/");

  public static String getDataRootPath() {
    System.out.println(dataRootPath);
    final Path resolved = getAzRootDir().resolve("test/execution-test-data/");
    System.out.println("resovled path" + resolved);
    System.out.println("normalized " + resolved.normalize());
    // Assume that the working directory of a test is always the sub-module directory.
    // It is the case when running gradle tests from the project root directory.
    return dataRootPath + "/";
  }

  private static Path getAzRootDir() {
    final URL resource = Resources.getResource("dummy.txt");
    final String resourceDir = requireNonNull(resource).getPath();
    System.out.println("resource dir: " + resourceDir);
    final Path resources = Paths.get(resourceDir).getParent();
    System.out.println("resource folder: " + resources);
    final Path azkabanRoot = resources.getParent().getParent().getParent().getParent();
    System.out.println("azkaban root:" + azkabanRoot);
    return azkabanRoot;
  }

  public static File getFlowDir(final String flowName) {
    return dataRootPath.resolve(flowName).toFile();
  }

  public static File getFlowFile(final String flowName, final String fileName) {
    return dataRootPath.resolve(flowName + "/" + fileName).toFile();
  }
}
