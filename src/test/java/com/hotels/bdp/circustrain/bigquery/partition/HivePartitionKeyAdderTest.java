/**
 * Copyright (C) 2018-2019 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hotels.bdp.circustrain.bigquery.partition;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.Table;
import org.junit.Test;

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.Schema;

public class HivePartitionKeyAdderTest {

  private final Table table = new Table();

  @Test
  public void addWithNullSchemaDoesNothing() {
    Table result = new HivePartitionKeyAdder(table).add("key", null);
    assertThat(result, is(table));
  }

  @Test
  public void addPartitionKeys() {
    String key = "foo";
    Schema schema = Schema.of(Field.of(key, LegacySQLTypeName.STRING), Field.of("bar", LegacySQLTypeName.BOOLEAN));
    Table result = new HivePartitionKeyAdder(table).add(key, schema);
    List<FieldSchema> partitionKeys = result.getPartitionKeys();
    FieldSchema partitionKey = partitionKeys.get(0);

    assertThat(partitionKey.getName(), is(key));
    assertThat(partitionKey.getType(), is("string"));
    assertThat(result, not(table));
  }
}
