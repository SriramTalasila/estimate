# Frontend Usage Guide

This document explains how to use the bundled frontend in `src/main/resources/static/index.html`.

The page is a single-file Vue 3 application served directly by Spring Boot. It provides the full UI for:

- user sign-in and sign-up
- business profile setup
- category and product management
- estimate creation and editing
- PDF printing and sharing
- demo mode testing without a live backend

## Where It Runs

When the backend is running, open:

- `http://localhost:8080/`
- or `http://localhost:8080/index.html`

The frontend is bundled inside the backend and does not need a separate build step.

## Frontend Stack

The page uses CDN-hosted libraries directly in the HTML:

- Vue 3
- Tailwind CSS
- Lucide icons

There is no separate frontend build system, component tree, or routing framework. All UI logic lives in the single HTML file.

## Main Screens

The app has three top-level views:

- `auth`: sign in / sign up page
- `dashboard`: main working area after login
- `estimate-form`: create or edit estimate screen

Inside the dashboard there are three tabs:

- `Estimates`
- `Catalog (Products & Categories)`
- `Business Profile`

## First-Time User Flow

For a new live user, the intended flow is:

1. Open the app.
2. Create an account from the `Sign Up` tab.
3. Sign in.
4. If no business profile exists, the app automatically redirects to `Business Profile`.
5. Create the business profile.
6. Add categories.
7. Add products inside categories.
8. Go to `Estimates` and create the first estimate.

The UI blocks estimate and catalog work until a business profile exists.

## API Settings

Use the server icon in the top-right header to open `API Configuration`.

You can change:

- `Base API URL`
- `Enable Demo Mode (Mock API)`

Default API base URL:

- the current browser origin, via `window.location.origin`

Examples:

- `http://localhost:8080` when opened locally from the Spring Boot app
- your deployed host when opened from a deployed environment

### Live API Mode

In live mode, the page calls the backend endpoints and sends authentication cookies with every request.

### Demo Mode

In demo mode, the page uses in-memory mock data for:

- categories
- products
- estimates
- login state

This is useful when:

- the backend is not running
- you want to test the UI quickly
- you want to demo the app without a database

Important behavior:

- demo mode starts with a mock admin user after sign-in
- demo data is not persisted
- page refresh resets mock data
- sharing is simulated only
- printing uses browser print, not backend PDF generation

The UI also tries to force non-admin live users out of demo mode after login.

## Authentication

The auth screen has two tabs:

- `Sign In`
- `Sign Up`

### Sign Up

Fields:

- username
- email
- password

On success, the UI shows a success message and switches back to the sign-in tab.

### Sign In

Fields:

- username
- password

On success:

- the app stores authenticated state in memory
- user roles are read from the response
- business profile is fetched
- the dashboard opens

If the session expires, the frontend logs the user out and returns to the auth screen.

## Business Profile

The `Business Profile` tab is where the user sets the company details used in estimates.

Fields:

- business logo
- shop/business name
- shop address
- phone number
- GST / tax number

### How It Works

- If no business exists, the screen opens in edit mode automatically.
- If a business exists, the page first shows a read-only card.
- Clicking `Edit` opens the form.
- Saving calls either create or update business APIs.

### Logo Upload

The UI allows logo upload from the browser and converts the image to a base64 data URL.

Current caveat:

- the backend business DTOs do not appear to support a `logo` field
- the frontend keeps the logo in local UI state after save
- the logo may disappear after a reload because the backend likely does not persist it

Recommended image guidance shown in the UI:

- around `250x100`
- max `1MB`

## Catalog Usage

The `Catalog` tab is split into two areas:

- left sidebar: categories
- right panel: products for the selected category

### Categories

Users can:

- create a category with the inline input
- click a category to load its products

Current UI behavior:

- category update is not exposed in the page even though the backend supports it
- category delete is not exposed in the page

### Products

After selecting a category, users can:

- add a product
- view products in a table
- delete a product

Product fields:

- name
- price
- unit

Current UI behavior:

- product update is not exposed in the page even though the backend supports it
- product delete uses a confirmation modal

## Estimates Dashboard

The `Estimates` tab shows:

- stats cards for today, this month, and this year
- a search box
- a list of saved estimates
- actions for each estimate

Estimate list actions:

- share via WhatsApp
- print/download PDF
- edit
- delete

### Search

The search box filters the estimate list in the browser. It is a client-side filter over the currently loaded estimate list.

### Stats

In live mode:

- stats are fetched from `/api/business/{businessId}/estimates/stats`

In demo mode:

- stats are calculated in the browser from mock estimate dates and totals

## Creating An Estimate

Click `New Estimate` in the `Estimates` tab.

The estimate form has two main columns.

### Left Column

- customer name
- customer phone
- overall additional discount
- tax amount
- notes
- category discounts

### Right Column

- line items table
- live totals summary

### Line Items

Each row contains:

- product/service selector
- quantity
- price
- item discount
- row total

Selecting a product auto-fills the row price from the catalog.

### Category Discounts

Category discounts are applied based on the categories used in the selected line items.

Behavior:

- category discount options only appear for categories currently used in the form
- a line item uses its category discount by default
- a row can override the category discount and use a manual item discount instead

### Totals

The frontend computes totals live in the browser:

- subtotal
- item discounts total
- additional discount value
- total discount
- final amount

Calculation order:

1. Sum all row subtotals.
2. Apply either category discount or row discount.
3. Apply overall additional discount.
4. Add flat tax amount.

### Save Actions

The form has two save buttons:

- `Save`
- `Save & Print`

Behavior:

- `Save` stores the estimate and returns to the dashboard
- `Save & Print` stores the estimate and then opens print behavior

At least one valid line item is required before save.

## Editing And Deleting Estimates

From the estimates table, users can:

- edit an estimate
- delete an estimate after confirmation

Editing loads:

- customer details
- tax and additional discount
- line items
- notes
- category discounts

## Print And PDF Behavior

### Live Mode

The print action:

- requests `/api/estimates/{estimateId}/pdf`
- downloads the PDF
- opens the browser/system print dialog through a hidden iframe

### Demo Mode

The print action uses `window.print()` on the current page instead of fetching a generated PDF.

## Share Behavior

The share action is designed around WhatsApp sharing.

### On supported mobile browsers

The page first tries the native Web Share API with the generated PDF attached.

### Fallback behavior

If native file sharing is unavailable:

- the PDF is downloaded first
- WhatsApp Web is opened with a prefilled message
- the user attaches the downloaded PDF manually if needed

The share message includes:

- customer name
- estimate number
- total amount

Phone numbers are sanitized before building the WhatsApp URL.

## Error Handling And Feedback

The UI uses:

- inline success messages
- inline error messages
- confirmation modal for destructive actions
- loading spinners on submit and fetch operations

If API requests return `401`, the UI treats that as session expiry and logs the user out.

## Important Frontend Limitations

- The app is a single large HTML file, so UI, state, and network logic are tightly coupled.
- Business logo upload appears to be frontend-only and may not survive reloads.
- Category edit and delete are not available in the UI.
- Product edit is not available in the UI.
- Demo mode data is reset on refresh.
- The page depends on CDN scripts for Vue, Tailwind, and Lucide.
- Currency formatting is hardcoded to `INR`.
- Date formatting is hardcoded to an English locale style.

## Backend Endpoints Used By The Frontend

Authentication:

- `POST /api/auth/signup`
- `POST /api/auth/signin`
- `POST /api/auth/signout`

Business:

- `GET /api/manage/my-business`
- `POST /api/manage/business`
- `PUT /api/manage/business/{businessId}`

Catalog:

- `GET /api/manage/business/{businessId}/categories`
- `POST /api/manage/business/{businessId}/categories`
- `GET /api/manage/categories/{categoryId}/products`
- `POST /api/manage/categories/{categoryId}/products`
- `DELETE /api/manage/products/{productId}`

Estimates:

- `GET /api/business/{businessId}/estimates`
- `GET /api/business/{businessId}/estimates/stats`
- `POST /api/business/{businessId}/estimates`
- `PUT /api/estimates/{estimateId}`
- `DELETE /api/estimates/{estimateId}`
- `GET /api/estimates/{estimateId}/pdf`

## Recommended Usage Sequence

For a smooth live setup:

1. Start the Spring Boot backend and PostgreSQL.
2. Open `http://localhost:8080/`.
3. Confirm the API base URL matches the current app host.
4. Keep demo mode off.
5. Sign up and sign in.
6. Create the business profile.
7. Add categories.
8. Add products.
9. Create estimates.
10. Use print/share from the estimate list.

For UI-only exploration:

1. Open the page.
2. Enable demo mode from `API Configuration`.
3. Sign in with any values.
4. Explore profile, catalog, and estimate workflows.

## Suggested Future Improvements

- Split the page into multiple Vue components.
- Persist frontend settings such as API base URL and demo mode.
- Add category edit/delete actions to match backend capabilities.
- Add product editing to match backend capabilities.
- Add server-side validation feedback per field.
- Persist business logos in the backend.
- Reduce reliance on CDN assets for production deployments.
