/**
 * Copyright (C) 2018 Expedia Inc.
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

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import org.apache.hadoop.hive.metastore.api.Table;
import org.springframework.stereotype.Component;

@Component
public class PartitionQueryFactory {

  public String newInstance(Table hiveTable, String partitionBy, String partitionFilter) {
    if (isNotBlank(partitionBy) && isNotBlank(partitionFilter)) {
      return String.format("select %s from %s.%s where %s group by %s order by %s", partitionBy, hiveTable.getDbName(),
          hiveTable.getTableName(), partitionFilter, partitionBy, partitionBy);
    } else if (isNotBlank(partitionBy) && isBlank(partitionFilter)) {
      return String.format("select %s from %s.%s group by %s order by %s", partitionBy, hiveTable.getDbName(),
          hiveTable.getTableName(), partitionBy, partitionBy);
    } else {
      throw new IllegalStateException(
          "Cannot create a partition filter query if neither partitionBy nor partitionFilter are provided");
    }
  }
}