# Directory Search and Replace

**Command-line Utility (Groovy)**  
**Author:** Ramil Jamolod

---

## 📌 Overview

**Directory Search and Replace** is a Groovy-based command-line utility that searches and replaces text in `.txt` files within a specified directory and its subdirectories.

The tool supports:
- Case-sensitive and case-insensitive matching
- Recursive or shallow directory scanning
- Automatic file backups
- Detailed logging
- Interactive mode for user-friendly input

> ⏱️ This project was completed in **1 day**, including learning the Groovy language.

---

## 🧪 Coding Exercise

- 📎 **Exercise Link:**  
  https://drive.google.com/file/d/1pE6Sx5q1eplGaQpd8HZqyxX707qh7PC3/view?usp=sharing

---

## ⚙️ Prerequisites

- **Groovy** (version `5.0.3`)
- **Java Runtime Environment (JRE)** or **Java Development Kit (JDK)**

Verify Groovy installation:
```bash
groovy -version
```

---

## 📥 Installation

No installation required.

1. Clone or download the project.
2. Ensure `groovy` is available in your system `PATH`.

---

## 🚀 Usage

```bash
groovy App.groovy <target_directory> <pattern> <replacement> <log_path>
```

---

## 🧾 Arguments

| Argument | Description |
|--------|------------|
| `<target_directory>` | Absolute or relative path to the directory to scan |
| `<pattern>` | String pattern to search for |
| `<replacement>` | String to replace the matched pattern |
| `<log_path>` *(optional)* | Directory where logs will be saved (defaults to `<target_directory>/.log`) |

---

## 🚩 Flags

| Flag | Description |
|-----|------------|
| `--shallow` | Scan only the top-level directory |
| `--ignore-case` | Perform case-insensitive matching |
| `--form` | Interactive mode |

---

## 🧑‍💻 Examples

```bash
groovy App.groovy /path/to/directory "old_text" "new_text"
```

```bash
groovy App.groovy --form --ignore-case --shallow
```

---

## ✨ Features

- Recursive scanning (default)
- Optional shallow scanning
- Case-sensitive and insensitive matching
- Automatic backups
- Detailed logging
- Robust error handling

---

## 📤 Output

- Modified files are overwritten
- Backups stored in:
  ```
  <target_directory>/.backup/<process_id>/<filename>
  ```
- Log file includes:
  - Start and end times
  - Modified files count
  - Line and column changes
  - Before/after content

---

## 📝 Notes

- Processes only `.txt` files
- Hidden directories are skipped
- Each run uses a unique process ID

---

## 🛠 Troubleshooting

- Ensure directory exists and is accessible
- Verify Groovy installation
- Review log file for errors
