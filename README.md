# Offline Retry Interceptor (Android / Kotlin)

A lightweight, unopinionated OkHttp interceptor for Android that automatically queues failed network requests offline and silently retries them when connectivity is restored.

Built and maintained by MockNode.

Never lose a user's data submission just because they drove through a tunnel or lost cell service.

---

## Features

- 📡 **Offline Queueing** – Automatically catches `IOException` connection failures from OkHttp.
- 💾 **Zero-Boilerplate Storage** – Uses native Android `SharedPreferences` as a lightweight NoSQL queue.
- 🔄 **Smart Retries** – Silently flushes queued requests when connectivity returns.
- 🛡️ **GET Request Safe** – Automatically ignores `GET` requests to prevent stale fetches.
- ⚡ **Plug & Play** – One file, one line of initialization.
- 🚀 **Retrofit Compatible** – Works seamlessly with Retrofit and OkHttp.

---

## Installation

### Gradle

> Replace with your actual Maven Central or JitPack coordinates once published.

```kotlin
dependencies {
    implementation("dev.mocknode:offline-retry-interceptor:1.0.0")
}
```

---

## Quick Start

### Initialize the Interceptor

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(
        OfflineRetryInterceptor(context)
    )
    .build()
```

Use Retrofit or OkHttp exactly as you normally would.

### Example Request

```kotlin
try {
    val request = Request.Builder()
        .url("https://api.example.com/submit")
        .post(jsonBody)
        .build()

    client.newCall(request).execute()

} catch (e: IOException) {

    // Device is offline.
    // The interceptor automatically saves the request
    // and retries it later when connectivity returns.

    Log.d(
        "Network",
        "Device offline. Request queued securely."
    )
}
```

---

## How It Works

### 1. Request Fails

When OkHttp throws an `IOException` because the device has no internet connection:

```text
POST /submit
        ↓
IOException
```

The interceptor catches the exception internally.

---

### 2. Request Is Queued

For supported HTTP methods:

- POST
- PUT
- PATCH
- DELETE

the interceptor serializes:

- URL
- Headers
- Request Body
- HTTP Method

and stores them in a private `SharedPreferences` queue.

```text
Offline Request
        ↓
Serialize
        ↓
SharedPreferences Queue
```

`GET` requests are intentionally ignored.

---

### 3. Exception Still Propagates

The original exception is re-thrown so your UI can respond appropriately.

Example:

```kotlin
showSnackbar("You're offline. Changes saved and will sync automatically.")
```

---

### 4. Connectivity Returns

The next successful network request acts as proof that connectivity is available again.

```text
Successful Request
        ↓
Internet Restored
        ↓
Flush Queue
```

---

### 5. Automatic Retry

A lightweight background thread:

1. Loads queued requests
2. Rebuilds the original OkHttp requests
3. Sends them sequentially
4. Removes successful requests from storage

```text
Queue
  ↓
Rebuild Requests
  ↓
Retry Sequentially
  ↓
Success
  ↓
Remove From Queue
```

All of this happens silently in the background.

---

## Supported Methods

| Method | Queued Offline |
|----------|----------|
| POST | ✅ |
| PUT | ✅ |
| PATCH | ✅ |
| DELETE | ✅ |
| GET | ❌ |
| HEAD | ❌ |
| OPTIONS | ❌ |

---

## Why SharedPreferences?

Most offline retry libraries introduce:

- Room databases
- SQL schemas
- Migrations
- Extra dependencies

This interceptor intentionally uses Android's built-in `SharedPreferences` because:

- It is fast
- It is lightweight
- It requires zero setup
- It is perfect for small offline queues

---

## Use Cases

Perfect for:

- Forms
- Surveys
- Check-ins
- Attendance systems
- CRM apps
- Field sales applications
- Delivery apps
- Inventory management
- Expense submissions
- Offline-first mobile applications

---

## Example Scenario

A delivery driver submits a proof-of-delivery form:

```text
Driver taps Submit
        ↓
Cell signal drops
        ↓
Request saved locally
        ↓
Driver continues working
        ↓
Signal returns
        ↓
Request automatically synced
```

No data loss.

No manual retry button.

No user intervention required.

---

## Thread Safety

- Queue writes are synchronized.
- Retries execute in a background thread.
- UI thread remains unaffected.
- Safe for Retrofit and OkHttp concurrent usage.

---

## Limitations

- Intended for small to medium request payloads.
- SharedPreferences is not suitable for large file uploads.
- Multipart uploads are not currently supported.
- Requests are retried sequentially.
- No retry backoff strategy is applied by default.

---

## Roadmap

- [ ] Exponential backoff retries
- [ ] Multipart request support
- [ ] Queue inspection APIs
- [ ] Encryption support
- [ ] Room-backed storage option
- [ ] Retry analytics hooks

---

## License

MIT License

Copyright (c) MockNode

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software.

---

## Author

MockNode

Build reliable mobile applications that continue working even when the network doesn't.
