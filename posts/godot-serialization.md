```yaml
title: Serializing data in Godot
description: >
    A comparison of Godot's various serialization methods
tags: [godot, gdscript]
published-date: "2025-11-17T20:12:00Z"
# comment-did: "at://did:plc:lkrhpzgaij74fjzqeimfyicc/app.bsky.feed.post/3m25udn7ey22f"
hide: true
```

Godot contains multiple ways to serialize data, each with their own positives and negatives. While this is subjective, I hope it gives some context as to which might be right for you.

## ConfigFile (INI)

[`#!gdscript ConfigFile`](https://docs.godotengine.org/en/stable/classes/class_configfile.html) is Godot's [INI-style](https://en.wikipedia.org/wiki/INI_file) data serialization method. Its API is simple to use but somewhat lacking. Unlike JSON, it doesn't store structured objects like a Dictionary directly. Rather, each file is split into `[section]`s containing any number of `key=value` pairs. For example, this code

```gdscript
var config := ConfigFile.new()
config.set_value("a section", "a value", "hi")
```

generates this file:

```ini
[a section]

"a value"="hi"
```

Without any whitespace, escaped quotes, or `=` in the key, it generates without quotes:

```ini
a-value="hi"
```

You can also insert an entire `#!gdscript Dictionary` or `#!gdscript Array` as the value.

```gdscript
var typed_dict: Dictionary[String, int] = {
    "hi": 1,
    "hello": 2,
}
var packed_array: PackedInt32Array = [1, 2, 3]
var typed_array: Array[StringName] = ["1", "2", "3"]
var untyped_dict: Dictionary = {
    "a": "b",
    5: 10,
}
var untyped_array: Array = [1, "2", 3]
config.set_value("", "typed_dict", typed_dict)
config.set_value("", "packed_array", packed_array)
config.set_value("", "typed_array", typed_array)
config.set_value("untyped", "untyped_dict", untyped_dict)
config.set_value("untyped", "untyped_array", untyped_array)
```

```ini
typed_dict=Dictionary[String, int]({
"hello": 2,
"hi": 1
})
packed_array=PackedInt32Array(1, 2, 3)
typed_array=Array[StringName]([&"1", &"2", &"3"])

[untyped]

untyped_dict={
5: 10,
"a": "b"
}
untyped_array=[1, "2", 3]
```

Like Resources and unlike JSON, it uses the packed array (`#!gdscript PackedInt32Array(1, 2, 3)`), typed array (`#!gdscript Array[StringName]([&"1", &"2", &"3"])`), and dictionary literals (`#!gdscript Dictionary[String, int({"hello": 2, "hi": 1 })`) where a typed value is provided. Also, using an empty string as the section name inserts the key into the top level.



To save the ConfigFile's data to disk, you can use [`#!gdscript ConfigFile.save`](https://docs.godotengine.org/en/stable/classes/class_configfile.html#class-configfile-method-save) or one of its encrypted variants. While having this built-in encryption is *nice,* you can't save ConfigFiles to strings, only directly to a file, which limits its use cases. Of course, it's possible to make, save it to, and read it from a temp file but that's quite a bit more work. The built-in encrypted save methods are overshadowed by [`#!gdscript FileAccess.open_encrypted`](https://docs.godotengine.org/en/stable/classes/class_fileaccess.html#class-fileaccess-method-open-encrypted), which does effectively the same thing while being more flexible and much simpler.

To get data back out of the serialized file, you can use [`#!gdscript ConfigFile.load`](https://docs.godotengine.org/en/stable/classes/class_configfile.html#class-configfile-method-load) or one the encrypted variant that matches how you saved it. Again with the encryption, `#!gdscript FileAccess.open_encrypted` is basically a superset of `#!gdscript ConfigFile`'s encryption facilities. There's also [`#!gdscript ConfigFile.parse`](https://docs.godotengine.org/en/stable/classes/class_configfile.html#class-configfile-method-parse), which *merges* the values of the given string into the `#!gdscript ConfigFile` object.

I think ConfigFiles an interface best suited for serializing a bunch of ad-hoc values. It has an immediate-mode API that seems nice on the surface, but becomes clunky once your serialization code is spread further across more types and functions. My general way of serializing data is to make functions that return Dictionaries, which I then merge together in more dictionaries and arrays, building up the data as I go and ConfigFile doesn't really support that. However, INI files are *very* easy to user-edit. The section-key-value layout is easy to visually parse and doesn't require you to track indentation. In addition, ConfigFile supports **all** Variant types, unlike JSON.

> Fun fact: the `project.godot` file uses a ConfigFile

## JSON

Godot's built-in JSON support comes in two flavors: static methods and a `#!gdscript JSON` type.

### JSON, the static method

The static methods are very straightforward to use. Serialize data (any type!) with [`#!gdscript JSON.stringify(data)`](https://docs.godotengine.org/en/stable/classes/class_json.html#class-json-method-stringify), including pretty printing, and parse it with [`#!gdscript JSON.parse_string(string)`](https://docs.godotengine.org/en/stable/classes/class_json.html#class-json-method-parse-string)

```gdscript
print(JSON.stringify(123, "  "))
print(JSON.stringify([1, 2, 3], "  "))
var typed_array: Array[StringName] = ["1", "2", "3"]
print(JSON.stringify(typed_array, "  "))
var array = [1, "2", 3]
print(JSON.stringify(array, "  "))
print(JSON.stringify({ "a": "b", "c": "hello", 100: "hi", "nested": { "dictionaries": true } }, "  "))
```

results in

```json
123
[
  1,
  2,
  3
]
[
  "1",
  "2",
  "3"
]
[
  1,
  "2",
  3
]
{
  "100": "hi",
  "a": "b",
  "c": "hello",
  "nested": {
    "dictionaries": true
  }
}
```

> Passing in `#!gdscript "  "` as the second argument makes it use indentations and newlines. Without it, everything would go on one like with no spacing.

Getting the data back is just as trivial: load the file as a String however you'd like, and pass it to [`#!gdscript JSON.parse_string`](https://docs.godotengine.org/en/stable/classes/class_json.html#class-json-method-parse-string). It's a "loose" (e.g. noncompliant) parser that allows trailing commas, but no comments.

As you can see, it doesn't yield the typed array literals that ConfigFile used. This is because those aren't valid JSON, and would make it unparsable by other implementations.

> Speaking of "unparsable by other implementations", Godot used to have some "bad" behavior regarding `#!gdscript INF` and `#!gdscript NAN`.
> Since they get stringified as plain string literals ("inf" and "nan") which are not valid JSON literals, nothing can read the JSON strings that are produced. Not even Godot.
>
> ```gdscript
> print(JSON.stringify({ "INF": INF }))
> print(JSON.stringify({ "NAN": NAN }))
> ```
>
> yields
>
> ```json
> {"INF":inf}
> {"NAN":nan}
> ```
>
> This was an *incredibly* strange bug which causes `#!gdscript JSON.parse_string(JSON.stringify(NAN))` to return `#!gdscript null` (e.g. invalid JSON) instead of `#!gdscript NAN`.
>
> A fix was implemented with [#108837](https://github.com/godotengine/godot/pull/108837), but due to things happening (see [this comment on #111496](https://github.com/godotengine/godot/pull/111496#pullrequestreview-3326429539)) it was reverted quickly due to a consensus not actually having been reached. It was then resubmitted on October 10th and remerged on October 30th with [#111498](https://github.com/godotengine/godot/pull/111498), and is now fixed for 4.6-dev3. Maybe it'll get backported to a patch version of 4.5.
>
> With this fix merged, `#!gdscript INF` is stringified as a large float (`#!json 1e99999`) and `#!gdscript NAN` is stringified as `#!json null`, matching other implementations.

### JSON, the resource

Believe it or not (I didn't know before writing this!), `#!gdscript JSON` inherits from `#!gdscript Resource`. Any JSON files in the resource filesystem will actually get loaded into the engine as a `#!gdscript JSON`-typed object. You can get the data out of it with [`#!gdscript JSON.data`](https://docs.godotengine.org/en/stable/classes/class_json.html#class-json-property-data) and use [`#!gdscript JSON.parse`](https://docs.godotengine.org/en/stable/classes/class_json.html#class-json-method-parse) to parse a new string into it. Beyond that, it's not very noteworthy.

### JSON, the conclusion

Overall, JSON is a good pick for most serialization needs. It's easy to manipulate with tools like jq, many languages either have built-in support for JSON or very good third-party libraries, and it's very simple (looking at you [YAML](https://www.arp242.net/yaml-config.html#its-pretty-complex)). This simplicity comes at a cost of some niceties, such as a lack of comments, no trailing commas on serialization (this one actually makes me so mad), and keys can only be strings. All of these things make it more annoying to human-edit.

## Resources

> The secret method Big Godot doesn't want you knowing about.

[`#!gdscript Resource`](https://docs.godotengine.org/en/stable/classes/class_resource.html)s are Godot's primary data serialization format. Most things in the filesystem are resources or get imported as resources, like how a JSON file gets imported to a JSON Resource. However, things like [scenes](https://docs.godotengine.org/en/stable/classes/class_packedscene.html), [themes](https://docs.godotengine.org/en/stable/classes/class_theme.html), and [curves](https://docs.godotengine.org/en/stable/classes/class_curve2d.html) *are* Resources rather than just being imported as resources. When you save one of those to the resource filesystem, it results in (normally) a `.tres` file containing all the data stored inside the resource object.

While Godot comes with many built-in resource types, you can make your own simply by extending the Resource class:

```gdscript
extends Resource
@export var property: String
```

By giving it a class name to make it a global class, it also shows up in the create resource dialogs:

```gdscript
class_name MyCustomResource extends Resource
@export var property: String
```

Like [`#!gdscript Node`](https://docs.godotengine.org/en/stable/classes/class_node.html), you can use the different `#!gdscript @export` annotations to control how properties are displayed in the inspector. This gives Resources the unique attribute that not only can you serialize them to disk, Godot will auto-generate an editor for your resource from its script as well. You can even make it a tool script and implement custom validation logic:

```gdscript
@tool
class_name MyCustomResource extends Resource
@export var property: String:
    set(value):
        if not value.begins_with("h"):
            return
        property = value
```

> Like Nodes, `#!gdscript @export_storage` gives you a serialized but hidden property.

Like ConfigFile and unlike JSON, Resources can serialize any type of Variant *including* other Resources.

To save a resource, you can use [`#!gdscript ResourceSaver.save(resource, path = "", flags = 0)`](https://docs.godotengine.org/en/stable/classes/class_resourcesaver.html). `path` can be a resource filesystem (e.g. `res://`) path, which really only works for editors, or it can be an absolute path (either absolute absolute or `user://` path), or it can be empty (the default) to make `resource` get saved to wherever it was saved before. Saving a resource with a `.tres` file extension causes it to get saved in a text format, and `.res` makes it get saved in a binary format.

To load a resource, you can use [`#!gdscript load(path)`](https://docs.godotengine.org/en/stable/classes/class_%40gdscript.html#class-gdscript-method-load). While it's normally used on `res://` paths, it can also be used to load external files.

When you deserialize a custom resource, you get your custom resource type back, making accessing properties completely type safe:

```gdscript
var resource := MyCustomResource.new()
resource.propery = "hello"
ResourceSaver.save("/home/fish/my_custom_resource.tres", resource)

# later...

var resource := load("/home/fish/my_custom_resource.tres") as MyCustomResource
print(resource.property)
```

> I'm not sure why there isn't a simple `#!gdscript save(resource, path)` counterpart to `#!gdscript load(path)`

A Resource's safety makes it an incredibly powerful tool for data serialization. Almost all Godot constructs are supported by it, and you can choose between a verbose text format and a compact binary format, making it very versatile. However, you can only save resources to file paths, because each resource needs to "know" where it's serialized to to correctly serialize subresources. If you're concerned about portability, JSON is definitely better.

Resource's text format, while not meant to be easily human editable, can be patched by hand. Its binary format on the other hand, is not easy to modify externally at all. If you're thinking about using this to add security to your save files, [**don't**](https://en.wikipedia.org/wiki/Security_through_obscurity). People will reverse engineer your formats anyway, especially because Godot's binary resource format's source can be [easily found](https://github.com/godotengine/godot/blob/master/core/io/resource_format_binary.cpp) and [reverse engineered](https://github.com/GDRETools/gdsdecomp).

## var_to_str and var_to_bytes

I like this function. It retains most of the good parts of JSON while sidestepping all the bad parts.

As the name implies, it takes one Variant and converts it to a string. For example,

```gdscript
print(var_to_str(123))
print(var_to_str([1, 2, 3]))
var typed_array: Array[StringName] = ["1", "2", "3"]
print(var_to_str(typed_array))
var array = [1, "2", 3]
print(var_to_str(array))
print(var_to_str({ "a": "b", "c": "hello", 100: "hi", "nested": { "dictionaries": true } }))
```

results in

```gdscript
123
[1, 2, 3]
Array[StringName]([&"1", &"2", &"3"])
[1, "2", 3]
{
100: "hi",
"a": "b",
"c": "hello",
"nested": {
"dictionaries": true
}
}
```

As you can see, it retains the typed literals of ConfigFile (they use the same system!), but is as simple and no-frills as JSON. If you want to serialize an array, just pass it an array. If you want to serialize some key/value pairs, just pass it a dictionary. And unlike JSON, it supports all types in Godot, even non-string map keys.

To serialize a Variant to a string, use [`#!gdscript var_to_str(variant)`](https://docs.godotengine.org/en/stable/classes/class_%40globalscope.html#class-globalscope-method-var-to-str). If you want to convert a string to a Variant, use [`#!gdscript str_to_var(string)`](https://docs.godotengine.org/en/stable/classes/class_%40globalscope.html#class-globalscope-method-str-to-var). It's as simple as JSON, but much more powerful.

[`#!gdscript var_to_bytes(variant)`](https://docs.godotengine.org/en/stable/classes/class_%40globalscope.html#class-globalscope-method-var-to-bytes) and [`#!gdscript bytes_to_var(variant)`](https://docs.godotengine.org/en/stable/classes/class_%40globalscope.html#class-globalscope-method-bytes-to-var) work the same as their string siblings, but use a PackedByteArray instead of a String. The returned value is much more compact, using the binary format specified in ["Binary serialization API"](https://docs.godotengine.org/en/stable/tutorials/io/binary_serialization_api.html). Again, it's not a good security practice to rely on the fact that it's a binary format to avoid reverse engineering. [`#!gdscript var_to_bytes_with_objects(variant)`](https://docs.godotengine.org/en/stable/classes/class_%40globalscope.html#class-globalscope-method-var-to-bytes-with-objects) and [`#!gdscript bytes_to_var_with_objects(bytes)`](https://docs.godotengine.org/en/stable/classes/class_%40globalscope.html#class-globalscope-method-bytes-to-var-with-objects) are effectively the same, but they serialize scripts as well. This can create security issues as embedding scripts allow them to execute arbitrary code on deserialization. I'd just avoid these methods.

If I'm looking for an easy way to serialize lots of data, I normally reach for [`#!gdscript var_to_bytes(variant)`](https://docs.godotengine.org/en/stable/classes/class_%40globalscope.html#class-globalscope-method-var-to-bytes).

## Which one's the best?

The one that's best depends entirely on your needs.

I wouldn't recommend ever using ConfigFile for anything — its API is just so bad compared to the other options explored here.

JSON is an industry standard well-rounded option. It's not specifically good for anything in particular, but it's widely used, many tools and languages accept it out of the box (except Java), and it's very well supported.

Custom resources excel when you don't need the files to be easily human editable and you're okay with putting in a little more effort to set up the resource classes. Its API is *very* simple, however, the fact that they can't be serialized to a String directly can be limiting in some cases.

var_to_str and var_to_bytes are just so simple and straightforward. They can be serialized directly into a string or to raw bytes, which can then be transmitted over the network, saved to a file, or shown to the user. They aren't as easily human-editable as JSON, nor do they have the industry support, but their convenience is unmatched.

For [sunfish](https://github.com/sockeye-d/sunfish), I use [`#!gdscript var_to_bytes(variant)`](https://docs.godotengine.org/en/stable/classes/class_%40globalscope.html#class-globalscope-method-var-to-bytes) in combination with zstd compression to save and load the project files, since it's so much simpler conceptually and practically to use.

## Beyond the standard library

There are even more options than explored here. For example, you could use a [SQLite](https://godotengine.org/asset-library/asset/1686) database for high-performance queries. You could use [YAML](https://godotengine.org/asset-library/asset/3774) or [TOML](https://godotengine.org/asset-library/asset/3395) for more exotic markup languages. You could write your own if you hated the rest of the options. While I'm satisfied with Godot's built-in options, you can always go further than this.
