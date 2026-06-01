# Offline Retry Interceptor
[![](https://jitpack.io/v/fajarcp/offline_retry_interceptor_android.svg)](https://jitpack.io/#fajarcp/offline_retry_interceptor_android)
A lightweight Android/Kotlin OkHttp interceptor that automatically queues failed network requests when the device is offline and silently retries them when connectivity returns.

Designed for mobile applications where losing user submissions is unacceptable.

> Never lose a user's form submission because they entered a tunnel, switched networks, or temporarily lost signal.

---

## Features

✅ Automatic Offline Queueing
✅ SharedPreferences-Based Storage (No Database Required)
✅ Automatic Retry When Connectivity Returns
✅ Retrofit Compatible
✅ Safe by Default (GET Requests Ignored)
✅ Minimal Setup
✅ Zero Additional Runtime Dependencies

---

## Installation

### Step 1: Add JitPack Repository

Add JitPack to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}
```

For older Gradle versions:

```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2: Add Dependency

```kotlin
dependencies {
    implementation("com.github.fajarcp:offline-retry-interceptor:1.0.1")
}
```

---

## Quick Start

Add the interceptor to your OkHttp client:

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(
        OfflineRetryInterceptor(context)
    )
    .build()
```

Use Retrofit or OkHttp exactly as you normally would.

---

## Example

```kotlin
try {

    val request = Request.Builder()
        .url("https://api.example.com/submit")
        .post(jsonBody)
        .build()

    client.newCall(request).execute()

} catch (e: IOException) {

    // Request has already been queued
    // by OfflineRetryInterceptor.

    showSnackbar(
        "You're offline. Changes will sync automatically."
    )
}
```

---

## How It Works

### When Offline

```text
POST Request
      │
      ▼
IOException
      │
      ▼
Request Serialized
      │
      ▼
SharedPreferences Queue
```

The interceptor automatically captures failed requests caused by connectivity issues.

---

### Supported Offline Methods

The following request types are stored and retried:

| HTTP Method | Supported |
| ----------- | --------- |
| POST        | ✅         |
| PUT         | ✅         |
| PATCH       | ✅         |
| DELETE      | ✅         |
| GET         | ❌         |
| HEAD        | ❌         |
| OPTIONS     | ❌         |

GET requests are intentionally excluded to avoid replaying stale fetch operations.

---

### When Connectivity Returns

The next successful network request acts as proof that internet access is available.

```text
Successful Request
      │
      ▼
Flush Queue
      │
      ▼
Replay Stored Requests
      │
      ▼
Remove Successful Entries
```

Retries occur automatically in the background.

No user action required.

---

## What Gets Stored?

For each queued request:

* URL
* HTTP Method
* Headers
* Request Body

This allows the original request to be reconstructed and replayed accurately.

---

## Why SharedPreferences?

Many offline synchronization libraries require:

* Room Database
* SQL Schema Management
* Migrations
* Additional Dependencies

Offline Retry Interceptor uses Android's built-in SharedPreferences because it is:

* Lightweight
* Fast
* Reliable
* Available on every Android device
* Ideal for small-to-medium offline queues

---

## Typical Use Cases

Perfect for:

* Survey Applications
* Attendance Systems
* CRM Applications
* Field Sales Apps
* Delivery Applications
* Inventory Management
* Expense Tracking
* Check-In Systems
* Data Collection Apps
* Offline-First Mobile Applications

---

## Real-World Example

```text
Driver Submits Delivery Form
             │
             ▼
Network Lost
             │
             ▼
Request Saved Locally
             │
             ▼
Driver Continues Working
             │
             ▼
Connectivity Restored
             │
             ▼
Request Synced Automatically
```

No lost submissions.

No retry button.

No manual intervention.

---

## Thread Safety

* Queue operations are synchronized.
* Retries run on a background thread.
* UI thread remains unaffected.
* Safe for concurrent Retrofit and OkHttp usage.

---

## Limitations

* Intended for small-to-medium payloads.
* Not suitable for large file uploads.
* Multipart requests are currently unsupported.
* Requests are retried sequentially.
* No retry backoff strategy is currently implemented.

---

## Roadmap

* [ ] Exponential Backoff Retries
* [ ] Multipart Upload Support
* [ ] Queue Inspection APIs
* [ ] Request Encryption
* [ ] Room Database Storage Option
* [ ] Retry Analytics Hooks
* [ ] Retry Status Callbacks

---

## Contributing

Contributions, issues, and feature requests are welcome.

Feel free to open an issue or submit a pull request.

---

## License

MIT License

Copyright (c) Fajar C P

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software.

---

## Author

**Fajar C P**

Android • Flutter • .NET • Open Source

Building reliable software that continues working even when the network doesn't.
