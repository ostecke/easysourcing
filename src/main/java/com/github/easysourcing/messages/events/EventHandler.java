package com.github.easysourcing.messages.events;

import com.github.easysourcing.messages.Handler;
import com.github.easysourcing.messages.events.exceptions.EventProcessingException;
import com.github.easysourcing.retry.Retry;
import com.github.easysourcing.retry.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class EventHandler implements Handler<Void> {

  private final Object target;
  private final Method method;
  private final RetryPolicy<Object> retryPolicy;

  public EventHandler(Object target, Method method) {
    this.target = target;
    this.method = method;
    this.retryPolicy = RetryUtil.buildRetryPolicyFromAnnotation(method.getAnnotation(Retry.class))
        .onRetry(e -> log.warn("Handling event failed, retrying... ({})", e.getAttemptCount()))
        .onRetriesExceeded(e -> log.error("Handling event failed after {} attempts.", e.getAttemptCount()));
  }

  @Override
  public Void invoke(Object... args) {
    Event event = (Event) args[0];
    ProcessorContext context = (ProcessorContext) args[1];

    if (log.isDebugEnabled()) {
      log.debug("Handling event: {}", event);
    } else if (log.isInfoEnabled()) {
      log.info("Handling event: {} ({})", event.getType(), event.getAggregateId());
    }

    try {
      return Failsafe.with(retryPolicy).get(() -> doInvoke(event, context));
    } catch (Exception e) {
      throw new EventProcessingException(ExceptionUtils.getRootCauseMessage(e), ExceptionUtils.getRootCause(e));
    }
  }

  private Void doInvoke(Event event, ProcessorContext context) throws InvocationTargetException, IllegalAccessException {
    Object result;
    if (method.getParameterCount() == 1) {
      result = method.invoke(target, event.getPayload());
    } else {
      result = method.invoke(target, event.getPayload(), event.getMetadata().inject(context));
    }
    return null;
  }

  @Override
  public Object getTarget() {
    return target;
  }

  @Override
  public Method getMethod() {
    return method;
  }

  @Override
  public Class<?> getType() {
    return method.getParameters()[0].getType();
  }

}
