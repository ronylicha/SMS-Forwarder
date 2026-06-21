# FAQ — SMS Forwarder

🇫🇷 [Français](FAQ.md) | 🇬🇧 English

Frequently asked questions about using SMS Forwarder.

---

## Does the app consume a lot of battery?

SMS Forwarder runs via a Foreground Service (visible through its persistent notification). This type of service is designed to have a small footprint: it does nothing as long as no message arrives.

In practice, the consumption is negligible. If your phone is restrictive about battery (Xiaomi, Huawei, Samsung), exclude SMS Forwarder from battery optimization:

**Settings > Apps > SMS Forwarder > Battery > Not optimized**

You can check the status in the app's **Diagnostics** screen.

---

## Do forwarded SMS cost me money?

Each SMS forwarding sends a real SMS via your carrier's network. If your plan includes unlimited SMS, there will be no additional cost.

**Forwarding via HTTP webhook, on the other hand, is free** (uses your data connection).

SMS longer than 160 characters are split into multiple parts by the GSM network and may count as several SMS.

---

## Is my data sent to a server?

No. SMS Forwarder does not communicate with any external server by default. All your data is stored exclusively on your device.

**Exception**: if you configure a webhook, messages matching that rule are sent only to the URL you defined. You keep full control. The source code is open and verifiable on GitHub.

---

## Does the app work with RCS messages?

Yes. RCS messages are captured via two complementary mechanisms:

- **ContentObserver**: monitors the system SMS/RCS inbox
- **NotificationListener**: intercepts notifications from Google Messages, Samsung Messages and AOSP Messages

Enable notification access in **Settings > Notification access > Configure access**.

---

## Can I forward WhatsApp, Telegram, etc. notifications?

Yes. SMS Forwarder can monitor third-party app notifications and forward them as SMS or via webhook.

1. Go to **Settings > Third-party apps**.
2. Enable monitoring.
3. Add the desired apps (WhatsApp, Telegram, Allo, Ringover, Onoff, etc.).

The `sourceLabel` field indicates the source app in the webhook payload.

---

## How do forwarding rules work?

Each incoming message is analyzed by your rules, in priority order:

1. **Sender pattern (regex)**: filter by number or pattern
2. **Keyword (regex)**: condition on the content
3. **Destination**: SMS or webhook, specific to that rule

If a rule matches, the message is sent to its destination. If no rule matches, the fallback uses the global destination.

You can test each rule interactively before enabling it.

---

## What happens on a send failure?

The app automatically retries according to your **configurable retry policy**:

- **Max attempts**: 1 to 10
- **Initial delay**: 30s, 1min, 5min or 15min
- **Exponential backoff**: x1.5, x2 or x3

Beyond the number of attempts, the message switches to **Failed** status and you can resend it manually from the history.

---

## Does the webhook work without an internet connection?

No. The webhook requires a data connection. On failure (network unavailable, endpoint unreachable), the app retries according to your retry policy. The notification center alerts you of repeated errors.

---

## How do I avoid a forwarding loop?

SMS Forwarder automatically detects messages it sent itself and excludes them. In addition, the destination number is automatically added to the blacklist. It is impossible to create an infinite loop.

---

## Does the app work after a phone reboot?

Yes. On reboot, the BootReceiver automatically restarts the service if forwarding was active. Your configuration, your rules and the activation state are preserved.

---

## Can I change the app language?

Yes. The app is bilingual FR/EN with automatic detection (FR if the phone is in French, EN otherwise).

**Settings > Language**: Follow system / French / English. The change is immediate (activity restart).

---

## Can I filter SMS by sender?

Yes, in two ways:

1. **Global filters** (Settings > Filtering): whitelist / blacklist by number or keyword
2. **Forwarding rules** (Settings > Forwarding rules): advanced routing with regex, per-rule destination and interactive test

---

## How do I export my history?

1. Open **Statistics** or **History**.
2. Tap **Export CSV**.
3. Choose the save location.

The file contains for each message: sender, content, reception date, forwarding date, status and number of attempts.

---

## What Android version is required?

**Android 8.0 (Oreo)** minimum (API 26). The Material You design is available on Android 12+.

---

## Is the app available as open source?

Yes, under the **AGPL-3.0** license. The source code is fully available on [GitHub](https://github.com/ronylicha/SMS-Forwarder). You can audit it, modify it and redistribute it.

---

## How do I uninstall cleanly?

1. Long-press the SMS Forwarder icon.
2. **Uninstall**.
3. Confirm.

All data is deleted. Export your history to CSV beforehand if you want to keep it.
