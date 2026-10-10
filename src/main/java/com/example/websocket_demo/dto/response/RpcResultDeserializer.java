package com.example.websocket_demo.dto.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;

public class RpcResultDeserializer extends JsonDeserializer<RpcResult<?>> implements ContextualDeserializer {
    private JavaType type;

    public RpcResultDeserializer() {}

    public RpcResultDeserializer(JavaType type) {
        this.type = type;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        JavaType wrapperType = ctxt.getContextualType() != null ? ctxt.getContextualType() : property.getType();
        JavaType payloadType = wrapperType.containedType(0);
        return new RpcResultDeserializer(payloadType);
    }

    @Override
    public RpcResult<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        if (node.isArray()) {
            int status = !node.isEmpty() ? node.get(0).asInt() : 0;
            Object payload = node.size() > 1 ? mapper.readerFor(type).readValue(node.get(1)) : null;
            return new RpcResult<>(status, payload);
        } else {
            Object payload = mapper.readerFor(type).readValue(node);
            return new RpcResult<>(0, payload);
        }
    }
}
