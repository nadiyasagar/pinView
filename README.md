# CustomPinView – Android PIN Input Library

**CustomPinView** is a flexible and customizable PIN input view for Android, built using Kotlin and XML. It supports numeric input, clear and done buttons, and visually indicates the entered PIN with circular indicators.

---

## 🌟 Features

- PIN input with 0–9 numeric buttons  
- Clear and Done buttons with separate customizable colors  
- Visual filled/empty circles to show PIN progress  
- Automatic `onPinComplete` callback when PIN reaches full length  
- Fully customizable colors, sizes, and text/icon styles  
- Rounded buttons and circular indicators  
- Easy XML integration and programmatic customization  

---

## ⚙️ Customizable Attributes

| Attribute | Default | Description |
|-----------|--------|-------------|
| `circleFilledColor` | `#000000` | Color of filled PIN circles |
| `circleEmptyColor` | `#00000000` | Color of empty PIN circles |
| `circleStrokeColor` | `#000000` | Border color of circles |
| `circleSize` | `14dp` | Diameter of the circles |
| `buttonBackgroundColor` | `#000000` | Default button background color |
| `buttonTextColor` | `#FFFFFF` | Button text color |
| `buttonIconColor` | `#FFFFFF` | Button icon color |
| `clearButtonBackgroundColor` | `#FF0000` | Clear button background color |
| `clearButtonIconColor` | `#FFFFFF` | Clear button icon color |
| `doneButtonBackgroundColor` | `#00FF00` | Done button background color |
| `doneButtonIconColor` | `#FFFFFF` | Done button icon color |
| `buttonTextSize` | `14sp` | Button text size |
| `pinLength` | `4` | Length of the PIN |

---

## 🔧 Usage

### XML

```xml
<com.example.CustomPinView
    android:id="@+id/customPinView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:circleFilledColor="@color/black"
    app:circleEmptyColor="@color/transparent"
    app:circleStrokeColor="@color/black"
    app:buttonBackgroundColor="@color/black"
    app:buttonTextColor="@color/white"
    app:clearButtonBackgroundColor="@color/red"
    app:doneButtonBackgroundColor="@color/green"
    app:buttonTextSize="16sp"
    app:circleSize="20dp"/>
