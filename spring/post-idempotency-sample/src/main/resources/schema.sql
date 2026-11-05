CREATE TABLE application_receipt (
    request_id UUID NOT NULL,
    title VARCHAR(120) NOT NULL,
    CONSTRAINT pk_application_receipt PRIMARY KEY (request_id)
);
