# User Guide — SMS Forwarder

🇫🇷 [Français](USER_GUIDE.md) | 🇬🇧 English

## Table of contents

1. [What is SMS Forwarder?](#1-what-is-sms-forwarder)
2. [Installation](#2-installation)
3. [First launch](#3-first-launch)
4. [Configure the destination number](#4-configure-the-destination-number)
5. [Enable forwarding](#5-enable-forwarding)
6. [Real-time dashboard](#6-real-time-dashboard)
7. [Forwarding rules](#7-forwarding-rules)
8. [HTTP webhook](#8-http-webhook)
9. [Monitoring third-party apps](#9-monitoring-third-party-apps)
10. [View the history](#10-view-the-history)
11. [Notification center](#11-notification-center)
12. [Diagnostics](#12-diagnostics)
13. [Configure filters](#13-configure-filters)
14. [Retry policy](#14-retry-policy)
15. [Change the language](#15-change-the-language)
16. [Statistics](#16-statistics)
17. [Android widget](#17-android-widget)
18. [Multi-SIM](#18-multi-sim)
19. [FAQ](#19-faq)

---

## 1. What is SMS Forwarder?

SMS Forwarder automatically forwards every SMS, RCS message and app notification you receive to a phone number or an HTTP webhook of your choice. The app runs in the background, without any intervention on your part, and keeps a complete history of all forwards.

The app is fully bilingual (French / English) and automatically detects your phone's language. All data stays on your device: nothing is sent to an external server (unless you configure a webhook).

---

## 2. Installation

### Download the APK

1. Get the APK file from the GitHub [Releases page](https://github.com/ronylicha/SMS-Forwarder/releases).
2. Transfer it to your phone (via USB cable, email or Bluetooth).

### Google Play

The app is also available on the [Google Play Store](https://play.google.com/store/apps/details?id=com.qr_communication.smsforwarder).

### Allow unknown sources

Before installing an APK file that does not come from the Google Play Store, Android asks you to allow installation from unknown sources.

1. Open **Settings** on your phone.
2. Go to **Apps** (or **App management**).
3. Tap the three dots in the top right, then **Special access**.
4. Tap **Install unknown apps**.
5. Select the file manager or app you use to open the APK, and enable the option.

### Install the app

1. Open the APK file with your file manager.
2. Tap **Install**.
3. Once installation is complete, tap **Open**.

---

## 3. First launch

On first launch, a 4-step onboarding guides you:

1. **Welcome** — overview of the main features
2. **Permissions** — each permission is explained individually
3. **Destination number** — target number configuration
4. **Test SMS** — sending a validation message

A security warning reminds you that SMS travel in plain text via your carrier's network.

### Permissions to accept

SMS Forwarder needs three permissions to operate:

| Permission | What it is used for |
|---|---|
| **Receive SMS** | Detect incoming SMS to forward them |
| **Send SMS** | Send SMS to the destination number |
| **Notifications** | Show the active background service |

### Notification access (for RCS messages)

So the app can also capture RCS messages (Google Messages, Samsung Messages):

1. In the app, tap **Settings**.
2. In the **Notification access** section, tap **Configure access**.
3. Android opens the system settings: enable **SMS Forwarder** in the list.
4. Return to the app. The **Access enabled** label confirms the configuration.

> This step is also required if you want to monitor third-party app notifications (WhatsApp, Telegram, etc.).

---

## 4. Configure the destination number

1. From the dashboard, tap **Settings**.
2. In the **Destination number** section, enter the number in international format: `+33 6 12 34 56 78`.
3. Tap **Save**.
4. A **confirmation toast** "Number saved" appears at the bottom of the screen.

### Test the configuration

1. In **Settings > Destination number**, tap **Send test**.
2. An SMS `[SMS Forwarder] This is a test SMS.` is sent to the configured number.
3. A toast confirms whether the send succeeded or failed.

---

## 5. Enable forwarding

1. Tap the **toggle button** in the center of the dashboard.
2. The card switches to active mode: **Forwarding active**, destination number displayed.
3. A persistent notification appears indicating that **SMS Forwarder is active**.

To disable forwarding, tap the same toggle button again.

---

## 6. Real-time dashboard

The dashboard displays real-time statistics that refresh automatically:

| Element | Description |
|---|---|
| **Sent 24h** | Number of SMS successfully forwarded in the last 24 hours |
| **Failed 24h** | Number of failures in the last 24 hours |
| **Total forwarded** | Global counter since installation |
| **Success rate** | Overall success percentage |
| **Notification badge** | Number of unread notifications (red dot) |

The dashboard provides quick access to: History, Rules, Diagnostics, Statistics and Settings.

---

## 7. Forwarding rules

Rules allow you to route messages differently based on sender and content. Each rule can send to a different SMS number or webhook.

Access: **Settings > Advanced > Forwarding rules** or from the dashboard.

### Create a rule

1. Tap **New rule**.
2. **Name**: a descriptive name (e.g. "2FA codes to webhook").
3. **Sender pattern (regex, optional)**: filter by number, e.g. `^(\+33|0)6`.
4. **Keyword / content regex (optional)**: filter the text, e.g. `code|otp|verification`.
5. **Destination**: SMS number or webhook URL.
6. **Enabled**: check to enable immediately.
7. Tap **Save**. A "Rule saved" toast appears.

### Test a rule

Before enabling a rule, you can test it with a sample SMS:

1. In the rule editor, test section.
2. Enter a **test sender** and a **test content**.
3. Tap **Test**.
4. The result indicates whether the rule matches.

> A rule with no pattern at all matches ALL SMS.

### Priority

Rules are evaluated in priority order. The first matching rule determines the destination. If no rule matches, the fallback uses the global destination.

---

## 8. HTTP webhook

The webhook allows you to send each message as JSON to any HTTP endpoint.

### Payload format

```json
POST https://your-endpoint.com/sms
Content-Type: application/json

{
  "sender": "+336****5678",
  "content": "Your code: 8472",
  "receivedAt": "2026-05-01T19:08:32Z",
  "sourceLabel": "WhatsApp",
  "originalDestination": "+336****0000"
}
```

| Field | Type | Description |
|---|---|---|
| `sender` | String | Sender's number |
| `content` | String | Full message body |
| `receivedAt` | String (ISO 8601) | Reception timestamp |
| `sourceLabel` | String? | Name of the source app (if third-party app notification) |
| `originalDestination` | String? | Configured global destination number |

### Configuration

1. Create a forwarding rule with destination type **Webhook**.
2. Enter the full URL of your endpoint.
3. Test with the **Test** button.

> The webhook uses native `HttpURLConnection` (no external dependency). In case of network failure, the automatic retry applies according to your configured policy.

---

## 9. Monitoring third-party apps

SMS Forwarder can intercept notifications from apps like WhatsApp, Telegram, Allo, Ringover or Onoff, and forward them as SMS or via webhook.

### Enable monitoring

1. Go to **Settings > Third-party apps**.
2. Enable **Notification monitoring**.
3. Tap **Manage apps**.
4. Add the apps whose notifications you want to forward.

The `sourceLabel` field in the webhook payload indicates the source app.

---

## 10. View the history

The history lists all received messages and the result of their forwarding.

### Status meanings

| Status | Color | Meaning |
|---|---|---|
| **Sent** | Green | The message was forwarded successfully |
| **Failed** | Red | The forwarding failed (network unavailable, etc.) |
| **Pending** | Blue | The forwarding is in progress or queued |
| **Filtered** | Gray | The message was blocked by a filter rule |

### Available filters

- **Text search**: by sender, content or destination
- **Status filter**: All, Sent, Failed, Pending, Filtered
- **Date range filter**: Material 3 DateRangePicker
- **Resend**: inline button on failed SMS

---

## 11. Notification center

The notification center gathers the app's system alerts:

- **Rule errors**: invalid regex, missing destination
- **Unreachable destination**: webhook or SMS repeatedly failing
- **Battery alerts**: battery optimization disabled
- **Quota**: plan alerts

Tap the dashboard badge or go to **Settings > Notification center** to view. Individual or bulk mark-as-read.

---

## 12. Diagnostics

The Diagnostics screen audits the system state:

| Check | Description |
|---|---|
| **Permissions** | Verifies that all required permissions are granted |
| **Battery optimization** | Checks whether the app is excluded from Doze mode |
| **Notification access** | Verifies NotificationListener access (RCS + third-party apps) |
| **Network connectivity** | For webhook rules |

Each check provides a **direct button** to the corresponding system settings.

---

## 13. Configure filters

Filters control which SMS are forwarded globally (independently from forwarding rules).

Access: **Settings > Filtering**.

| Mode | Behavior |
|---|---|
| **None** | All SMS are forwarded |
| **Whitelist** | Only SMS matching the rules are forwarded |
| **Blacklist** | SMS matching the rules are blocked |

---

## 14. Retry policy

The retry policy is configurable in **Settings > Retry policy**:

| Setting | Options |
|---|---|
| **Max attempts** | 1 to 10 (slider) |
| **Initial delay** | 30s, 1min, 5min, 15min |
| **Backoff multiplier** | x1.5, x2, x3 |

If a send fails, the app automatically retries according to this policy.

---

## 15. Change the language

The app is bilingual FR/EN with automatic detection.

1. Go to **Settings > Language**.
2. Choose from:
   - **Follow system**: FR if the phone is in French, EN otherwise
   - **French**: force French
   - **English**: force English
3. The app immediately restarts with the new language.
4. A "Language changed" toast confirms the change.

---

## 16. Statistics

The Statistics screen displays:

- **Summary**: total, sent, failed, filtered, pending, success rate
- **Daily chart**: daily activity over 7d / 14d / 30d
- **CSV export**: export of the full history

---

## 17. Android widget

The widget lets you enable/disable forwarding from the home screen:

1. Long-press an empty area of the home screen.
2. Tap **Widgets**.
3. Search for **SMS Forwarder**.
4. Drag the widget to the desired location.

The widget shows ON/OFF and the counter of forwarded SMS.

---

## 18. Multi-SIM

If your phone has two SIM cards:

- **Receiving SIM**: which SIMs are monitored (All / SIM 1 / SIM 2)
- **Sending SIM**: which SIM is used to send forwarded SMS

Configuration in **Settings > Multi-SIM** (appears automatically if 2 SIMs are detected).

---

## 19. FAQ

See the [FAQ.md](FAQ_EN.md) file for detailed frequently asked questions.

**Quick questions:**

- **Does forwarding work if I closed the app?** Yes. The Foreground Service stays active as long as the persistent notification is visible.
- **Will the app restart after a reboot?** Yes, automatically thanks to the BootReceiver.
- **How do I avoid a loop?** The app detects messages it sent itself + the destination number is automatically blacklisted.
- **Does the webhook work offline?** No, a data connection is required. The automatic retry applies on failure.
- **Do forwarded SMS cost me money?** Each forwarded SMS consumes one SMS from your plan. Forwarding via webhook is free (data).
- **Can I change the language?** Yes, in Settings > Language (System / French / English).
