# Passive Income - Mobile-First Responsive Web Application

A complete, fully-featured, and responsive mobile-first earning platform built with HTML5, Tailwind CSS, and Vanilla JavaScript with a modern orange and white color scheme.

---

## 🌟 Key Features & Business Logic (100% Preserved)

### 1. User Authentication System
- Sign-Up and Login supporting either **Email Address** or **Mobile/Phone Number** and secure password.
- Session persistence tracking user profiles, wallet balances (USD & PKR), active VIP plans, and daily ad counters.
- Built-in quick test buttons for **Demo Investor** (`demo@passiveincome.com` / `demo123`) and **System Admin** (`admin@passiveincome.com` / `admin123`).

### 2. 10 VIP Investment Plans
| Tier | Price (PKR) | Equivalent USD | Daily Ads Limit | Daily Max Earnings | 30-Day Potential |
|---|---|---|---|---|---|
| **VIP Plan 1** | 1,000 PKR | ~$3.57 | 5 Ads | +$0.25 USD | 2,100 PKR |
| **VIP Plan 2** | 2,000 PKR | ~$7.14 | 7 Ads | +$0.35 USD | 2,940 PKR |
| **VIP Plan 3** | 3,000 PKR | ~$10.71 | 10 Ads | +$0.50 USD | 4,200 PKR |
| **VIP Plan 4** | 4,000 PKR | ~$14.28 | 12 Ads | +$0.60 USD | 5,040 PKR |
| **VIP Plan 5** | 5,000 PKR | ~$17.85 | 15 Ads | +$0.75 USD | 6,300 PKR |
| **VIP Plan 6** | 6,000 PKR | ~$21.42 | 18 Ads | +$0.90 USD | 7,560 PKR |
| **VIP Plan 7** | 7,000 PKR | ~$25.00 | 20 Ads | +$1.00 USD | 8,400 PKR |
| **VIP Plan 8** | 8,000 PKR | ~$28.57 | 25 Ads | +$1.25 USD | 10,500 PKR |
| **VIP Plan 9** | 9,000 PKR | ~$32.14 | 30 Ads | +$1.50 USD | 12,600 PKR |
| **VIP Plan 10** | 10,000 PKR | ~$35.71 | 35 Ads | +$1.75 USD | 14,700 PKR |

- Plan purchase logic validates wallet balance and immediately activates the corresponding daily ad limit upon confirmation.

### 3. Fixed Ad Earnings & Counter Logic
- **Fixed Reward:** Exactly **$0.05 USD** credited directly to the user's wallet per ad watched.
- **5-Second Interactive Video Player:** Displays animated circular progress countdown and instant reward feedback.
- **24-Hour Auto-Reset:** Live countdown timer tracking the daily reset cycle. Ad watching locks automatically once the daily limit is reached.

### 4. Deposit Section (3 Methods + Screenshot Proof)
1. **USDT (TRC20):** Displays TRC20 wallet address with one-click copy and screenshot proof upload.
2. **JazzCash:** Displays official title and account number (0301-2345678) with screenshot receipt upload.
3. **EasyPaisa:** Displays official title and account number (0345-9876543) with screenshot receipt upload.
- Full receipt image preview modal for both users and administrators.

### 5. Withdrawal Section (3 Methods & Minimum Thresholds)
1. **USDT TRC20:** Minimum withdrawal threshold **$12 USD**.
2. **JazzCash:** Minimum withdrawal threshold **4,000 PKR**.
3. **EasyPaisa:** Minimum withdrawal threshold **4,000 PKR**.
- Requires account holder title and destination account/wallet address before submitting. Deductions and refund on rejection are handled seamlessly.

### 6. Admin Backoffice Portal
- Accessible anytime via the **Admin** button in the header.
- View and manage **Pending Deposits** (Approve & Credit or Reject).
- View and manage **Pending Withdrawals** (Mark Paid/Completed or Reject & Refund).
- Inspect all registered users, their balances, and active VIP tiers.

---

## 🚀 Live Hosting & Deployment Guide

### Deploying to Netlify
1. Connect your repository to [Netlify](https://www.netlify.com/).
2. Set **Publish directory** to `.` (or leave default, `netlify.toml` is pre-configured).
3. Click **Deploy Site** — your site will be live instantly!

### Deploying to Vercel
1. Import the repository into [Vercel](https://vercel.com/).
2. Framework Preset: **Other** / Static.
3. Root Directory: `./` (pre-configured via `vercel.json`).
4. Click **Deploy** — ready in seconds!

### Deploying to GitHub Pages
1. Go to repository **Settings** > **Pages**.
2. Source: **Deploy from a branch** > branch `main` / `root`.
3. Save — your app is live on `https://<username>.github.io/<repo>/`!

### Local Development / Testing
You can preview the app using any static web server:
```bash
# Using Node.js npx serve
npx serve -s .

# Or using Python
python3 -m http.server 8080
```
Then visit `http://localhost:8080` or `http://localhost:3000` in your mobile device or browser.
