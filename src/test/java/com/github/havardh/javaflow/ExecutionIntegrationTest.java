package com.github.havardh.javaflow;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import static com.github.havardh.javaflow.testutil.Assertions.assertStringEqual;
import static com.github.havardh.javaflow.util.TypeMap.emptyTypeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.havardh.javaflow.exceptions.AggregatedException;
import com.github.havardh.javaflow.exceptions.MissingTypeException;
import com.github.havardh.javaflow.phases.parser.java.JavaParser;
import com.github.havardh.javaflow.phases.reader.FileReader;
import com.github.havardh.javaflow.phases.transform.InheritanceTransformer;
import com.github.havardh.javaflow.phases.transform.SortedTypeTransformer;
import com.github.havardh.javaflow.phases.verifier.ClassGetterNamingVerifier;
import com.github.havardh.javaflow.phases.verifier.MemberFieldsPresentVerifier;
import com.github.havardh.javaflow.phases.writer.flow.FlowWriter;
import com.github.havardh.javaflow.phases.writer.flow.converter.JavaFlowConverter;

public class ExecutionIntegrationTest {

  private static final String BASE_PATH = "src/test/java/com/github/havardh/javaflow/model/";

  private Execution execution;

  @BeforeEach
  public void setup() {
    this.execution = new Execution(
        new FileReader(),
        new JavaParser(),
        asList(
            new InheritanceTransformer(),
            new SortedTypeTransformer()
        ),
        asList(new MemberFieldsPresentVerifier(emptyTypeMap()), new ClassGetterNamingVerifier()),
        new FlowWriter(new JavaFlowConverter()),
        emptyList()
    );
  }

  @Test
  public void shouldParseModel() {
    String flowCode = execution.run(BASE_PATH + "Model.java");

    assertStringEqual(flowCode,
        "export type Model = {",
        "  yolo: string,",
        "};"
    );
  }

  @Test
  public void shouldParseEnum() {
    String flowCode = execution.run(BASE_PATH + "Enumeration.java");

    assertStringEqual(flowCode,
        "export type Enumeration =",
        "  | \"ONE\"",
        "  | \"TWO\";"
    );
  }

  @Test
  public void shouldParseModelWithList() {
    String flowCode = execution.run(BASE_PATH + "ModelWithList.java");

    assertStringEqual(flowCode,
        "export type ModelWithList = {",
        "  words: Array<string>,",
        "};");
  }

  @Test
  public void shouldParseModelWithMap() {
    String flowCode = execution.run(BASE_PATH + "ModelWithMap.java");

    assertStringEqual(flowCode,
        "export type ModelWithMap = {",
        "  field: {[key: string]: number},",
        "};");
  }

  @Test
  public void shouldParseModelWithCollection() {
    String flowCode = execution.run(BASE_PATH + "ModelWithCollection.java");

    assertStringEqual(flowCode,
        "export type ModelWithCollection = {",
        "  strings: Array<string>,",
        "};");
  }

  @Test
  public void shouldParseModelWithSet() {
    String flowCode = execution.run(BASE_PATH + "ModelWithSet.java");

    assertStringEqual(flowCode,
        "export type ModelWithSet = {",
        "  strings: Array<string>,",
        "};");
  }

  @Test
  public void shouldParseModelWithMember() {
    String flowCode = execution.run(
        BASE_PATH + "Wrapper.java",
        BASE_PATH + "Member.java",
        BASE_PATH + "packaged/PackagedMember.java"
    );

    assertStringEqual(flowCode,
        "export type Member = {};",
        "",
        "export type PackagedMember = {};",
        "",
        "export type Wrapper = {",
        "  member: Member,",
        "  packagedMember: PackagedMember,",
        "};"
    );
  }

  @Test
  public void shouldThrowExceptionWhenFieldIsNotFound() {
    try {
      execution.run(BASE_PATH + "Wrapper.java");
      fail("Should fail when member types are not found");
    } catch (AggregatedException e) {
      MissingTypeException missingTypeException = (MissingTypeException) e.getExceptions().get(0);
      assertThat(missingTypeException.getTypes().entrySet(), hasSize(1));
    }
  }

  @Test
  public void shouldThrowExceptionWhenGetterDoesNotHaveMatchingField() {
    assertThrows(
        AggregatedException.class,
        () -> execution.run(BASE_PATH + "ModelWithNotMatchingGetter.java"),
        "Model com.github.havardh.javaflow.model.ModelWithNotMatchingGetter is not a pure DTO. Name of getter " +
            "'getStringFields' does not correspond to any field name."
    );
  }

  @Test
  public void shouldThrowExceptionWhenBooleanGetterDoesNotHaveMatchingField() {
    assertThrows(
        AggregatedException.class,
        () -> execution.run(BASE_PATH + "ModelWithNotMatchingBooleanGetter.java"),
        "Model com.github.havardh.javaflow.model.ModelWithNotMatchingBooleanGetter is not a pure DTO. Name of getter " +
            "'isBooleanFields' does not correspond to any field name."
    );
  }

  @Test
  public void shouldThrowExceptionWhenDifferentNumberOfGettersAndFields() {
    assertThrows(
        AggregatedException.class,
        () -> execution.run(BASE_PATH + "ModelWithoutGetters.java"),
        "Model com.github.havardh.javaflow.model.ModelWithoutGetters is not a pure DTO. Number of getters and fields is not same."
    );
  }
}
