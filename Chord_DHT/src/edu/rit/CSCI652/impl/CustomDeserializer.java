package edu.rit.CSCI652.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by payalkothari on 10/20/17.
 */
public class CustomDeserializer extends StdDeserializer<List<FingerTableEntry>> {
        public CustomDeserializer() {
            this(null);
        }

        public CustomDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public List<FingerTableEntry> deserialize(
             JsonParser jsonparser,
             DeserializationContext context)
         throws IOException, JsonProcessingException {

            return new ArrayList<>();
        }
}