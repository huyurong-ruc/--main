alter table cert_template drop constraint cert_template_output_format_check;
alter table cert_template add constraint cert_template_output_format_check check (output_format in ('pdf', 'docx'));
