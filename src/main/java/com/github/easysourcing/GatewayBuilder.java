package com.github.easysourcing;

import com.github.easysourcing.messages.Message;
import com.github.easysourcing.messages.MessageGateway;
import com.github.easysourcing.messages.commands.CommandGateway;
import com.github.easysourcing.messages.events.EventGateway;
import com.github.easysourcing.messages.snapshots.SnapshotGateway;
import com.github.easysourcing.support.serializer.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;

@Slf4j
public class GatewayBuilder {

  private Config config;

  public GatewayBuilder() {
  }

  public GatewayBuilder withConfig(Config config) {
    this.config = config;
    return this;
  }

  public MessageGateway messageGateway() {
    if (this.config == null) {
      throw new IllegalStateException("No config provided!");
    } else {
      return new MessageGateway(kafkaProducer());
    }
  }

  public CommandGateway commandGateway() {
    if (this.config == null) {
      throw new IllegalStateException("No config provided!");
    }
    return new CommandGateway(kafkaProducer());
  }

  public EventGateway eventGateway() {
    if (this.config == null) {
      throw new IllegalStateException("No config provided!");
    }
    return new EventGateway(kafkaProducer());
  }

  public SnapshotGateway snapshotGateway() {
    if (this.config == null) {
      throw new IllegalStateException("No config provided!");
    }
    return new SnapshotGateway(kafkaProducer());
  }

  private KafkaProducer<String, Message> kafkaProducer() {
    return new KafkaProducer<>(config.producerConfigs(),
        new StringSerializer(),
        new JsonSerializer<>());
  }

}
