```yaml
meta:
  title: "other"
  description: "a really long description"
```
# coho

aaahjgkehjkgehjkgjhke

😂

Hallucinated API:

```kt

val source = FileSource("/path/to/source")

root {
    page("a") {
        markdown(source.file("file.md"))
        html(source.file("html-file.html"))
    }
}

// results in this file tree
// build/
// build/a/file.html
// build/a/html-file.html
```
