package c.j.framework.http;

import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import com.alibaba.fastjson.JSON;

public class Jackson {

    public static <T> Unmarshaller<HttpEntity, T> unmarshaller(Class<T> var0) {
        return unmarshallerF(var0);
    }

    public static <T> Unmarshaller<HttpEntity, T> unmarshallerF(Class<T> var1) {
        return Unmarshaller.forMediaType(MediaTypes.APPLICATION_JSON, Unmarshaller.entityToString()).thenApply((var2) ->
            fromJSON(var2, var1)
        );
    }

    private static <T> T fromJSON(String var1, Class<T> var2) {
        return JSON.parseObject(var1, var2);
    }

    public static <T> Marshaller<T, RequestEntity> marshaller() {
        return marshallerF();
    }

    public static <T> Marshaller<T, RequestEntity> marshallerF() {
        return Marshaller.wrapEntity((var1) -> {
            return toJSON(var1);
        }, Marshaller.stringToEntity(), MediaTypes.APPLICATION_JSON);
    }

    private static String toJSON(Object var1) {
        return JSON.toJSONString(var1);
    }
}
