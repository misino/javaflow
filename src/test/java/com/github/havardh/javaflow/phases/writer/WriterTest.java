package com.github.havardh.javaflow.phases.writer;

import static java.lang.String.format;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.StringWriter;

public class WriterTest<T> {

  private final Writer<T> writer;

  public WriterTest(Writer<T> writer) {
    this.writer = writer;
  }

  protected String toFlow(T t) throws IOException {
    StringWriter stringWriter = new StringWriter();
    writer.write(t, stringWriter);
    return stringWriter.toString();
  }

}

