package org.nerdpower.tabula.json;

import java.lang.reflect.Type;

import org.nerdpower.tabula.Ruling;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class RulingSerializer implements JsonSerializer<Ruling> {

    @Override
    public JsonElement serialize(Ruling arg0, Type arg1,
            JsonSerializationContext arg2) {

        JsonObject object = new JsonObject();
        object.addProperty("x1", arg0.getX1());
        object.addProperty("y1", arg0.getY1());
        object.addProperty("x2", arg0.getX2());
        object.addProperty("y2", arg0.getY2());
        object.addProperty("visible", arg0.isGraphic());
        
        return object;
    }

}
