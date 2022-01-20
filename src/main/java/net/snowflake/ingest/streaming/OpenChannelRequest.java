/*
 * Copyright (c) 2021 Snowflake Computing Inc. All rights reserved.
 */

package net.snowflake.ingest.streaming;

import net.snowflake.ingest.utils.Utils;

/** A class that is used to open/create a {@link SnowflakeStreamingIngestChannel} */
public class OpenChannelRequest {
  public enum OnErrorOption {
    CONTINUE, // CONTINUE loading the rows, and return all the errors in the response
    ABORT, // ABORT the entire batch, and throw an exception when we hit the first error
  }

  // Name of the channel
  private final String channelName;

  // Name of the database that the channel belongs to
  private final String dbName;

  // Name of the schema that the channel belongs to
  private final String schemaName;

  // Name of the table that the channel belongs to
  private final String tableName;

  // On_error option on this channel
  private final OnErrorOption onErrorOption;

  public static OpenChannelRequestBuilder builder(String channelName) {
    return new OpenChannelRequestBuilder(channelName);
  }

  /** A builder class to build a OpenChannelRequest */
  public static class OpenChannelRequestBuilder {
    private final String channelName;
    private String dbName;
    private String schemaName;
    private String tableName;
    private OnErrorOption onErrorOption;

    public OpenChannelRequestBuilder(String channelName) {
      this.channelName = channelName;
    }

    public OpenChannelRequestBuilder setDBName(String dbName) {
      this.dbName = dbName;
      return this;
    }

    public OpenChannelRequestBuilder setSchemaName(String schemaName) {
      this.schemaName = schemaName;
      return this;
    }

    public OpenChannelRequestBuilder setTableName(String tableName) {
      this.tableName = tableName;
      return this;
    }

    public OpenChannelRequestBuilder setOnErrorOption(OnErrorOption onErrorOption) {
      this.onErrorOption = onErrorOption;
      return this;
    }

    public OpenChannelRequest build() {
      return new OpenChannelRequest(this);
    }
  }

  private OpenChannelRequest(OpenChannelRequestBuilder builder) {
    Utils.assertStringNotNullOrEmpty("channel name", builder.channelName);
    Utils.assertStringNotNullOrEmpty("database name", builder.dbName);
    Utils.assertStringNotNullOrEmpty("schema name", builder.schemaName);
    Utils.assertStringNotNullOrEmpty("table name", builder.tableName);
    Utils.assertNotNull("on_error option", builder.onErrorOption);

    this.channelName = builder.channelName;
    this.dbName = builder.dbName;
    this.schemaName = builder.schemaName;
    this.tableName = builder.tableName;
    this.onErrorOption = builder.onErrorOption;
  }

  public String getDBName() {
    return this.dbName;
  }

  public String getSchemaName() {
    return this.schemaName;
  }

  public String getTableName() {
    return this.tableName;
  }

  public String getChannelName() {
    return this.channelName;
  }

  public String getFullyQualifiedTableName() {
    return String.format("%s.%s.%s", this.dbName, this.schemaName, this.tableName);
  }

  public OnErrorOption getOnErrorOption() {
    return this.onErrorOption;
  }
}