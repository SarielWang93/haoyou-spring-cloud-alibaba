// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: LRequest.proto

package com.haoyou.spring.cloud.alibaba.commons.protobuf.proto;

public final class MyRequestMsg {
  private MyRequestMsg() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface MyRequestOrBuilder extends
      // @@protoc_insertion_point(interface_extends:MyRequest)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>int32 id = 1;</code>
     */
    int getId();

    /**
     * <code>string useruid = 2;</code>
     */
    java.lang.String getUseruid();
    /**
     * <code>string useruid = 2;</code>
     */
    com.google.protobuf.ByteString
        getUseruidBytes();

    /**
     * <code>bytes msg = 3;</code>
     */
    com.google.protobuf.ByteString getMsg();
  }
  /**
   * Protobuf type {@code MyRequest}
   */
  public  static final class MyRequest extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:MyRequest)
      MyRequestOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use MyRequest.newBuilder() to construct.
    private MyRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private MyRequest() {
      useruid_ = "";
      msg_ = com.google.protobuf.ByteString.EMPTY;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private MyRequest(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 8: {

              id_ = input.readInt32();
              break;
            }
            case 18: {
              java.lang.String s = input.readStringRequireUtf8();

              useruid_ = s;
              break;
            }
            case 26: {

              msg_ = input.readBytes();
              break;
            }
            default: {
              if (!parseUnknownField(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.internal_static_MyRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.internal_static_MyRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest.class, com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest.Builder.class);
    }

    public static final int ID_FIELD_NUMBER = 1;
    private int id_;
    /**
     * <code>int32 id = 1;</code>
     */
    public int getId() {
      return id_;
    }

    public static final int USERUID_FIELD_NUMBER = 2;
    private volatile java.lang.Object useruid_;
    /**
     * <code>string useruid = 2;</code>
     */
    public java.lang.String getUseruid() {
      java.lang.Object ref = useruid_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        useruid_ = s;
        return s;
      }
    }
    /**
     * <code>string useruid = 2;</code>
     */
    public com.google.protobuf.ByteString
        getUseruidBytes() {
      java.lang.Object ref = useruid_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        useruid_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int MSG_FIELD_NUMBER = 3;
    private com.google.protobuf.ByteString msg_;
    /**
     * <code>bytes msg = 3;</code>
     */
    public com.google.protobuf.ByteString getMsg() {
      return msg_;
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (id_ != 0) {
        output.writeInt32(1, id_);
      }
      if (!getUseruidBytes().isEmpty()) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 2, useruid_);
      }
      if (!msg_.isEmpty()) {
        output.writeBytes(3, msg_);
      }
      unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (id_ != 0) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(1, id_);
      }
      if (!getUseruidBytes().isEmpty()) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, useruid_);
      }
      if (!msg_.isEmpty()) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(3, msg_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest)) {
        return super.equals(obj);
      }
      com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest other = (com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest) obj;

      if (getId()
          != other.getId()) return false;
      if (!getUseruid()
          .equals(other.getUseruid())) return false;
      if (!getMsg()
          .equals(other.getMsg())) return false;
      if (!unknownFields.equals(other.unknownFields)) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + ID_FIELD_NUMBER;
      hash = (53 * hash) + getId();
      hash = (37 * hash) + USERUID_FIELD_NUMBER;
      hash = (53 * hash) + getUseruid().hashCode();
      hash = (37 * hash) + MSG_FIELD_NUMBER;
      hash = (53 * hash) + getMsg().hashCode();
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code MyRequest}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:MyRequest)
        com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequestOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.internal_static_MyRequest_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.internal_static_MyRequest_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest.class, com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest.Builder.class);
      }

      // Construct using com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        id_ = 0;

        useruid_ = "";

        msg_ = com.google.protobuf.ByteString.EMPTY;

        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.internal_static_MyRequest_descriptor;
      }

      @java.lang.Override
      public com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest getDefaultInstanceForType() {
        return com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest.getDefaultInstance();
      }

      @java.lang.Override
      public com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest build() {
        com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest buildPartial() {
        com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest result = new com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest(this);
        result.id_ = id_;
        result.useruid_ = useruid_;
        result.msg_ = msg_;
        onBuilt();
        return result;
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest) {
          return mergeFrom((com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest other) {
        if (other == com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest.getDefaultInstance()) return this;
        if (other.getId() != 0) {
          setId(other.getId());
        }
        if (!other.getUseruid().isEmpty()) {
          useruid_ = other.useruid_;
          onChanged();
        }
        if (other.getMsg() != com.google.protobuf.ByteString.EMPTY) {
          setMsg(other.getMsg());
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private int id_ ;
      /**
       * <code>int32 id = 1;</code>
       */
      public int getId() {
        return id_;
      }
      /**
       * <code>int32 id = 1;</code>
       */
      public Builder setId(int value) {
        
        id_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>int32 id = 1;</code>
       */
      public Builder clearId() {
        
        id_ = 0;
        onChanged();
        return this;
      }

      private java.lang.Object useruid_ = "";
      /**
       * <code>string useruid = 2;</code>
       */
      public java.lang.String getUseruid() {
        java.lang.Object ref = useruid_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          useruid_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>string useruid = 2;</code>
       */
      public com.google.protobuf.ByteString
          getUseruidBytes() {
        java.lang.Object ref = useruid_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          useruid_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string useruid = 2;</code>
       */
      public Builder setUseruid(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        useruid_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>string useruid = 2;</code>
       */
      public Builder clearUseruid() {
        
        useruid_ = getDefaultInstance().getUseruid();
        onChanged();
        return this;
      }
      /**
       * <code>string useruid = 2;</code>
       */
      public Builder setUseruidBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
        
        useruid_ = value;
        onChanged();
        return this;
      }

      private com.google.protobuf.ByteString msg_ = com.google.protobuf.ByteString.EMPTY;
      /**
       * <code>bytes msg = 3;</code>
       */
      public com.google.protobuf.ByteString getMsg() {
        return msg_;
      }
      /**
       * <code>bytes msg = 3;</code>
       */
      public Builder setMsg(com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        msg_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>bytes msg = 3;</code>
       */
      public Builder clearMsg() {
        
        msg_ = getDefaultInstance().getMsg();
        onChanged();
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:MyRequest)
    }

    // @@protoc_insertion_point(class_scope:MyRequest)
    private static final com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest();
    }

    public static com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<MyRequest>
        PARSER = new com.google.protobuf.AbstractParser<MyRequest>() {
      @java.lang.Override
      public MyRequest parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new MyRequest(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<MyRequest> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<MyRequest> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public com.haoyou.spring.cloud.alibaba.commons.protobuf.proto.MyRequestMsg.MyRequest getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_MyRequest_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MyRequest_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\016LRequest.proto\"5\n\tMyRequest\022\n\n\002id\030\001 \001(" +
      "\005\022\017\n\007useruid\030\002 \001(\t\022\013\n\003msg\030\003 \001(\014BF\n6com.h" +
      "aoyou.spring.cloud.alibaba.commons.proto" +
      "buf.protoB\014MyRequestMsgb\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_MyRequest_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_MyRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_MyRequest_descriptor,
        new java.lang.String[] { "Id", "Useruid", "Msg", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
