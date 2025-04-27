# SoundSaga – AudioBook Player for Android

[![Min SDK Version](https://img.shields.io/badge/Min%20SDK-29-green.svg)]()

A lightweight, feature-rich audiobook player for Android, featuring a splash screen, grid-based browse layout, chapter swipe navigation, variable playback speeds, and a “My Books” tracker for in-progress listening.

---

## Table of Contents

- [Features](#features)  
- [Demo Screenshots](#demo-screenshots)  
- [Getting Started](#getting-started)  
  - [Prerequisites](#prerequisites)  
  - [Installation](#installation)  
- [Usage](#usage)  
  - [Main Screen](#main-screen)  
  - [AudioBook Screen](#audiobook-screen)  
  - [My Books Screen](#my-books-screen)  
- [Data Source](#data-source)  
- [Error Handling](#error-handling)  
- [Project Structure](#project-structure)  
- [Resources](#resources)  
- [Contributing](#contributing)  
- [License](#license)

---

## Features

✅ **Splash Screen**  
Displays app logo on launch before navigating to main list.  

✅ **Responsive Grid Layout**  
Portrait: 2-column grid; Landscape: 4-column grid using `RecyclerView` + `GridLayoutManager`.

✅ **Browse & Select**  
Fetches audiobook catalog from remote JSON via Volley. Tap to open, long-press for details dialog.

✅ **Playback Activity**  
Swipe or arrow-tap to change chapters. SeekBar with current/total time and play/pause controls.

✅ **Variable Playback Speed**  
Popup menu to choose speeds: 0.75×, 1.0×, 1.1×, 1.25×, 1.5×, 1.75×, 2.0×.

✅ **My Books Tracker**  
Tracks in-progress books, sorted by most recent. Tap to resume, long-press to delete.  

✅ **Persistent Progress**  
Save and restore last-played position and chapter per book.

✅ **Graceful Error Handling**  
Network errors, bad URLs, or playback failures show dialogs and exit or revert to main screen.

---

## Demo Screenshots

![Screenshot 2025-04-27 125229](https://github.com/user-attachments/assets/209bbc62-8cd1-4388-a541-fd8db69ea178)
![Screenshot 2025-04-27 125238](https://github.com/user-attachments/assets/d6aecf4a-1616-4f10-ac3d-532f1778afd8)
![Screenshot 2025-04-27 125248](https://github.com/user-attachments/assets/2b55dadc-7c64-44f7-afa2-35a29ce1f9bc)
![Screenshot 2025-04-27 125305](https://github.com/user-attachments/assets/f7de85c3-d94b-4938-a578-4485516ac6c4)


---

## Getting Started

### Prerequisites

- Android Studio (Arctic Fox or later)  
- Android SDK API Level 29 or higher  
- Internet connection for initial data fetch  

### Installation

1. **Clone the repository**  
   ```bash
   https://github.com/vinodonweb/sound-saga-android
   cd sound-saga-android
