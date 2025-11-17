```yaml
title: Serializing data in Godot
description: >
    A comparison of Godot's various serialization methods
tags: [godot, gdscript]
published-date: "2025-11-17T20:12:00Z"
# comment-did: "at://did:plc:lkrhpzgaij74fjzqeimfyicc/app.bsky.feed.post/3m25udn7ey22f"
hide: true
```

<!--
* INI: *very* simple configuration, annoying to set up, no API for serializing a Dictionary, etc.
* YAML: very powerful configuration and easily human-read and written, good for configuration files you expect your user to edit, must be used through addons
* JSON: in between, can serialized any Godot type into values consumable by third-party apps, somewhat easy to edit, very common
* Custom resources: can serialize subresources, supports all Godot types, can potentially contain executable code (bad for save file sharing, for example)
* `var_to_bytes`: Compact binary serialization, extremely hard to edit by a third party (but it can be done! Don't expect to make an "unhackable" save file with this)
* `var_to_str`: similar to var_to_bytes and JSON, uses the same serialization method that custom resources do but it can't serialize subresources, semi-prettified output format but lacking indentation like JSON and YAML-->

Godot contains multiple ways to serialize data to a String, each with their own positives and negatives. While this is subjective, I hope it gives some context as to which might be right for you.

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

The secret method Big Godot doesn't want you knowing about.
