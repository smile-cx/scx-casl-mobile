# CASL Mobile

Native ports of [CASL](https://github.com/stalniy/casl) — the isomorphic authorization JavaScript library — for iOS (Swift) and Android (Java).

## Packages

### casl-ios — Swift
An idiomatic Swift port of CASL for iOS and macOS applications.

- **Language:** Swift
- **Minimum targets:** iOS 13, macOS 10.15
- **Distribution:** Swift Package Manager (module `SCXCASL`)

```swift
// Package.swift
.package(url: "https://github.com/smile-cx/scx-casl-mobile", from: "1.0.0")
// product: .product(name: "SCXCASL", package: "SCXCASL")
```

### casl-android — Java
An idiomatic Java port of CASL for Android applications.

- **Language:** Java
- **Minimum SDK:** 21
- **Distribution:** JitPack (group `cx.smile`, artifact `CASL-android`)

```kotlin
// settings.gradle
maven { url 'https://jitpack.io' }

// build.gradle
implementation 'com.github.smile-cx:scx-casl-mobile:1.0.0'
```

## About CASL

CASL is an isomorphic authorization library that restricts what resources a given user is allowed to access. It is designed to be incrementally adoptable and can be used with any front-end or back-end.

Original library: https://github.com/stalniy/casl
Author: Sergii Stotskyi

## License and third-party notices

This repository is licensed under the MIT License. See the [LICENSE](LICENSE) file for the full license text.

This repository contains original code and may also include redistributed or modified third-party software. Where applicable, license texts, copyright notices, attribution notices, and other required third-party notices are retained in accordance with their respective licenses. If this distribution includes modified third-party code, it should not be confused with an official upstream release.

### Attribution

The design, API concepts, and logic of this port are derived from [CASL](https://github.com/stalniy/casl), an isomorphic authorization JavaScript library by Sergii Stotskyi, licensed under the MIT License. See the [NOTICE](NOTICE) file for the full upstream attribution and license text.
