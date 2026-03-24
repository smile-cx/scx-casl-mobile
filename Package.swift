// swift-tools-version:5.7
import PackageDescription

let package = Package(
    name: "SCXCASL",
    platforms: [
        .iOS(.v13),
        .macOS(.v10_15)
    ],
    products: [
        .library(
            name: "SCXCASL",
            targets: ["SCXCASL"]
        )
    ],
    targets: [
        .target(
            name: "SCXCASL",
            path: "casl-ios/Sources/SCXCASL"
        ),
        .testTarget(
            name: "SCXCASLTests",
            dependencies: ["SCXCASL"],
            path: "casl-ios/Tests/SCXCASLTests"
        )
    ]
)
