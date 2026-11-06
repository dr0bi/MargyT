# MargyT

MargyT is a project for working with plugins in the `.mtp` format.

## Building an HTMLWidgets plugin

From the project directory, copy the widget files into the example input directory and run the plugin builder:

```bash
mkdir -p examples/htmlwidgets
cp -R ../HtmlWidgets/. examples/htmlwidgets/

python3 -m margyt.plugin examples/htmlwidgets
```

The generated `.mtp` files can then be copied to a distribution directory:

```bash
mkdir -p ../dist
cp examples/htmlwidgets/*.mtp ../dist/
```

## Project status

This README documents the currently known plugin build workflow. More details about installation, configuration, and supported plugin features can be added as the project develops.
