/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.client.api;

import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.client.internal.DefaultImplementation;
import org.apache.pulsar.common.schema.KeyValue;
import org.apache.pulsar.common.schema.SchemaInfo;
import org.apache.pulsar.common.schema.SchemaType;

/**
 * Message schema definition
 */
public interface Schema<T> {

    /**
     * Check if the message is a valid object for this schema.
     *
     * <p>The implementation can choose what its most efficient approach to validate the schema.
     * If the implementation doesn't provide it, it will attempt to use {@link #decode(byte[])}
     * to see if this schema can decode this message or not as a validation mechanism to verify
     * the bytes.
     *
     * @param message the messages to verify
     * @return true if it is a valid message
     * @throws SchemaSerializationException if it is not a valid message
     */
    default void validate(byte[] message) {
        decode(message);
    }

    /**
     * Encode an object representing the message content into a byte array.
     *
     * @param message
     *            the message object
     * @return a byte array with the serialized content
     * @throws SchemaSerializationException
     *             if the serialization fails
     */
    byte[] encode(T message);

    /**
     * Decode a byte array into an object using the schema definition and deserializer implementation
     *
     * @param bytes
     *            the byte array to decode
     * @return the deserialized object
     */
    T decode(byte[] bytes);

    /**
     * @return an object that represents the Schema associated metadata
     */
    SchemaInfo getSchemaInfo();

    /**
     * Schema that doesn't perform any encoding on the message payloads. Accepts a byte array and it passes it through.
     */
    Schema<byte[]> BYTES = DefaultImplementation.newBytesSchema();

    /**
     * Schema that can be used to encode/decode messages whose values are String. The payload is encoded with UTF-8.
     */
    Schema<String> STRING = DefaultImplementation.newStringSchema();

    /**
     * Create a Protobuf schema type by extracting the fields of the specified class.
     *
     * @param clazz the Protobuf generated class to be used to extract the schema
     * @return a Schema instance
     */
    static <T extends com.google.protobuf.GeneratedMessageV3> Schema<T> PROTOBUF(Class<T> clazz) {
        return DefaultImplementation.newProtobufSchema(clazz);
    }

    /**
     * Create a Avro schema type by extracting the fields of the specified class.
     *
     * @param clazz the POJO class to be used to extract the Avro schema
     * @return a Schema instance
     */
    static <T> Schema<T> AVRO(Class<T> clazz) {
        return DefaultImplementation.newAvroSchema(clazz);
    }

    /**
     * Create a JSON schema type by extracting the fields of the specified class.
     *
     * @param clazz the POJO class to be used to extract the JSON schema
     * @return a Schema instance
     */
    static <T> Schema<T> JSON(Class<T> clazz) {
        return DefaultImplementation.newJSONSchema(clazz);
    }

    /**
     * Key Value Schema using passed in schema type, support JSON and AVRO currently.
     */
    static <K, V> Schema<KeyValue<K, V>> KeyValue(Class<K> key, Class<V> value, SchemaType type) {
        return DefaultImplementation.newKeyValueSchema(key, value, type);
    }

    /**
     * Schema that can be used to encode/decode KeyValue.
     */
    static Schema<KeyValue<byte[], byte[]>> KV_BYTES() {
        return DefaultImplementation.newKeyValueBytesSchema();
    }

    /**
     * Key Value Schema whose underneath key and value schemas are JSONSchema.
     */
    static <K, V> Schema<KeyValue<K, V>> KeyValue(Class<K> key, Class<V> value) {
        return DefaultImplementation.newKeyValueSchema(key, value, SchemaType.JSON);
    }

    /**
     * Key Value Schema using passed in key and value schemas.
     */
    static <K, V> Schema<KeyValue<K, V>> KeyValue(Schema<K> key, Schema<V> value) {
        return DefaultImplementation.newKeyValueSchema(key, value);
    }

    @Deprecated
    static Schema<GenericRecord> AUTO() {
        return AUTO_CONSUME();
    }

    /**
     * Create a schema instance that automatically deserialize messages
     * based on the current topic schema.
     * <p>
     * The messages values are deserialized into a {@link GenericRecord} object.
     * <p>
     * Currently this is only supported with Avro and JSON schema types.
     *
     * @return the auto schema instance
     */
    static Schema<GenericRecord> AUTO_CONSUME() {
        return DefaultImplementation.newAutoConsumeSchema();
    }

    /**
     * Create a schema instance that accepts a serialized payload
     * and validates it against the topic schema.
     * <p>
     * Currently this is only supported with Avro and JSON schema types.
     * <p>
     * This method can be used when publishing a raw JSON payload,
     * for which the format is known and a POJO class is not avaialable.
     *
     * @return the auto schema instance
     */
    static Schema<byte[]> AUTO_PRODUCE_BYTES() {
        return DefaultImplementation.newAutoProduceSchema();
    }

    static Schema<?> getSchema(SchemaInfo schemaInfo) {
        return DefaultImplementation.getSchema(schemaInfo);
    }
}
