# Google Student Training in Engineering Program

## Overview

This repo contains Bogdan Ciobanu's portfolio and STEP projects.

## Contents

#### A web application which contains the bio and portfolio of the author
- A frontpage with personal and contact info. The page has a modern layout,
  with a parallax effect, with the content at the front.
- A page which details various personal projects. Each project is listed
  in a collapsible list, with additional details given when clicking the title.
- A gallery page with photos of the author's cat. Each image is zoomable when
  hovered over.
- Each image in the gallery has its own comment section, handled by the
  Java Servlet backend. Each comment section is stored in the DataStore service
  and is retrieved accordingly to what image the user has clicked.

#### Walkthroughs for building a similar app

## How to use

#### Running the website
Run the command ```mvn package appengine:run``` from the portfolio
directory or ```bash ./preview.sh``` from the root directory of the
project. The webpage will be then accessible on localhost:8080.
