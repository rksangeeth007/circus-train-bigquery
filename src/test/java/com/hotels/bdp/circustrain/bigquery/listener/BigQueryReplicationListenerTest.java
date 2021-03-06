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
package com.hotels.bdp.circustrain.bigquery.listener;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.Table;
import com.google.cloud.storage.Storage;

import com.hotels.bdp.circustrain.api.CircusTrainException;
import com.hotels.bdp.circustrain.api.event.EventReplicaTable;
import com.hotels.bdp.circustrain.api.event.EventSourceTable;
import com.hotels.bdp.circustrain.api.event.EventTableReplication;
import com.hotels.bdp.circustrain.bigquery.extraction.service.ExtractionService;

@RunWith(MockitoJUnitRunner.class)
public class BigQueryReplicationListenerTest {

  private @Mock EventTableReplication eventTableReplication;
  private @Mock ExtractionService service;
  private @Mock BigQuery bigQuery;
  private @Mock Storage storage;

  private BigQueryReplicationListener listener;

  @Before
  public void init() {
    listener = new BigQueryReplicationListener(service);
    when(eventTableReplication.getSourceTable()).thenReturn(mock(EventSourceTable.class));
    when(eventTableReplication.getReplicaTable()).thenReturn(mock(EventReplicaTable.class));
  }

  @Test
  public void tableReplicationStart() {
    Dataset dataset = mock(Dataset.class);
    when(bigQuery.getDataset(anyString())).thenReturn(dataset);
    Table table = mock(Table.class);
    when(dataset.get(anyString())).thenReturn(table);

    listener.tableReplicationStart(eventTableReplication, "eventId");

    verifyZeroInteractions(storage);
  }

  @Test
  public void tableReplicationSuccess() {
    BigQueryReplicationListener listener = new BigQueryReplicationListener(service);
    Dataset dataset = mock(Dataset.class);
    when(bigQuery.getDataset(anyString())).thenReturn(dataset);
    Table table = mock(Table.class);
    when(dataset.get(anyString())).thenReturn(table);
    listener.tableReplicationSuccess(eventTableReplication, "eventId");
    verify(service).cleanup();
  }

  @Test
  public void tableReplicationFailure() {
    BigQueryReplicationListener listener = new BigQueryReplicationListener(service);
    Dataset dataset = mock(Dataset.class);
    when(bigQuery.getDataset(anyString())).thenReturn(dataset);
    Table table = mock(Table.class);
    when(dataset.get(anyString())).thenReturn(table);
    listener.tableReplicationFailure(eventTableReplication, "eventId", mock(CircusTrainException.class));
    verify(service).cleanup();
  }
}
