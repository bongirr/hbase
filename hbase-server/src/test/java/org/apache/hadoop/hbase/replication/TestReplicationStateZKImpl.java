/**
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

package org.apache.hadoop.hbase.replication;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.MediumTests;
import org.apache.hadoop.hbase.Server;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.catalog.CatalogTracker;
import org.apache.hadoop.hbase.zookeeper.ZKUtil;
import org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher;
import org.apache.zookeeper.KeeperException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

@Category(MediumTests.class)
public class TestReplicationStateZKImpl extends TestReplicationStateBasic {

  private static Configuration conf;
  private static HBaseTestingUtility utility;
  private static ZooKeeperWatcher zkw;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    utility = new HBaseTestingUtility();
    utility.startMiniZKCluster();
    conf = utility.getConfiguration();
    zkw = HBaseTestingUtility.getZooKeeperWatcher(utility);
  }

  @Before
  public void setUp() throws KeeperException {
    DummyServer ds1 = new DummyServer(server1);
    DummyServer ds2 = new DummyServer(server2);
    DummyServer ds3 = new DummyServer(server3);
    rq1 = new ReplicationQueuesZKImpl(zkw, conf, ds1);
    rq2 = new ReplicationQueuesZKImpl(zkw, conf, ds2);
    rq3 = new ReplicationQueuesZKImpl(zkw, conf, ds3);
    rqc = new ReplicationQueuesClientZKImpl(zkw, conf, ds1);
  }

  @After
  public void tearDown() throws KeeperException, IOException {
    String replicationZNodeName = conf.get("zookeeper.znode.replication", "replication");
    String replicationZNode = ZKUtil.joinZNode(zkw.baseZNode, replicationZNodeName);
    ZKUtil.deleteNodeRecursively(zkw, replicationZNode);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    utility.shutdownMiniZKCluster();
  }

  static class DummyServer implements Server {
    private String serverName;
    private boolean isAborted = false;
    private boolean isStopped = false;

    public DummyServer(String serverName) {
      this.serverName = serverName;
    }

    @Override
    public Configuration getConfiguration() {
      return conf;
    }

    @Override
    public ZooKeeperWatcher getZooKeeper() {
      return zkw;
    }

    @Override
    public CatalogTracker getCatalogTracker() {
      return null;
    }

    @Override
    public ServerName getServerName() {
      return new ServerName(this.serverName);
    }

    @Override
    public void abort(String why, Throwable e) {
      this.isAborted = true;
    }

    @Override
    public boolean isAborted() {
      return this.isAborted;
    }

    @Override
    public void stop(String why) {
      this.isStopped = true;
    }

    @Override
    public boolean isStopped() {
      return this.isStopped;
    }
  }
}

