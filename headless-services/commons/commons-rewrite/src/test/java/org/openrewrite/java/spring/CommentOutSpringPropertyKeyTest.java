/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.spring;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.properties.tree.Properties;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.yaml.tree.Yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

class CommentOutSpringPropertyKeyTest implements RewriteTest {

    @DocumentExample
    @Test
    void yamlComment() {
        rewriteRun(
          spec -> spec.recipe(new CommentOutSpringPropertyKey("server.port", "This property has been removed.")),
          //language=yaml
          yaml(
            "server.port: 8080",
            """
              # This property has been removed.
              # server.port: 8080
              """)
        );
    }

    @Test
    void multipleYamlComments() {
        rewriteRun(
          spec -> spec.recipes(
            new CommentOutSpringPropertyKey("test.propertyKey1", "my comment 1"),
            new CommentOutSpringPropertyKey("test.propertyKey2", "my comment 2")
          ),
          //language=yaml
          yaml(
            """
              test:
                propertyKey1: xxx
                propertyKey2: yyy
                propertyKey3: zzz
              """,
            """
              test:
                # my comment 2
                # my comment 1
                # propertyKey1: xxx
                # propertyKey2: yyy
                propertyKey3: zzz
              """,
            spec -> spec.path("application.yaml")
              .afterRecipe(file ->
                assertThat(
                  ((Yaml.Mapping)
                    ((Yaml.Mapping) file.getDocuments().get(0)
                      .getBlock()).getEntries().get(0)
                      .getValue()).getEntries().get(0)
                    .getPrefix())
                  .isEqualTo(
                    """

                        # my comment 2
                        # my comment 1
                        # propertyKey1: xxx
                        # propertyKey2: yyy
                        \
                      """
                  )
              )
          )
        );
    }

    @Test
    void multiplePropertiesComments() {
        rewriteRun(
          spec -> spec.recipes(
            new CommentOutSpringPropertyKey("test.propertyKey1", "my comment 1"),
            new CommentOutSpringPropertyKey("test.propertyKey2", "my comment 2")
          ),
          //language=properties
          properties(
            """
              test.propertyKey1=xxx
              test.propertyKey2=yyy
              test.propertyKey3=zzz
              """,
            """
              # my comment 1
              # test.propertyKey1=xxx
              # my comment 2
              # test.propertyKey2=yyy
              test.propertyKey3=zzz
              """,
            spec -> spec.path("application.properties")
              .afterRecipe(file ->
                assertThat(((Properties.Comment) file.getContent().get(3)).getMessage())
                  .isEqualTo(" test.propertyKey2=yyy"))
          )
        );
    }
}
