ALTER TABLE books
    DROP CONSTRAINT chk_books_topic;

ALTER TABLE books
    ALTER COLUMN topic TYPE VARCHAR(40);

ALTER TABLE books
    ADD CONSTRAINT chk_books_topic CHECK (
        topic IN (
            'PROGRAMMING',
            'FINANCE_INVESTING',
            'PSYCHOLOGY',
            'PERSONAL_GROWTH',
            'BUSINESS_ENTREPRENEURSHIP',
            'LANGUAGES',
            'PHILOSOPHY',
            'HEALTH_SPORTS',
            'FICTION',
            'BIOGRAPHY',
            'LAW',
            'MATH',
            'SCIENCE',
            'HISTORY',
            'OTHER'
        )
    );
