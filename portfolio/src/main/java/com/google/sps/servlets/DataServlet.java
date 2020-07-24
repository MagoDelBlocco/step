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

package com.google.sps.servlets;

import com.google.sps.comment.CommentStorage;
import com.google.sps.config.Constants;
import com.google.appengine.api.datastore.Entity;
import java.util.ArrayList;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content.*/
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private CommentStorage commentStorage = new CommentStorage();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;");

    response.getWriter().println(
             formatCommentsBulk(commentStorage.getComments(request.getQueryString())), "Comment");
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    commentStorage.addComment(request.getParameter("username"),
                              request.getParameter("comment"),
                              request.getParameter("image-id"),
                              "Comment");
  }

  private String formatCommentsBulk(final ArrayList<Entity> comments) {
    StringBuilder formattedComments = new StringBuilder();

    for (Entity it : comments) {
      formattedComments.append(formatComment(it));
    }

    return formattedComments.toString();
  }

  private StringBuilder formatComment(final Entity comment) {
    StringBuilder formattedComment = new StringBuilder();

    formattedComment.append("<div class=\"comment\">").append("<h6 class=\"comment-username\">")
                    .append(comment.getProperty("username")).append(" said at ")
                    .append(comment.getProperty("timestamp")).append(":</h6>")
                    .append(comment.getProperty("body")).append("</div>");

    return formattedComment;
  }
}
