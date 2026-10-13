/*
 * Copyright 2021, 2026 the original author or authors.
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.DocumentExample;
import org.openrewrite.Issue;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.springframework.ide.vscode.commons.rewrite.java.ORAstUtils;

import static org.openrewrite.java.Assertions.java;

class ImplicitWebAnnotationNamesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ImplicitWebAnnotationNames())
          .parser(JavaParser.fromJavaVersion().classpath("spring-web", "spring-core", "jspecify", "spring-context"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "@ModelAttribute(\"form\") UserRegistrationForm form",
      "@ModelAttribute(value = \"form\") UserRegistrationForm form",
      "@ModelAttribute(name = \"form\") UserRegistrationForm form",
      "@ModelAttribute(name = \"form\", binding = false) UserRegistrationForm form",
      "@ModelAttribute(\"registration\") UserRegistrationForm form",
      "@ModelAttribute UserRegistrationForm form",
      "@ModelAttribute(binding = false) UserRegistrationForm form",
      "@ModelAttribute(\"fo\" + \"rm\") UserRegistrationForm form",
      "@ModelAttribute(\"userRegistrationForm\") UserRegistrationForm form",
      "@ModelAttribute(\"userRegistrationForm\") UserRegistrationForm userRegistrationForm",
      "@org.springframework.web.bind.annotation.ModelAttribute(\"form\") UserRegistrationForm form",
      "@org.springframework.web.bind.annotation.ModelAttribute(value = \"form\") UserRegistrationForm form",
      "@org.springframework.web.bind.annotation.ModelAttribute(name = \"form\") UserRegistrationForm form"
    })
    void preserveModelAttributeNamesDuringCleanup(String parameter) {
        // Include RequestParam so that the recipe's precondition permits visiting this file.
        rewriteRun(
          java(
            """
              import org.springframework.web.bind.annotation.*;

              class RegistrationController {
                  @PostMapping("/register")
                  public String register(%s, @RequestParam("id") String id) {
                      return "success";
                  }
              }

              class UserRegistrationForm {}
              """.formatted(parameter),
            """
              import org.springframework.web.bind.annotation.*;

              class RegistrationController {
                  @PostMapping("/register")
                  public String register(%s, @RequestParam String id) {
                      return "success";
                  }
              }

              class UserRegistrationForm {}
              """.formatted(parameter)
          )
        );
    }

    @Test
    void preserveModelAttributeNamesAcrossFiles() {
        rewriteRun(
          java(
            """
              import org.springframework.web.bind.annotation.*;

              class RegistrationController {
                  public String register(@ModelAttribute("form") UserRegistrationForm form) {
                      return "success";
                  }
              }

              class UserRegistrationForm {}
              """
          ),
          java(
            """
              import org.springframework.web.bind.annotation.*;

              class UsersController {
                  public void user(@RequestParam("id") String id,
                                   @PathVariable("userId") String userId,
                                   @RequestHeader(value = "header") String header,
                                   @RequestAttribute(name = "attribute") String attribute,
                                   @CookieValue("cookie") String cookie,
                                   @SessionAttribute("session") String session,
                                   @ModelAttribute(name = "form", binding = false) UserRegistrationForm form) {
                  }

                  public void search(@RequestParam("user_id") String userId) {
                  }
              }
              """,
            """
              import org.springframework.web.bind.annotation.*;

              class UsersController {
                  public void user(@RequestParam String id,
                                   @PathVariable String userId,
                                   @RequestHeader String header,
                                   @RequestAttribute String attribute,
                                   @CookieValue String cookie,
                                   @SessionAttribute String session,
                                   @ModelAttribute(name = "form", binding = false) UserRegistrationForm form) {
                  }

                  public void search(@RequestParam("user_id") String userId) {
                  }
              }
              """
          )
        );
    }

    @Test
    void removeOnlySelectedParameterName() {
        // The node-scoped wrapper holds a runtime predicate and is not serialized by Spring Tools.
        rewriteRun(
          spec -> spec.recipe(ORAstUtils.nodeRecipe(new ImplicitWebAnnotationNames(),
            node -> node instanceof J.Annotation annotation && "RequestParam".equals(annotation.getSimpleName())))
            .validateRecipeSerialization(false),
          java(
            """
              import org.springframework.web.bind.annotation.ModelAttribute;
              import org.springframework.web.bind.annotation.PathVariable;
              import org.springframework.web.bind.annotation.RequestParam;

              class RegistrationController {
                  public void register(@ModelAttribute("form") UserRegistrationForm form,
                                       @RequestParam("id") String id,
                                       @PathVariable("userId") String userId) {
                  }
              }

              class UserRegistrationForm {}
              """,
            """
              import org.springframework.web.bind.annotation.ModelAttribute;
              import org.springframework.web.bind.annotation.PathVariable;
              import org.springframework.web.bind.annotation.RequestParam;

              class RegistrationController {
                  public void register(@ModelAttribute("form") UserRegistrationForm form,
                                       @RequestParam String id,
                                       @PathVariable("userId") String userId) {
                  }
              }

              class UserRegistrationForm {}
              """
          )
        );
    }

    @DocumentExample
    @Test
    void removeUnnecessaryAnnotationArgument() {
        //language=java
        rewriteRun(
          java(
            """
              import org.springframework.http.ResponseEntity;
              import org.springframework.web.bind.annotation.*;
              
              @RestController
              @RequestMapping("/users")
              public class UsersController {
                  @GetMapping("/{id}")
                  public ResponseEntity<String> getUser(@PathVariable("id") Long id,
                                                        @PathVariable(required = false) Long p2,
                                                        @PathVariable(value = "p3") Long anotherName) {
                      System.out.println(anotherName);
                  }
              }
              """,
            """
              import org.springframework.http.ResponseEntity;
              import org.springframework.web.bind.annotation.*;
              
              @RestController
              @RequestMapping("/users")
              public class UsersController {
                  @GetMapping("/{id}")
                  public ResponseEntity<String> getUser(@PathVariable Long id,
                                                        @PathVariable(required = false) Long p2,
                                                        @PathVariable(value = "p3") Long anotherName) {
                      System.out.println(anotherName);
                  }
              }
              """
          )
        );
    }

    @Issue("#4")
    @Test
    void removeUnnecessarySpacingInFollowingAnnotationArgument() {
        //language=java
        rewriteRun(
          java(
            """
              import org.springframework.http.ResponseEntity;
              import org.springframework.web.bind.annotation.*;
              
              @RestController
              @RequestMapping("/users")
              public class UsersController {
                  @GetMapping("/{id}")
                  public ResponseEntity<String> getUser(
                          @RequestParam(name = "count", defaultValue = 3) int count) {
                  }
              }
              """,
            """
              import org.springframework.http.ResponseEntity;
              import org.springframework.web.bind.annotation.*;
              
              @RestController
              @RequestMapping("/users")
              public class UsersController {
                  @GetMapping("/{id}")
                  public ResponseEntity<String> getUser(
                          @RequestParam(defaultValue = 3) int count) {
                  }
              }
              """
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-spring/issues/340")
    @Test
    void autoFormatAfterRemovingArgument() {
        //language=java
        rewriteRun(
          java(
            """
              import org.springframework.http.ResponseEntity;
              import org.springframework.web.bind.annotation.*;
              
              @RestController
              @RequestMapping("/users")
              public class UsersController {
                  @GetMapping("/{id}")
                  public ResponseEntity<String> getUser(@PathVariable("id")Long id) {
                  }
              }
              """,
            """
              import org.springframework.http.ResponseEntity;
              import org.springframework.web.bind.annotation.*;
              
              @RestController
              @RequestMapping("/users")
              public class UsersController {
                  @GetMapping("/{id}")
                  public ResponseEntity<String> getUser(@PathVariable Long id) {
                  }
              }
              """
          )
        );
    }

    @Test
    void doNotRemoveModelAttributeOnMethods() {
        //language=java
        rewriteRun(
          java(
            """
              import org.springframework.web.bind.annotation.*;
              import java.util.*;
              
              public class UsersController {
                  @ModelAttribute("types")
                  public Collection<String> populateUserTypes() {
                      return Arrays.asList("free", "premium");
                  }
              }
              """
          )
        );
    }

    @Test
    void doesNotRenamePathVariable() {
        //language=java
        rewriteRun(
          java(
            """
              import org.springframework.http.ResponseEntity;
              import org.springframework.web.bind.annotation.*;
              
              public class UsersController {
                  public ResponseEntity<String> getUser(@PathVariable("uid") Long id,
                                                        @PathVariable(value = "another_name") Long anotherName) {
                  }
              }
              """
          )
        );
    }
}
