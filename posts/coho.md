```yaml
title: Why I wrote coho
description: Why I wrote coho, the Kotlin programmer's static website generator
tags: [kotlin, web, tooling]
published-date: "2025-09-21T21:23:37Z"
updated-date: "2025-09-21T21:23:37Z"
```

There are plenty of other good static website generators, like MkDocs and Hugo.
So why would I write my own static website framework?

The short answer is that I like Kotlin, I wanted to do more with it, and I knew it could be compiled and run at runtime through the scripting APIs. I also had [Kotlin DSLs](https://kotlinlang.org/docs/type-safe-builders.html) on my mind and was thinking about how I could apply that to web development.

The other option in my mind was writing a pure vanilla HTML/JS/CSS static website, but that seemed like a daunting task since I wouldn't have access to any sort of higher level language features. The main ones I desired was Markdown parsing (this article is written in Markdown!) and templating (check out the [posts](/posts) page for templates in action). I also knew that I wanted to use a *real* language as the buildscript instead of a configuration language like YAML.

Also, if you couldn't tell, it's slightly inspired by [nob.h](https://github.com/tsoding/nob.h), although it's diverged from that model quite a bit.

## What coho isn't

Coho is not an all-in-one static website generator.
It's more of a framework builder to let you build your own little web framework.
It doesn't come with a preset selection of components.
It doesn't come with any built-in HTML (well, okay,
[it has](https://github.com/sockeye-d/coho/blob/f1d0c6d954c21c5866a5297aa8c70a1cf9b2285b/core/src/main/kotlin/dev/fishies/coho/markdown/ProcessedMarkdownFile.kt#L98) a
[little bit](https://github.com/sockeye-d/coho/blob/f1d0c6d954c21c5866a5297aa8c70a1cf9b2285b/core/src/main/kotlin/dev/fishies/coho/RootPath.kt#L10)),
and it doesn't control any styles.

That's wasn't my aim — there are plenty of other tools you can use (like the aforementioned Hugo) that do that for you. Rather, I wanted something that'd make the customizability of raw HTML available but also make things like content creation with metadataful Markdown possible as well.

## The final result

To me coho will likely never be actually fully featured — there'll always be things I can add to make life a little easier. For now though, these are a rough outline of the "unique" features it has:

* Kotlin DSL-based buildscript
* Kotlin-templated HTML files
* Static Markdown HTML generation with build-time syntax highlighting via Prism4j

## Kotlin DSL buildscript

Coho is *really* just a Kotlin builder DSL for defining the tree for the build folder.
There's the root element which defines the root of the output path:

```kotlin
root {

}
```

You can nest elements inside of it:

```kotlin
root {
    html(src("index.html"))
}
```

[`html`](https://coho.fishies.dev/core/dev.fishies.coho.html/html.html) is a method that adds a new file to the output path (in this case, just copying over an HTML file).

You can nest directories inside of directories:

```kotlin
root {
    html(src("index.html"))
    path("blog") {
        html(src("index.html"))
    }
}
```

And that's basically the basic functionality of coho. Define the folder structure, then define what goes in those folders.

## Kotlin-templated HTML files

The pitch for this is basically:

> Imagine PHP, but static, and not PHP at all

The [`ktHtml`](https://coho.fishies.dev/core/dev.fishies.coho.html/kt-html.html) function lets you template an HTML file that contains embedded Kotlin code within `#!kthtml <?kt /* ... */ ?>` blocks. The embedded code gets evaluated and echoed into the resulting HTML file.

This is, for example, how I made [/posts/index.html](/posts/):

```kthtml
<!DOCTYPE HTML>
<html>

<head>
    <meta charset='UTF-8'>
    <title>kotlin template test</title>
    <meta property='og:title' content='kotlin template test'>
    <meta property='og:description' content='a really long description'>
    <meta property='og:type' content='website'>
    <meta name="viewport" content="width=device-width,initial-scale=1" />
</head>

<body>
    <?kt headerHtml ?>
    <content>
        <h1> Blog posts </h1>
        <div class="tag-container">
            <?kt@file:Import; allTags.joinToString("", transform = ::clickableTag) ?>
        </div>
        <?kt@file:Import; (posts as List<Map<String?, Any? >>)
            .joinToString("") {
                val tags = (it["tags"] as List<*>).joinToString(" ") { "post-tag-$it" }
                tag("div", "class" to "card post-container ${tags}") {
                    val source = (it["source"] as Path).nameWithoutExtension
                    tag("a", "href" to "/posts/$source.html") {
                        tag("h2") {
                            append(it["title"])
                        }
                    }
                    tag("p") {
                        append(it["description"])
                    }
                    tag("p", "class" to "subtext") {
                        append("Published ")
                        append(it["pubDate"].formatDateTime())
                    }
                    tag("div", "class" to "tag-container") {
                        append((it["tags"] as List<*>)
                            .joinToString("", transform = ::clickableTag)
                        )
                    }
                }
            }?>
    </content>
    <?kt footerHtml ?>
</body>
<link rel='stylesheet' href='/style.css'>
<link rel='icon' href='/favicon.ico'>
<script src="/main.js"> </script>
<script src="/posts/index.js"> </script>

</html>
```

I basically directly iterate over the list of posts and emit extra XML using a basic XML builder, all right inside the HTML.
This creates an extremely flexible templating system that makes seemingly complex tasks quite simple.

<details>
<summary>
<!-- cursed -->
What's the <span class="inline-code"><code><span class="code-annotation code-builtin code-kotlin-annotation code-kotlin-builtin">@file:Import</span><span class="code-punctuation code-kotlin-punctuation">;</span></code></span> for?

</summary>

The `#!kotlin @file:Import;` at the beginning of each section is necessary annoyingly to work around a limitation of the Kotlin scripting engine configuration. It lets you import other scripts, but since the same configuration is applied to all the scripts (including the imported ones!) you get recursive dependency errors because there's no way to remove the included script just for one script. The only way to do that is to hook into the `onAnnotation` configuration refinement callback, hence the random annotation.

</details>

Also, if you're wondering where the `#!kotlin ::clickableTag` function came from, it's included in the root script like this:

```kotlin
root {
    /* ... */
    includes = listOf(src("util.coho.kts"))
    /* ... */
}
```

`includes`, like `markdownTemplate`, is propagated through the path hierarchy
(e.g. children inherit the value from their parents but can override it as necessary).

## How does this work internally?

Coho scripts are built in two phases: the evaluation phase, and the build phase.

### Evaluation

I use the Kotlin scripting engine (not the `javax.script` interface, I moved away from that for being too inflexible) to evaluate the `main.coho.kts` script. It returns a `RootPath` object which provides the entry point into the build directory.
The `RootPath` class inherits from `OutputPath`, which defines a folder with a set of children. If you look at the string representation of the object returned from evaluation, it actually looks like this:

```
root (RootPath)
    other.md (ProcessedMarkdownFile)
    index.html (KtHtmlFile)
    main.js (CopyFile)
    style.css (CopyFile)
    font.css (CopyFile)
    color.css (CopyFile)
    favicon.png (CopyFile)
    [build] magick -background transparent favicon.png -define icon:auto-resize=512,16,32 favicon.ico (ShellElement)
    projects (OutputPath)
        sled.md (ProcessedMarkdownFile)
        coho.md (ProcessedMarkdownFile)
        godl.md (ProcessedMarkdownFile)
        routine.md (ProcessedMarkdownFile)
    components (OutputPath)
        header.js (KtHtmlFile)
        footer.js (KtHtmlFile)
    fonts (OutputPath)
        LiberationSans-Regular.ttf (CopyFile)
        LiberationSerif-Regular.ttf (CopyFile)
        IosevkaWeb-Italic.woff2 (CopyFile)
        LiberationSans-BoldItalic.ttf (CopyFile)
        IosevkaWeb-Regular.woff2 (CopyFile)
        IosevkaWeb-Bold.woff2 (CopyFile)
        LiberationSerif-BoldItalic.ttf (CopyFile)
        LiberationSans-Bold.ttf (CopyFile)
        IosevkaWeb-BoldItalic.woff2 (CopyFile)
        LiberationSans-Italic.ttf (CopyFile)
        LiberationSerif-Bold.ttf (CopyFile)
        LiberationSerif-Italic.ttf (CopyFile)
    posts (OutputPath)
        coho.md (ProcessedMarkdownFile)
        run (null)
        index.js (CopyFile)
        index.html (KtHtmlFile)
    https://cdn.jsdelivr.net/npm/@tabler/icons-sprite@latest/dist/tabler-sprite.svg (DownloadElement)
    CNAME (null)
```

The hierarchy of output paths is effectively embedded inside the structure of the objects.

### Building

After I have the hierarchy of paths, I can just iterate over all the children.
The logic here is pretty simple — most types like `ProcessedMarkdownFile` and `KtHtmlFile` generate a single file, and `OutputPath` generates a new directory and then generates all its children in it.
The whole tree gets walked recursively like this, until an entire website is generated.

### Live-updating web server

There's also a little bit more magic involved to get the web server to work.
The live-reloading JS doesn't get included in the build output — it gets injected at runtime when the client requests a file from the built-in server:

```kotlin
const val RELOAD_JS = """
const reload = new WebSocket("/reload");
reload.addEventListener('message', event => {
    location.reload();
});
"""

private val endHtmlRegex = Regex("<\\s*?/\\s*?[hH][tT][mM][lL]\\s*?>")

private fun injectReloadJs(html: String): String {
    val endHtmlIndex = endHtmlRegex.find(html)?.groups?.get(0)?.range?.start ?: return html
    info("Injecting reload JS", verbose = true)
    // language=html
    return "${html.take(endHtmlIndex)}<script>$RELOAD_JS</script>${html.substring(endHtmlIndex)}"
}
```

The rest of the server is just a pretty basic Ktor Netty server with a websocket set up to relay filesystem change events to the client:

```kotlin
fun runLocalServer(buildPath: Path, reload: StateFlow<Int>, noReloadScript: Boolean, port: Int = 8080) =
    embeddedServer(Netty, port, host = "127.0.0.1") {
        install(WebSockets)
        routing {
            webSocket("/reload") {
                pos("Client connected, live reload is active", verbose = true)
                var lastReloadState: Int? = null
                reload.collect {
                    if (lastReloadState == null) {
                        lastReloadState = it
                    } else if (lastReloadState != it) {
                        lastReloadState = it
                        info("Reloading clients", verbose = true)
                        send("reload please")
                    }
                }
            }
            staticFiles("/", buildPath.absolute().toFile()) {
                modify { file, call ->
                    if (file.extension == "html" && !noReloadScript) {
                        call.respondText(injectReloadJs(file.readText()), ContentType.Text.Html)
                    }
                }
            }
        }
    }.start()
```

I use a hot StateFlow here to communicate between the server and filesystem watcher coroutines.
I don't know if this is the best way to do it, but it does work (although it looks a little cursed).

> [coho/cli/src/main/kotlin/dev/fishies/coho/cli/LocalServer.kt at main · sockeye-d/coho](https://github.com/sockeye-d/coho/blob/main/cli/src/main/kotlin/dev/fishies/coho/cli/LocalServer.kt)

## Conclusion

Coho may be the greatest thing I've ever made. I think it gives me the flexibility to write almost vanilla HTML/JS/CSS (which of course everything compiles down to anyway) while also having enough features to let me write this post in Markdown, have it get automatically converted to HTML with a fancy template, and be automatically syndicated through a statically hosted but dynamically generated (at build-time, that is) `rss.xml` feed.

For a project that came to me randomly while I was driving to a robotics meeting (dangerous, I know), I'd say it's gone pretty well.
As far as I know I'll be the only one using it, but it'd be pretty cool if you've got a static website you need making and decide to give it a spin.

> The source for this website can be [found on GitHub](https://github.com/sockeye-d/sockeye-d.github.io) as an example of coho in action.
