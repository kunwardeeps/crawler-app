# Website Change Monitor Android App

## Overview

This Android app allows users to monitor changes on various websites and receive notifications when changes occur. Example use cases include:
- Monitoring restaurant availability on lunch ordering sites
- Tracking commute ETA on Google Maps
- Watching for price changes on Amazon or Google Flights

## Features

- **Website Monitoring**: Add monitors for any website, including login-protected sites.
- **Configurable Frequency**: Set how often each monitor checks for changes (e.g., every 10 minutes, daily).
- **Change Detection**: Get notified when relevant content changes (e.g., new restaurants, price drops, ETA updates).
- **Push Notifications**: Receive alerts directly on your device.
- **Secure Storage**: Credentials and settings are stored securely.
- **Resource Efficient**: Uses background jobs to minimize battery and data usage.

## Example Use Cases

1. **Lunch Ordering**: Log in to your lunch ordering website. The app checks every 10 minutes and notifies you when restaurants are available.
2. **Commute ETA**: Set up a Google Maps search for your commute. Get notified about the ETA at a fixed time each day.
3. **Amazon Price Watch**: Monitor a product page for price changes and receive alerts.
4. **Flight Price Tracking**: Track flight prices on Google Flights and get notified of changes.

## How It Works

1. **Add a Monitor**: Enter the website URL, login details (if needed), and what to monitor (e.g., price, availability).
2. **Set Frequency**: Choose how often the app should check for changes.
3. **Get Notified**: Receive a notification when a change is detected.

## Security

- All user credentials are encrypted and stored securely on the device.
- No data is shared with third parties.

## Requirements

- Android 8.0 (API 26) or higher
- Internet connection

## Tech Stack

- Java
- Android SDK
- WorkManager for background tasks
- EncryptedSharedPreferences for secure storage
- Retrofit/Jsoup for web requests and parsing

## Setup

1. Clone the repository.
2. Open in Android Studio.
3. Build and run on your device or emulator.

## License

MIT License