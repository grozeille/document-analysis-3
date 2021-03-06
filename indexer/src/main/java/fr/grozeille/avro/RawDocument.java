/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package fr.grozeille.avro;

import org.apache.avro.specific.SpecificData;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class RawDocument extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 8388541285375950703L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Document\",\"namespace\":\"org.grozeille.avro\",\"fields\":[{\"name\":\"path\",\"type\":\"string\"},{\"name\":\"lang\",\"type\":\"string\"},{\"name\":\"body\",\"type\":{\"type\":\"bytes\",\"java-class\":\"[B\"}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<RawDocument> ENCODER =
      new BinaryMessageEncoder<RawDocument>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<RawDocument> DECODER =
      new BinaryMessageDecoder<RawDocument>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   */
  public static BinaryMessageDecoder<RawDocument> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   */
  public static BinaryMessageDecoder<RawDocument> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<RawDocument>(MODEL$, SCHEMA$, resolver);
  }

  /** Serializes this Document to a ByteBuffer. */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /** Deserializes a Document from a ByteBuffer. */
  public static RawDocument fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  @Deprecated public java.lang.CharSequence path;
  @Deprecated public java.lang.CharSequence lang;
  @Deprecated public java.nio.ByteBuffer body;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public RawDocument() {}

  /**
   * All-args constructor.
   * @param path The new value for path
   * @param lang The new value for lang
   * @param body The new value for body
   */
  public RawDocument(java.lang.CharSequence path, java.lang.CharSequence lang, java.nio.ByteBuffer body) {
    this.path = path;
    this.lang = lang;
    this.body = body;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return path;
    case 1: return lang;
    case 2: return body;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: path = (java.lang.CharSequence)value$; break;
    case 1: lang = (java.lang.CharSequence)value$; break;
    case 2: body = (java.nio.ByteBuffer)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'path' field.
   * @return The value of the 'path' field.
   */
  public java.lang.CharSequence getPath() {
    return path;
  }

  /**
   * Sets the value of the 'path' field.
   * @param value the value to set.
   */
  public void setPath(java.lang.CharSequence value) {
    this.path = value;
  }

  /**
   * Gets the value of the 'lang' field.
   * @return The value of the 'lang' field.
   */
  public java.lang.CharSequence getLang() {
    return lang;
  }

  /**
   * Sets the value of the 'lang' field.
   * @param value the value to set.
   */
  public void setLang(java.lang.CharSequence value) {
    this.lang = value;
  }

  /**
   * Gets the value of the 'body' field.
   * @return The value of the 'body' field.
   */
  public java.nio.ByteBuffer getBody() {
    return body;
  }

  /**
   * Sets the value of the 'body' field.
   * @param value the value to set.
   */
  public void setBody(java.nio.ByteBuffer value) {
    this.body = value;
  }

  /**
   * Creates a new Document RecordBuilder.
   * @return A new Document RecordBuilder
   */
  public static RawDocument.Builder newBuilder() {
    return new RawDocument.Builder();
  }

  /**
   * Creates a new Document RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new Document RecordBuilder
   */
  public static RawDocument.Builder newBuilder(RawDocument.Builder other) {
    return new RawDocument.Builder(other);
  }

  /**
   * Creates a new Document RecordBuilder by copying an existing Document instance.
   * @param other The existing instance to copy.
   * @return A new Document RecordBuilder
   */
  public static RawDocument.Builder newBuilder(RawDocument other) {
    return new RawDocument.Builder(other);
  }

  /**
   * RecordBuilder for Document instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<RawDocument>
    implements org.apache.avro.data.RecordBuilder<RawDocument> {

    private java.lang.CharSequence path;
    private java.lang.CharSequence lang;
    private java.nio.ByteBuffer body;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(RawDocument.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.path)) {
        this.path = data().deepCopy(fields()[0].schema(), other.path);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.lang)) {
        this.lang = data().deepCopy(fields()[1].schema(), other.lang);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.body)) {
        this.body = data().deepCopy(fields()[2].schema(), other.body);
        fieldSetFlags()[2] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing Document instance
     * @param other The existing instance to copy.
     */
    private Builder(RawDocument other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.path)) {
        this.path = data().deepCopy(fields()[0].schema(), other.path);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.lang)) {
        this.lang = data().deepCopy(fields()[1].schema(), other.lang);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.body)) {
        this.body = data().deepCopy(fields()[2].schema(), other.body);
        fieldSetFlags()[2] = true;
      }
    }

    /**
      * Gets the value of the 'path' field.
      * @return The value.
      */
    public java.lang.CharSequence getPath() {
      return path;
    }

    /**
      * Sets the value of the 'path' field.
      * @param value The value of 'path'.
      * @return This builder.
      */
    public RawDocument.Builder setPath(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.path = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'path' field has been set.
      * @return True if the 'path' field has been set, false otherwise.
      */
    public boolean hasPath() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'path' field.
      * @return This builder.
      */
    public RawDocument.Builder clearPath() {
      path = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'lang' field.
      * @return The value.
      */
    public java.lang.CharSequence getLang() {
      return lang;
    }

    /**
      * Sets the value of the 'lang' field.
      * @param value The value of 'lang'.
      * @return This builder.
      */
    public RawDocument.Builder setLang(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.lang = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'lang' field has been set.
      * @return True if the 'lang' field has been set, false otherwise.
      */
    public boolean hasLang() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'lang' field.
      * @return This builder.
      */
    public RawDocument.Builder clearLang() {
      lang = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'body' field.
      * @return The value.
      */
    public java.nio.ByteBuffer getBody() {
      return body;
    }

    /**
      * Sets the value of the 'body' field.
      * @param value The value of 'body'.
      * @return This builder.
      */
    public RawDocument.Builder setBody(java.nio.ByteBuffer value) {
      validate(fields()[2], value);
      this.body = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'body' field has been set.
      * @return True if the 'body' field has been set, false otherwise.
      */
    public boolean hasBody() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'body' field.
      * @return This builder.
      */
    public RawDocument.Builder clearBody() {
      body = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RawDocument build() {
      try {
        RawDocument record = new RawDocument();
        record.path = fieldSetFlags()[0] ? this.path : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.lang = fieldSetFlags()[1] ? this.lang : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.body = fieldSetFlags()[2] ? this.body : (java.nio.ByteBuffer) defaultValue(fields()[2]);
        return record;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<RawDocument>
    WRITER$ = (org.apache.avro.io.DatumWriter<RawDocument>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<RawDocument>
    READER$ = (org.apache.avro.io.DatumReader<RawDocument>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}
