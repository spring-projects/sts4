package org.springframework.ide.vscode.commons.languageserver.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

public class JSON {
	
    public static final ObjectMapper JSON = new ObjectMapper().registerModule(new Jdk8Module())
            .registerModule(new JSR310Module())
            .registerModule(pathAsJson())
            .configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

    private static SimpleModule pathAsJson() {
        SimpleModule m = new SimpleModule();

        m.addSerializer(Path.class, new JsonSerializer<Path>() {
            @Override
            public void serialize(Path path,
                                  JsonGenerator gen,
                                  SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
                gen.writeString(path.toString());
            }
        });

        m.addDeserializer(Path.class, new JsonDeserializer<Path>() {
            @Override
            public Path deserialize(JsonParser parse,
                                    DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
                return Paths.get(parse.getText());
            }
        });

        return m;
    }


    
}
