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
  
  const image = document.getElementById('focus-image'),
        caption = document.getElementById('focus-image-caption'),
        imageIndex = document.getElementById('focus-image-index'),
        commSection = document.getElementById('previous-comments');

  image.src = target.src;
  caption.innerHTML = target.alt;
  imageIndex.innerText = index;

  const params = new URLSearchParams();
  params.append('id', index);

  const response = await fetch('/data?' + index, {
                                                   method:'GET'
  });
  const comments = await response.text();

  commSection.innerHTML = comments;
}

function closeModal() {
  const modal = document.getElementById('modal');

  modal.style.display = 'none';
}

function postComment() {
  const comment = document.querySelector('form');
  const formData = new FormData(comment);
  const request = new XMLHttpRequest();

  request.open('POST', '/data');
  formData.append('id', document.getElementById('focus-image-index'));
  request.send(formData);
}
