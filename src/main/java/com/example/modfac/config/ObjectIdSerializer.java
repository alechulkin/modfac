package com.example.modfac.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bson.types.ObjectId;

import java.io.IOException;

public class ObjectIdSerializer extends StdSerializer<ObjectId> {
    public ObjectIdSerializer() {
        this(null);
    }

    public ObjectIdSerializer(Class<ObjectId> t) {
        super(t);
    }

    @Override
    public void serialize(ObjectId objectId, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(objectId.toHexString());
    }
}

