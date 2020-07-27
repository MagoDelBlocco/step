// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.comment;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.PreparedQuery;

@RunWith(JUnit4.class)
public final class CommentStorageTest {
  private DatastoreService exposedStorage;

  public CommentStorageTest() {
    exposedStorage = DatastoreServiceFactory.getDatastoreService();
  }

  @Test
  public void testAddEntryIds() {
    // CommentStorage storage = new CommentStorage();

    // storage.addStorageEntry("", "", "1", "TEST");
    // storage.addStorageEntry("", "", "-1", "TEST");
    // storage.addStorageEntry("", "", "abcd", "TEST");

    // Query query = new Query("TEST1");
    // PreparedQuery results = exposedStorage.prepare(query);

    // Assert.assertEquals(elemCount(results.asIterable()), 1);

    // query = new Query("TEST-1");
    // results = exposedStorage.prepare(query);

    // Assert.assertEquals(elemCount(results.asIterable()), 0);

    // query = new Query("TESTabcd");
    // results = exposedStorage.prepare(query);

    // Assert.assertEquals(elemCount(results.asIterable()), 0);
  }

  private int elemCount(final Iterable<Entity> target) {
    int retval = 0;

    for (Entity it : target) {
      ++retval;
    }

    return retval;
  }
}
