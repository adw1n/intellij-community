// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.execution.testDiscovery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.annotations.SerializedName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Couple;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.io.HttpRequests;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IntellijTestDiscoveryProducer implements TestDiscoveryProducer {
  private static final String INTELLIJ_TEST_DISCOVERY_HOST = "http://intellij-test-discovery.labs.intellij.net";

  @NotNull
  @Override
  public MultiMap<String, String> getDiscoveredTests(@NotNull Project project,
                                                     @NotNull List<Couple<String>> classesAndMethods,
                                                     byte frameworkId) {
    if (!ApplicationManager.getApplication().isInternal()) {
      return MultiMap.empty();
    }
    try {
      List<String> bareClasses = ContainerUtil.newSmartList();
      List<Couple<String>> allTogether = ContainerUtil.newSmartList();

      classesAndMethods.forEach(couple -> {
        if (couple.second == null) bareClasses.add(couple.first);
        else allTogether.add(couple);
      });

      MultiMap<String, String> result = new MultiMap<>();
      result.putAllValues(request(allTogether, couple -> "\"" + couple.first + "." + couple.second + "\"", "methods"));
      result.putAllValues(request(bareClasses, s -> "\"" + s + "\"", "classes"));
      return result;
    }
    catch (HttpRequests.HttpStatusException http) {
      LOG.debug("No tests found", http);
    }
    catch (IOException e) {
      LOG.debug(e);
    }
    return MultiMap.empty();
  }

  @NotNull
  private static <T> MultiMap<String, String> request(List<T> collection, Function<T, String> toString, String what) throws IOException {
    if (collection.isEmpty()) return MultiMap.empty();
    String url = INTELLIJ_TEST_DISCOVERY_HOST + "/search/tests/by-" + what;
    LOG.debug(url);
    return HttpRequests.post(url, "application/json").productNameAsUserAgent().gzip(true).connect(r -> {
      r.write(collection.stream().map(toString).collect(Collectors.joining(",", "[", "]")));
      TestsSearchResult search = new ObjectMapper().readValue(r.getInputStream(), TestsSearchResult.class);
      MultiMap<String, String> result = new MultiMap<>();
      search.getTests().forEach((classFqn, testMethodName) -> result.putValues(classFqn, testMethodName));
      return result;
    });
  }

  @Override
  public boolean isRemote() {
    return true;
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TestsSearchResult {
    @Nullable
    private String method;

    @SerializedName("class")
    @JsonProperty("class")
    @Nullable
    private String className;

    private int found;

    @NotNull
    private Map<String, List<String>> tests = new HashMap<>();

    @Nullable
    private String message;

    @Nullable
    public String getMethod() {
      return method;
    }

    public TestsSearchResult setMethod(String method) {
      this.method = method;
      return this;
    }

    @Nullable
    public String getClassName() {
      return className;
    }

    public TestsSearchResult setClassName(String name) {
      this.className = name;
      return this;
    }

    public int getFound() {
      return found;
    }

    public TestsSearchResult setFound(int found) {
      this.found = found;
      return this;
    }

    @NotNull
    public Map<String, List<String>> getTests() {
      return tests;
    }

    public TestsSearchResult setTests(@NotNull Map<String, List<String>> tests) {
      this.tests = tests;
      return this;
    }

    @Nullable
    public String getMessage() {
      return message;
    }

    public TestsSearchResult setMessage(String message) {
      this.message = message;
      return this;
    }
  }
}
