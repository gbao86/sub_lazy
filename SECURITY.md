# Security Policy

## Supported Versions

As the app is currently in active development, security updates are applied to the latest development version.

| Version | Supported |
| ------- | --------- |
| < 0.0.7 | No        |
| 0.0.7   | Yes       |

## Security Architecture

**Sub Lazy** is built with user privacy and security as top priorities:
- **Offline-First Data Storage**: All subscription data, payment histories, and bank details for VietQR payment splitting are stored locally on your device in a secure Room SQLite database. No data is synchronized to external servers.
- **On-Device Machine Learning (OCR)**: Screenshot scanning and text recognition via Google ML Kit are executed completely on-device. No images or parsed text are uploaded or transmitted over the network.
- **No Sensitive Permissions**: The app does not request SMS reading or Gmail permissions to parse bills, keeping your accounts fully secure.

## Reporting a Vulnerability

If you discover a security vulnerability in this project, please report it immediately by sending an email to the developer:
- **Email**: [tiktokthu10@gmail.com](mailto:tiktokthu10@gmail.com)

We will investigate and address the issue as soon as possible.
