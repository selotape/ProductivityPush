# ProductivityPush Chrome Extension

A Chrome extension that blocks distracting websites and provides daily motivation to boost productivity.

## Features

- **Website Blocking**: Block time-wasting sites like YouTube, Facebook, 9GAG, etc.
- **Motivation System**: Daily task tracking and motivational messages
- **Simple Toggle**: Easy on/off switch in popup
- **Custom Block Page**: Motivational page shown when sites are blocked
- **Daily Progress**: Track completed tasks and blocked attempts

## Installation

1. Open Chrome and navigate to `chrome://extensions/`
2. Enable "Developer mode" in the top right
3. Click "Load unpacked" and select the `chrome-extension` folder
4. The ProductivityPush icon should appear in your toolbar

## Usage

1. Click the extension icon to open the popup
2. Toggle "Enable Blocking" to start blocking distracting sites
3. Customize blocked sites in the settings (coming soon)
4. Track your daily progress and stay motivated!

## Development

The extension is built with:
- Manifest V3 for modern Chrome extension standards
- Service Worker for background processing
- Content scripts for site blocking
- Chrome Storage API for persistence

## Files Structure

- `manifest.json` - Extension configuration
- `src/popup.html/js` - Extension popup interface
- `src/background.js` - Background service worker
- `src/content.js` - Content script for blocking
- `src/blocked.html` - Blocked site page with motivation