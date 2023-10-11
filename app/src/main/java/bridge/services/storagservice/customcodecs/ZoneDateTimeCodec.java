package bridge.services.storagservice.customcodecs;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZoneDateTimeCodec implements Codec<ZonedDateTime>
{
  @Override
  public void encode(BsonWriter writer, ZonedDateTime value, EncoderContext encoderContext) {
    writer.writeDateTime(value.toInstant().toEpochMilli());
  }

  @Override
  public ZonedDateTime decode(BsonReader reader, DecoderContext decoderContext) {
    if (reader.getCurrentBsonType() == BsonType.NULL) {
      reader.readNull();
      return null;
    }
    return ZonedDateTime.ofInstant(Instant.ofEpochSecond(reader.readDateTime()), ZoneId.of("Etc/Zulu"));
  }

  @Override
  public Class<ZonedDateTime> getEncoderClass() {
    return ZonedDateTime.class;
  }
}
