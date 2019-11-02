package com.github.easysourcing.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.easysourcing.message.annotations.AggregateId;
import lombok.Builder;
import lombok.Value;

import java.beans.Transient;
import java.lang.reflect.Field;

@Value
@Builder(toBuilder = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class Message<T> {

  private MessageType type;
  private String name;
  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
  private T payload;


  @Transient
  public String getAggregateId() {
    for (Field field : payload.getClass().getDeclaredFields()) {
      AggregateId annotation = field.getAnnotation(AggregateId.class);
      if (annotation != null) {
        field.setAccessible(true);
        try {
          Object value = field.get(payload);
          if (value instanceof String) {
            return (String) value;
          }
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

}
