package com.example.modfac.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bson.types.ObjectId;

import java.io.IOException;

public class ObjectIdSerializer extends StdSerializer<ObjectId> {
    /**
     * A custom serializer for the {@link ObjectId} class from the BSON library.
     * <p>
     * This serializer converts an {@link ObjectId} instance into its hexadecimal string representation
     * when serializing JSON objects. It is particularly useful when working with MongoDB ObjectIds
     * in a Java application that uses Jackson for JSON serialization.
     * </p>
     * <p>
     * Usage:
     * <pre>
     * {@code
     * ObjectMapper mapper = new ObjectMapper();
     * SimpleModule module = new SimpleModule();
     * module.addSerializer(ObjectId.class, new ObjectIdSerializer());
     * mapper.registerModule(module);
     * }
     * </pre>
     * </p>
     */
    public ObjectIdSerializer() {
        this(null);
    }

    public ObjectIdSerializer(Class<ObjectId> t) {
        super(t);
    }

    /**
     * Serializes an {@link ObjectId} instance into its hexadecimal string representation.
     * <p>
     * This method is invoked during the JSON serialization process to convert an {@link ObjectId}
     * into a format that can be represented as a JSON string.
     * </p>
     *
     * @param objectId the {@link ObjectId} instance to serialize.
     * @param gen the {@link JsonGenerator} used to write JSON content.
     * @param provider the {@link SerializerProvider} that can be used to get serializers for
     *                 serializing objects value contains, if any.
     * @throws IOException if an I/O error occurs during serialization.
     */
    @Override
    public void serialize(ObjectId objectId, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(objectId.toHexString());
    }
}

