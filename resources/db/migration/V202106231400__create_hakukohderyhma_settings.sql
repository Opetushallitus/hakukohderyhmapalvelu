create table hakukohderyhma_settings
(
    hakukohderyhma_oid text not null,
    rajaava            boolean default false not null,
    max_hakukohteet    int check ((max_hakukohteet > 0 and rajaava is true) or (max_hakukohteet is null and rajaava is false)),
    created_at         timestamp with time zone default now() not null,
    updated_at         timestamp with time zone default now() not null,
    primary key (hakukohderyhma_oid)
);
