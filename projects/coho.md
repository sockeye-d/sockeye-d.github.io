```yaml
meta:
  title: coho
  description: Static site generator written in Kotlin
```
# coho

Coho is a static site generator written in Kotlin. It supports
* live reload
* simple Kotlin-based configuration

[link](/projects/godl.md)

```qml
import QtQuick
import QtQuick.Layouts
import QtQuick.Controls as Controls
import org.kde.kirigami as Kirigami

// SquareButton.qml
import QtQuick

Rectangle {
    id: root

    signal activated(real xPosition, real yPosition)
    property point mouseXY
    property int side: 100
    width: side; height: side

    TapHandler {
        id: handler
        onTapped: root.activated(root.mouseXY.x, root.mouseXY.y)
        onPressedChanged: root.mouseXY = handler.point.position
    }
}
```

````markdown
# coho

Coho is a static site generator written in *Kotlin*. It supports
* live reload
* simple Kotlin-based configuration
````
