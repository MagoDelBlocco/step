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

public class CommentStorage {
  private DatastoreService storage;

  public CommentStorage() {
    storage = DatastoreServiceFactory.getDatastoreService();
  }

  public void addComment(final String username, final String commentBody, final String id,
                                                                          final String entity) {
    Integer idx = Integer.parseInt(id);

    if (idx > 0 && idx <= Constants.IMG_COUNT) {
      StringBuilder entityID = new StringBuilder(entity);
      entityID.append(id);

      Entity commentEntity = new Entity(entityID.toString());
      SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/mm/yyyy hh:mm");  
      Date date = new Date();  

      commentEntity.setProperty("username", username.equals("") ? "Anonymous" : username);
      commentEntity.setProperty("timestamp", dateFormatter.format(date));
      commentEntity.setProperty("body", commentBody.equals("") ? "Nothing" : commentBody);

      storage.put(commentEntity);
    }
  }

  public ArrayList<Entity> getComments(final String id, final String entity) {
    ArrayList<Entity> retval = new ArrayList<>();
    Integer idx = Integer.parseInt(id);
    StringBuilder entityID = new StringBuilder(entity);
    entityID.append(id);

    if (idx > 0 && idx <= Constants.IMG_COUNT) {
      Query query = new Query(entityID.toString()).addSort("timestamp", SortDirection.DESCENDING);
      PreparedQuery results = storage.prepare(query);

      for (Entity it : results.asIterable()) {
        retval.add(it);
      }
    }

    return retval;
  }
}
