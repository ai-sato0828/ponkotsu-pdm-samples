
    create table customer_accounts (
        created_at timestamp(6) not null,
        id bigint not null,
        updated_at timestamp(6) not null,
        name varchar(255) not null,
        primary key (id)
    );

    create table purchase_orders (
        billing_postal_code varchar(8) not null,
        created_at timestamp(6) not null,
        id bigint not null,
        shipping_postal_code varchar(8) not null,
        updated_at timestamp(6) not null,
        billing_city varchar(80) not null,
        shipping_city varchar(80) not null,
        billing_street varchar(160) not null,
        shipping_street varchar(160) not null,
        primary key (id)
    );
