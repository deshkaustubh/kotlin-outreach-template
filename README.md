# 🚀 Kotlin Outreach Engine

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)

This repository is a **Reusable Template** for automated, high-volume email outreach. I developed this as the **Coordinator for the AI Mobile Hackathon 2026** to bridge the gap between community developers and industry leaders.

---

## How it Works
The engine uses **Kotlin Scripting** and **GitHub Actions** to bypass the manual labor of sending hundreds of emails. It follows a "Throttled Delivery" strategy to ensure your Gmail account isn't flagged as spam by sending small batches (10 emails) every 6 hours.

### Key Logic:
1. **Persistence:** The script reads a CSV, checks who hasn't been emailed yet, sends the mail, and writes a "Sent at [Timestamp]" back to the CSV.
2. **Auto-Retry:** If an email fails due to a network glitch, the status is marked as "Error," and the engine will automatically try again in the next 6-hour cycle.
3. **Smart Placeholders:** It dynamically replaces `{{Name}}` in your Subject and Body with the recipient's actual name.

---

## Project Structure & Data Formatting

### 1. The Data Folder (`/data`)
Because of privacy (GDPR/Data Protection), the actual mailing list is **ignored by Git** via `.gitignore`.

- **`recipients.sample.csv`**: A public example of the required format.
- **`recipients.csv`**: **(Create this file manually)**. This is where you paste your 250+ leads.

### CSV Column Structure:
The script expects exactly three columns (Header row is required):

| Name | Email | Status (Leave Empty) |
| :--- | :--- | :--- |
| John Doe | john@example.com | |
| Jane Smith | jane@example.com | |

*Once sent, the engine updates the 3rd column to:* `Sent at 2026-03-26T14:30:00`

### 2. The Engine (`outreach.main.kts`)
A standalone Kotlin script that handles:
- **SMTP SSL/TLS Connection** via Port 465.
- **HTML Content Rendering** for professional-looking "Card" layouts.
- **Batch Limiting** to stay within Gmail's safety limits.

---

## Setup Instructions (The "Coordinator" Workflow)

### 1. Repository Setup
1. Click **"Use this template"** and create a **Private** repository.
2. Upload your `data/recipients.csv` to your new private repo.

### 2. Security (GitHub Secrets)
Go to **Settings > Secrets and variables > Actions** and add:
- `GMAIL_USER`: Your Gmail address.
- `GMAIL_PASS`: Your 16-character **Google App Password**.
- `EMAIL_SUBJECT`: `Exciting News for {{Name}}! AI Mobile Hackathon 2026`
- `EMAIL_BODY`: Your full HTML template code.

### 3. Permissions
Go to **Settings > Actions > General**. Under **Workflow permissions**, select **"Read and write permissions"**. This allows the script to update your CSV file after sending emails.

### 4. Automation
The script is set to run every 6 hours via `.github/workflows/outreach.yml`. You can also trigger it manually by going to the **Actions** tab and clicking **"Run workflow"**.

---

## Let's Connect
As the Coordinator for this event, I'm always looking to connect with mobile developers and AI enthusiasts.

- **Portfolio:** [kaustubhdeshpande.tech](http://kaustubhdeshpande.tech/)
- **LinkedIn:** [deshkaustubh](https://www.linkedin.com/in/deshkaustubh/)
- **GitHub:** [deshkaustubh](https://github.com/deshkaustubh)

---
*Disclaimer: Use this tool responsibly. Ensure all recipients have opted-in to receive communications in accordance with local anti-spam laws.*