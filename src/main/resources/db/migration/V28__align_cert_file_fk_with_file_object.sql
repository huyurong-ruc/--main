alter table cert_template drop constraint if exists cert_template_file_id_fkey;
alter table cert_template add constraint cert_template_file_id_fkey foreign key (file_id) references file_object(id);

alter table cert_application drop constraint if exists cert_application_generated_pdf_file_id_fkey;
alter table cert_application add constraint cert_application_generated_pdf_file_id_fkey foreign key (generated_pdf_file_id) references file_object(id);

alter table cert_generated_file drop constraint if exists cert_generated_file_file_id_fkey;
alter table cert_generated_file add constraint cert_generated_file_file_id_fkey foreign key (file_id) references file_object(id);
