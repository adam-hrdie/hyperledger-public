package hm.util;

import java.lang.reflect.Type;
import java.util.Base64;

import com.google.gson.*;

public class EncodedBase64Deserialiser implements com.google.gson.JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

   @Override
   public byte[] deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
      return Base64.getDecoder().decode(json.getAsString());
   }

   @Override
   public JsonElement serialize(final byte[] src, final Type typeOfSrc, final JsonSerializationContext context) {
      return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
   }

}
