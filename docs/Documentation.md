---
  layout: default.md
  title: "Documentation guide"
  pageNav: 3
---

# Documentation Guide

* We use [**MarkBind**](https://markbind.org/) to manage documentation.
* The `docs/` folder contains the source files for the documentation website.
* To learn how set it up and maintain the project website, follow the guide [[se-edu/guides] Working with Forked MarkBind sites](https://se-education.org/guides/tutorials/markbind-forked-sites.html) for project documentation.

## Local preview and diagram rendering

Install Node.js/npm, Java and [Graphviz](https://graphviz.org/download/) before building the site.
Graphviz provides the `dot` executable used by many of the PlantUML class, component and state diagrams.
Sequence diagrams and diagrams configured with Smetana can render without it, so a missing installation
may affect only some images. Graphviz is a documentation tool; users do not need it to run Laplace.

On macOS with Homebrew, install and verify Graphviz:

```sh
brew install graphviz
dot -V
```

From the repository root:

```sh
npm --prefix docs ci
npm --prefix docs run build
npm --prefix docs run serve
```

Open the preview URL printed by MarkBind. After installing Graphviz, stop any existing preview with
Ctrl+C and start it again, then refresh the browser so previously generated error images are replaced.
Inspect the diagrams themselves: a successful MarkBind build does not guarantee that PlantUML rendered
every image successfully.

If an image reports `Dot executable does not exist` or `Cannot find Graphviz`, confirm that `dot -V`
works in the terminal used to start MarkBind. If PlantUML still selects a nonexistent path, set its
[Graphviz executable location](https://plantuml.com/graphviz-dot) before rebuilding or serving:

```sh
export GRAPHVIZ_DOT="$(command -v dot)"
npm --prefix docs run build
npm --prefix docs run serve
```

For editor previews, restart the editor after installation and configure its Graphviz path if needed.
Do not add a machine-specific executable path to the diagram sources. The deployment workflow already
installs Graphviz on its Linux runner.

**Style guidance:**

* Follow the [**_Google developer documentation style guide_**](https://developers.google.com/style).
* Also relevant is the [_se-edu/guides **Markdown coding standard**_](https://se-education.org/guides/conventions/markdown.html).


**Converting to PDF**

* See the guide [_se-edu/guides **Saving web documents as PDF files**_](https://se-education.org/guides/tutorials/savingPdf.html).
