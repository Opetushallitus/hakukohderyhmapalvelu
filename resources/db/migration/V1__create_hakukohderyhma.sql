create table hakukohderyhma
(
    hakukohderyhma_oid text                                   not null,
    hakukohde_oid      text                                   not null,
    created_at         timestamp with time zone default now() not null,
    primary key (hakukohderyhma_oid, hakukohde_oid)
);
