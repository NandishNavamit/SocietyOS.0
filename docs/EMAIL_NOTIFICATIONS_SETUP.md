# Email notifications (prototype)

In-app notifications always remain available. Email copies are optional and must
be enabled deliberately; a mail failure never blocks a bill, payment, complaint,
or invitation flow.

Create a local `.env` file from `.env.example` and set:

```ini
APP_MAIL_ENABLED=true
APP_MAIL_FROM=your-address@example.com
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-address@example.com
SMTP_PASSWORD=your-provider-app-password
SMTP_AUTH=true
SMTP_STARTTLS=true
```

For Gmail, use an app password rather than your normal account password. Keep
`.env` private; it is excluded from version control. Restart the backend after
changing any of these values.

When mail is disabled, society administrators can still copy an invitation link
from Society Setup, and every recipient can still use the in-app inbox.
