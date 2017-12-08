package com.github.havardh.javaflow.phases.verifier;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import com.github.havardh.javaflow.ast.Class;
import com.github.havardh.javaflow.ast.Field;
import com.github.havardh.javaflow.ast.Method;
import com.github.havardh.javaflow.ast.Type;
import com.github.havardh.javaflow.exceptions.AggregatedException;
import com.github.havardh.javaflow.exceptions.FieldGettersMismatchException;

public class ClassGetterNamingVerifier implements Verifier {

  @Override
  public void verify(List<Type> types) {
    List<Exception> exceptions = new ArrayList<>();
    for (Type type : types) {
      if (type instanceof Class) {
        try {
          validate((Class) type);
        } catch (Exception e) {
          exceptions.add(e);
        }
      }
    }

    if (!exceptions.isEmpty()) {
      throw new AggregatedException("Class getter naming validation failed", exceptions);
    }
  }

  private void validate(Class classToValidate) {
    List<Method> getters = classToValidate.getGetters();
    List<Field> fields = classToValidate.getFields();
    if (getters.size() != fields.size()) {
      throw new FieldGettersMismatchException(format(
          "Model %s is not a pure DTO. Number of getters and fields is not the same.\n" +
              "Fields in model: %s\n" +
              "Getters in model: %s",
          classToValidate.getFullName(),
          fields,
          getters
      ));
    }
    for (Method getter : getters) {
      fields.stream()
          .filter(field -> field.getName().equals(convertGetterNameToFieldName(getter.getName())))
          .findFirst()
          .orElseThrow(() -> new FieldGettersMismatchException(format(
              "Model %s is not a pure DTO. Name of getter %s does not correspond to any field name.",
              classToValidate.getFullName(), getter.getName()
          )));
    }
  }

  private static String convertGetterNameToFieldName(String getterName) {
    if (getterName.startsWith("get") && getterName.length() > 3) {
      return Character.toLowerCase(getterName.charAt(3)) + (getterName.length() > 4 ? getterName.substring(4) : "");
    }

    if (getterName.startsWith("is") && getterName.length() > 2) {
      return Character.toLowerCase(getterName.charAt(2)) + (getterName.length() > 4 ? getterName.substring(3) : "");
    }

    return getterName;
  }
}