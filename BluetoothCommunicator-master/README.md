# RESQMESH

**RESQMESH** is an offline, peer-to-peer (P2P) mesh communication Android application powered by Bluetooth Low Energy (BLE). It allows users to connect, chat, and relay messages across multiple devices without requiring an internet connection or cellular coverage—making it ideal for emergency and rescue scenarios.

## Features

- **Offline Mesh Networking:** Uses BLE to create a self-healing P2P mesh network. Messages hop from device to device to reach their destination.
- **Real-Time Chat:** Send and receive messages with peers on the network. Includes a "Clear Chat" feature.
- **Dynamic Network Visualization:** Visually maps out connected peers in real-time, showing the current topology of your local mesh.
- **Robust Connection Handling:** Automatically handles dropped connections and offers a "Restart Scan" feature to quickly flush and re-discover nearby peers without needing to restart the app.

## Tech Stack

- **Platform:** Android (API Level 24+)
- **Language:** Kotlin (UI & Business Logic) & Java (Underlying BLE library)
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM with Coroutines & StateFlow
- **Local Storage:** Room Database for message persistence

## Setup & Installation

1. Clone this repository.
2. Open the project in **Android Studio**.
3. Sync project with Gradle files.
4. Build and run the `meshmap` module on a physical Android device. (Note: Bluetooth functionality cannot be fully tested on an emulator).

## Permissions Required

- `BLUETOOTH`, `BLUETOOTH_ADMIN`
- `BLUETOOTH_SCAN`, `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT` (Android 12+)
- `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` (Required for BLE scanning on older Android versions)

## License
MIT License
