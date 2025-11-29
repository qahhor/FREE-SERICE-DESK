-- Seed data for Currency Exchange module

INSERT INTO marketplace_modules (
    module_id, name, description, short_description, category, author,
    documentation_url, icon, pricing_model, latest_version,
    minimum_platform_version, status, is_verified, is_featured, is_official, published_at
) VALUES (
    'currency-exchange',
    'Currency Exchange',
    'Real-time currency exchange rates from the Central Bank of Uzbekistan (CBU.uz). Features include:

• **Live Exchange Rates**: Get current exchange rates for 70+ currencies updated daily by CBU
• **Currency Conversion**: Convert amounts between any supported currencies with UZS as base
• **Historical Data**: View and compare exchange rates from any past date
• **Dashboard Widget**: Display popular currency rates directly on your dashboard
• **Converter Widget**: Quick currency conversion tool for agents
• **Multi-language Support**: Currency names available in English, Russian, Uzbek (Latin & Cyrillic)

Perfect for organizations dealing with international customers or multi-currency transactions.',
    'Real-time currency exchange rates from CBU.uz with conversion and historical data',
    'FINANCE',
    'Service Desk Team',
    'https://docs.servicedesk.uz/modules/currency-exchange',
    'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0OCIgaGVpZ2h0PSI0OCIgdmlld0JveD0iMCAwIDQ4IDQ4IiBmaWxsPSJub25lIj48cmVjdCB3aWR0aD0iNDgiIGhlaWdodD0iNDgiIHJ4PSIxMCIgZmlsbD0iIzEwQjk4MSIvPjxwYXRoIGQ9Ik0yNCA4djMyTTMwIDE2SDIwYTQgNCAwIDAgMCAwIDhoOGE0IDQgMCAwIDEgMCA4SDE0IiBzdHJva2U9IndoaXRlIiBzdHJva2Utd2lkdGg9IjMiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCIvPjwvc3ZnPg==',
    'FREE',
    '1.0.0',
    '1.0.0',
    'PUBLISHED',
    TRUE,
    TRUE,
    TRUE,
    NOW()
);

-- Add screenshots
INSERT INTO module_screenshots (module_id, screenshot_url)
SELECT id, url FROM marketplace_modules,
    unnest(ARRAY[
        '/assets/modules/currency-exchange/screenshot1.png',
        '/assets/modules/currency-exchange/screenshot2.png',
        '/assets/modules/currency-exchange/widget-preview.png'
    ]) as url
WHERE module_id = 'currency-exchange';

-- Add tags
INSERT INTO module_tags (module_id, tag)
SELECT id, tag FROM marketplace_modules,
    unnest(ARRAY['currency', 'finance', 'uzbekistan', 'cbu', 'exchange', 'converter', 'uzs', 'rates']) as tag
WHERE module_id = 'currency-exchange';

-- Add version
INSERT INTO module_versions (
    module_id, version, release_notes, changelog,
    minimum_platform_version, configuration_schema, default_configuration,
    required_permissions, status, is_stable, published_at
)
SELECT
    id,
    '1.0.0',
    'Initial release of the Currency Exchange module',
    '## Version 1.0.0

### Features
- Live exchange rates from CBU.uz
- Currency conversion between 70+ currencies
- Historical rate data
- Dashboard widget for popular currencies
- Quick converter widget

### Supported Currencies
USD, EUR, RUB, GBP, CHF, JPY, CNY, KZT, and 60+ more',
    '1.0.0',
    '{
        "type": "object",
        "properties": {
            "defaultCurrency": {
                "type": "string",
                "title": "Default Currency",
                "default": "USD"
            },
            "popularCurrencies": {
                "type": "array",
                "title": "Popular Currencies",
                "items": { "type": "string" },
                "default": ["USD", "EUR", "RUB", "GBP"]
            },
            "refreshInterval": {
                "type": "integer",
                "title": "Refresh Interval (minutes)",
                "default": 30,
                "minimum": 5
            },
            "showDiff": {
                "type": "boolean",
                "title": "Show Rate Changes",
                "default": true
            }
        }
    }',
    '{"defaultCurrency": "USD", "popularCurrencies": ["USD", "EUR", "RUB", "GBP", "CNY", "KZT"], "refreshInterval": 30, "showDiff": true}',
    '["modules.currency.view", "modules.currency.convert"]',
    'PUBLISHED',
    TRUE,
    NOW()
FROM marketplace_modules
WHERE module_id = 'currency-exchange';
