/**
 * Copyright 2007 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hama.algebra;

import java.io.IOException;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scanner;
import org.apache.hadoop.hbase.io.RowResult;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hama.Constants;
import org.apache.hama.DenseMatrix;
import org.apache.hama.HamaConfiguration;
import org.apache.hama.SubMatrix;
import org.apache.hama.io.BlockID;
import org.apache.hama.io.BlockWritable;
import org.apache.hama.mapred.BlockInputFormat;
import org.apache.log4j.Logger;

public class BlockCyclicMultiplyMap extends MapReduceBase implements
    Mapper<BlockID, BlockWritable, BlockID, BlockWritable> {
  static final Logger LOG = Logger.getLogger(BlockCyclicMultiplyMap.class);
  protected DenseMatrix matrix_b;
  public static final String MATRIX_B = "hama.multiplication.matrix.b";

  public void configure(JobConf job) {
    try {
      matrix_b = new DenseMatrix(new HamaConfiguration(), job.get(MATRIX_B, ""));
    } catch (IOException e) {
      LOG.warn("Load matrix_b failed : " + e.getMessage());
    }
  }

  public static void initJob(String matrix_a, String matrix_b,
      Class<BlockCyclicMultiplyMap> map, Class<BlockID> outputKeyClass,
      Class<BlockWritable> outputValueClass, JobConf jobConf) {

    jobConf.setMapOutputValueClass(outputValueClass);
    jobConf.setMapOutputKeyClass(outputKeyClass);
    jobConf.setMapperClass(map);
    jobConf.set(MATRIX_B, matrix_b);

    jobConf.setInputFormat(BlockInputFormat.class);
    FileInputFormat.addInputPaths(jobConf, matrix_a);

    jobConf.set(BlockInputFormat.COLUMN_LIST, Constants.BLOCK);
  }

  @Override
  public void map(BlockID key, BlockWritable value,
      OutputCollector<BlockID, BlockWritable> output, Reporter reporter)
      throws IOException {
    // we don't need to get blockSize each time
    int blockSize = matrix_b.getRows();
    SubMatrix a = value.get();
    HTable table = matrix_b.getHTable();

    // startKey : new BlockID(key.getColumn(), 0).toString()
    // endKey : new BlockID(key.getColumn(), blockSize+1).toString()
    Scanner scan = table.getScanner(new byte[][] { Bytes
        .toBytes(Constants.BLOCK) },
        new BlockID(key.getColumn(), 0).getBytes(), new BlockID(
            key.getColumn(), blockSize + 1).getBytes());

    for (RowResult row : scan) {
      BlockID bid = new BlockID(row.getRow());
      SubMatrix b = new SubMatrix(row.get(Constants.BLOCK).getValue());
      SubMatrix c = a.mult(b);
      output.collect(new BlockID(key.getRow(), bid.getColumn()),
          new BlockWritable(c));
    }
    scan.close();
  }
}