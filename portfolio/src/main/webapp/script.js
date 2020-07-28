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

function expandCollapsible(element) {
  const content = element.nextElementSibling;

  content.style.maxHeight = content.style.maxHeight ? null :
                                                      content.scrollHeight + 'px';
}

function navtoggle() {
  document.getElementById('navmenu').classList.toggle('visible');
}

async function openModal(target, index) {
  const modal = document.getElementById('modal');

  modal.style.display = 'block';
  
  const image = document.getElementById('focus-image');
  const caption = document.getElementById('focus-image-caption');
  const imageID = document.getElementById('image-id');

  image.src = target.src;
  caption.innerHTML = target.alt;
  imageID.value = index;

  const params = new URLSearchParams();
  params.append('id', index);

  const response = await fetch('/data?' + index, {
                                                   method: 'GET'
  });
  const comments = await response.text();
  
  displayComments(comments, document.getElementById('previous-comments'));
}

function displayComments(comments, commSection) {
  const commentsJSON = eval(comments);

  for (let i = 0; i < commentsJSON.length; ++i) {
    const commentHeader = document.createElement('h5');
    commentHeader.innerText = commentsJSON[i].username + ' said at ' +
                              commentsJSON[i].zorblax + ':';
    commSection.appendChild(commentHeader);

    const commentBody = document.createElement('div');
    commentBody.innerText = commentsJSON[i].body;
    commSection.appendChild(commentBody);
  }
}

function closeModal() {
  const modal = document.getElementById('modal');

  modal.style.display = 'none';
}

function submitComment() {
  document.forms['comment-form'].submit();

  location.reload();
}
