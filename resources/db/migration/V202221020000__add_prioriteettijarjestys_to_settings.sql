alter table hakukohderyhma_settings add column if not exists prioriteettijarjestys jsonb default '[]'::jsonb not null;
