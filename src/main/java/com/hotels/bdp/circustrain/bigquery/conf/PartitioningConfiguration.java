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
package com.hotels.bdp.circustrain.bigquery.conf;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import static com.hotels.bdp.circustrain.bigquery.CircusTrainBigQueryConstants.PARTITION_BY;
import static com.hotels.bdp.circustrain.bigquery.CircusTrainBigQueryConstants.PARTITION_FILTER;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.hadoop.hive.metastore.api.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.hotels.bdp.circustrain.api.CircusTrainException;
import com.hotels.bdp.circustrain.api.Modules;
import com.hotels.bdp.circustrain.api.conf.SourceTable;
import com.hotels.bdp.circustrain.api.conf.TableReplication;
import com.hotels.bdp.circustrain.api.conf.TableReplications;
import com.hotels.bdp.circustrain.bigquery.util.TableNameFactory;

@Profile({ Modules.REPLICATION })
@Component
public class PartitioningConfiguration {

  private static final Logger log = LoggerFactory.getLogger(PartitioningConfiguration.class);

  private final Map<String, Map<String, Object>> replicationConfigMap;

  @Autowired
  PartitioningConfiguration(TableReplications tableReplications) {
    Map<String, Map<String, Object>> replicationConfig = new HashMap<>();
    for (TableReplication tableReplication : tableReplications.getTableReplications()) {
      SourceTable sourceTable = tableReplication.getSourceTable();
      String key = TableNameFactory
          .newInstance(sourceTable.getDatabaseName().trim().toLowerCase(Locale.ROOT),
              sourceTable.getTableName().trim().toLowerCase(Locale.ROOT));
      if (replicationConfig.containsKey(key)) {
        throw new CircusTrainException(
            "Partitioning cannot be carried out when there are duplicate source tables with partitioning configured");
      }
      log.debug("Loading BigQuery partitioning configuration for table {}", key);
      Map<String, Object> copierOptions = tableReplication.getCopierOptions();
      replicationConfig.put(key, copierOptions);
    }
    replicationConfigMap = Collections.unmodifiableMap(replicationConfig);
  }

  public String getPartitionFilterFor(Table table) {
    String key = TableNameFactory.newInstance(table);
    log.debug("Loading 'partition-filter' for table {}", key);
    return getPartitionForKey(key, PARTITION_FILTER);
  }

  public String getPartitionByFor(Table table) {
    String key = TableNameFactory.newInstance(table);
    log.debug("Loading 'partition-by' for table {}", key);
    return getPartitionForKey(key, PARTITION_BY);
  }

  private String getPartitionForKey(String key, String partitionType) {
    if (replicationConfigMap.containsKey(key)) {
      Map<String, Object> copierOptions = replicationConfigMap.get(key);
      if (copierOptions != null && copierOptions.containsKey(partitionType)) {
        Object yamlValue = copierOptions.get(partitionType);
        if (yamlValue != null) {
          return yamlValue.toString();
        }
      }
    }
    return null;
  }

  public boolean isPartitioningConfigured(Table table) {
    return isNotBlank(getPartitionByFor(table));
  }
}
