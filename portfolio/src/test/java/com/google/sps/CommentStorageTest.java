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

package com.google.sps;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class CommentStorageTest {

  @Test
  public void testAddComment() {
    //  I think doing dependency injection for the date is overkill right now
    CommentStorage storage = new CommentStorage();

    storage.addComment("test1", "this is test1", "1", "TEST");
    storage.addComment("test2", "this is test2", "0", "TEST");
    storage.addComment("test3", "this is test3", "16", "TEST");
    storage.addComment("test4", "this is test4", "-1", "TEST");
    storage.addComment("test5", "this is test5", "5", "BEST");

    // TODO: make CommentStorage::storage package-visibile, and assert on it?
  }
}
