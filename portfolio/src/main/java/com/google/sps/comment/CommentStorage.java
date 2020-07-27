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

import com.google.sps.config.Constants;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.PreparedQuery;
import java.util.ArrayList;
import java.text.SimpleDateFormat;  
import java.util.Date;
import java.lang.NumberFormatException;

/** Wrapper over Datastore. Allows for safe adding and retrieval of entities, requiring
 * a specific format, all fields as Strings: [author name], [entity body], [entity name],
 * entity's section id.
 */
public class CommentStorage {
  private DatastoreService storage;

  public CommentStorage() {
    storage = DatastoreServiceFactory.getDatastoreService();
  }

  /** If the entity id doesn't have a numeric format, the request will be ignored. */
  public void addStorageEntry(final String username, final String body,
                              final String id,       final String entity) {
    int idx;
    try {
      idx = Integer.parseInt(id);
    } catch(NumberFormatException e) {
      return;
    }

    if (idx > 0 && idx <= Constants.IMG_COUNT) {
      Entity commentEntity = new Entity(entity + id);
      SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mm");  

      commentEntity.setProperty("username", username.equals("") ? "Anonymous" : username);
      commentEntity.setProperty("timestamp", dateFormatter.format(new Date()));
      commentEntity.setProperty("body", body.equals("") ? "Nothing" : body);

      storage.put(commentEntity);
    }
  }

  /** If  the entity id doesn't have a numeric format, the request will return null. */
  public Iterable<Entity> getStorageEntries(final String keyword, final String id) {
    int idx;
    try {
      idx = Integer.parseInt(id);
    } catch(NumberFormatException e) {
      return null;
    }

    if (idx > 0 && idx <= Constants.IMG_COUNT) {
      Query query = new Query(keyword + id).addSort("timestamp", SortDirection.DESCENDING);
      PreparedQuery results = storage.prepare(query);

      return results.asIterable();
    } else {
      return null;
    }
  }
}
