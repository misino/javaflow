package no.havard.javaflow.ast;

import static java.lang.String.format;

import no.havard.javaflow.model.CanonicalName;

public class Type {

  protected final CanonicalName name;

  public Type(CanonicalName name) {
    this.name = name;
  }

  public String getName() {
    return name.getName();
  }

  public String getFullName() {
    return name.getCanonicalName();
  }

  public CanonicalName getCanonicalName() {
    return name;
  }

  public String getPackageName() {
    return name.getPackageName();
  }

  @Override
  public String toString() {
    return format("Type[%s]", name);
  }
}
