package T3;

import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.EOFException;
import java.lang.UnsupportedOperationException;
import java.util.Base64.Decoder;
import java.util.Base64;
import com.google.gson.stream.JsonReader;
import com.google.gson.Gson;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;


final class Base64TypeAdapterFactory implements TypeAdapterFactory {

    // Gson can instantiate this one itself, no need to expose it
    private Base64TypeAdapterFactory() {
    }

    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> typeToken) {
        final TypeAdapter<String> stringTypeAdapter = gson.getAdapter(String.class);
        final TypeAdapter<T> dataTypeAdapter = gson.getAdapter(typeToken);
        return Base64TypeAdapter.of(stringTypeAdapter, dataTypeAdapter);
    }

    private static final class Base64TypeAdapter<T>
            extends TypeAdapter<T> {

        private static final Decoder base64Decoder = Base64.getDecoder();

        private final TypeAdapter<String> stringTypeAdapter;
        private final TypeAdapter<T> dataTypeAdapter;

        private Base64TypeAdapter(final TypeAdapter<String> stringTypeAdapter, final TypeAdapter<T> dataTypeAdapter) {
            this.stringTypeAdapter = stringTypeAdapter;
            this.dataTypeAdapter = dataTypeAdapter;
        }

        static <T> TypeAdapter<T> of(final TypeAdapter<String> stringTypeAdapter, final TypeAdapter<T> dataTypeAdapter) {
            return new Base64TypeAdapter<>(stringTypeAdapter, dataTypeAdapter)
                    .nullSafe(); // Just let Gson manage nulls itself. It's convenient
        }

        @Override
        public void write(final JsonWriter jsonWriter, final T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T read(final JsonReader jsonReader)
                throws IOException {
            // Decode the payload first as a Base64-encoded message
            final byte[] payload = base64Decoder.decode(stringTypeAdapter.read(jsonReader));
            try ( final JsonReader payloadJsonReader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(payload))) ) {
                // And tell Gson to not refuse unquoted property names
                payloadJsonReader.setLenient(true);
                return dataTypeAdapter.read(payloadJsonReader);
            } catch ( final EOFException ignored ) {
                return null;
            }
        }

    }

}
