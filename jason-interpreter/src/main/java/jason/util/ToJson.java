package jason.util;

import java.io.StringWriter;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

public interface ToJson {

    public default String getAsJsonStr() {
        var config = new HashMap<String, Boolean>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);

        var jwf = Json.createWriterFactory(config);
        var sw = new StringWriter();

        try (var jsonWriter = jwf.createWriter(sw)) {
            jsonWriter.write(getAsJson());
            return sw.toString();
        }
    }

    public JsonValue getAsJson();
}
