package com.github.easysourcing.messages;

import com.github.easysourcing.messages.annotations.Priority;

import java.lang.reflect.Method;
import java.util.Optional;

public interface Handler<R> {

  R invoke(Object... args);

  Object getTarget();

  Method getMethod();

  Class<?> getType();

  default int getPriority() {
    return Optional.ofNullable(getMethod())
        .map(method -> method.getAnnotation(Priority.class))
        .map(Priority::value)
        .orElse(0);
  }
}
